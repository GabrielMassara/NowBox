package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.entity.StatusAluguel;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.repository.IArquivoAluguelRepository;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage;
import com.nowbox.nowbox_api.modules.contrato.messaging.ContratoSolicitadoMessage.Alteracao;
import com.nowbox.nowbox_api.modules.email.messaging.EmailSolicitadoMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final IAluguelRepository aluguelRepository;
    private final IBoxRepository boxRepository;
    private final IClienteRepository clienteRepository;
    private final IArquivoAluguelRepository arquivoAluguelRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Page<AluguelResponseDTO> listAllByFilter(Pageable pageable, AluguelFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idBox = null;
        UUID idCliente = null;
        StatusAluguel status = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdBox()))) {
                idBox = filtro.getIdBox();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdCliente()))) {
                idCliente = filtro.getIdCliente();
            }
            if(filtro.getStatus() != null) {
                status = filtro.getStatus();
            }
        }

        return aluguelRepository.findAllByFilter(idBox, idCliente, status, pageable).map(this::toResponseDTO);
    }

    public AluguelResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<AluguelEntity> encontrado = aluguelRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Aluguel não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public AluguelResponseDTO create(AluguelCreateDTO aluguel) throws NaoEncontradoException {

        Optional<BoxEntity> box = boxRepository.findByIdAndDeletedAtIsNull(aluguel.getIdBox());

        // Se não encontrar o box
        if(box.isEmpty()) {
            throw new NaoEncontradoException("Box inválido");
        }

        Optional<ClienteEntity> cliente = clienteRepository.findByIdAndDeletedAtIsNull(aluguel.getIdCliente());

        // Se não encontrar o cliente
        if(cliente.isEmpty()) {
            throw new NaoEncontradoException("Cliente inválido");
        }

        // Um box bloqueado nao pode ser alugado
        validarBoxLiberado(box.get());

        // Um box so pode ter um aluguel em andamento por vez (pendente de assinatura ou ativo). Tambem fica ocupado o box que um aluguel acabou de deixar enquanto o aditivo dele aguarda assinatura
        if(aluguelRepository.existsByBoxIdAndStatusNotAndDeletedAtIsNull(aluguel.getIdBox(), StatusAluguel.INATIVO)
                || aluguelRepository.existsByBoxAnteriorIdAndStatusAndDeletedAtIsNull(aluguel.getIdBox(), StatusAluguel.PENDENTE_ASSINATURA_ADITIVO)) {
            throw new ConflitoException("O box já possui um aluguel ativo");
        }

        // Todo aluguel nasce pendente da assinatura do contrato. Ele so fica ativo quando o contrato assinado e enviado
        AluguelEntity created = aluguelRepository.save(AluguelEntity.builder()
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .status(StatusAluguel.PENDENTE_ASSINATURA_CONTRATO)
                .build());

        solicitarContrato(created);

        return toResponseDTO(created);
    }

    @Transactional
    public AluguelResponseDTO update(AluguelCreateDTO aluguel, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<AluguelEntity> existente = aluguelRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Aluguel não encontrado");
        }

        // busca o box
        Optional<BoxEntity> box = boxRepository.findByIdAndDeletedAtIsNull(aluguel.getIdBox());
        if(box.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        // busca o cliente
        Optional<ClienteEntity> cliente = clienteRepository.findByIdAndDeletedAtIsNull(aluguel.getIdCliente());
        if(cliente.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        AluguelEntity atual = existente.get();

        // So um aluguel ativo pode ser alterado. Antes disso o contrato ainda nao foi assinado e depois o contrato esta encerrado ou em encerramento
        if(atual.getStatus() != StatusAluguel.ATIVO) {
            throw new ConflitoException("O aluguel só pode ser alterado enquanto o contrato está ativo");
        }

        // Um box bloqueado nao pode ser alugado. So valida quando o aluguel troca de box, para que o bloqueio do box atual nao impeça editar o aluguel que ja estava ativo nele
        boolean mudouDeBox = atual.getBox() == null || !aluguel.getIdBox().equals(atual.getBox().getId());
        if(mudouDeBox) {
            validarBoxLiberado(box.get());
        }

        // Um box so pode ter um aluguel ativo por vez, desconsiderando o proprio aluguel
        if(aluguelRepository.existsByBoxIdAndStatusNotAndDeletedAtIsNullAndIdNot(aluguel.getIdBox(), StatusAluguel.INATIVO, id)
                || aluguelRepository.existsByBoxAnteriorIdAndStatusAndDeletedAtIsNullAndIdNot(aluguel.getIdBox(), StatusAluguel.PENDENTE_ASSINATURA_ADITIVO, id)) {
            throw new ConflitoException("O box já possui um aluguel ativo");
        }

        List<Alteracao> alteracoes = identificarAlteracoes(atual, box.get(), cliente.get(), aluguel);

        // O contrato original e mantido e a situacao nao e alterada aqui. A edicao so gera um aditivo descrevendo o que mudou
        AluguelEntity updated = aluguelRepository.save(AluguelEntity.builder()
                .id(id)
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                // Com alteracao o aditivo precisa ser assinado antes de o aluguel voltar a ficar ativo
                .status(alteracoes.isEmpty() ? atual.getStatus() : StatusAluguel.PENDENTE_ASSINATURA_ADITIVO)
                .contrato(atual.getContrato())
                .distrato(atual.getDistrato())
                .contratoAssinado(atual.getContratoAssinado())
                .distratoAssinado(atual.getDistratoAssinado())
                .boxAnterior(alteracoes.isEmpty() ? null : atual.getBox())
                .clienteAnterior(alteracoes.isEmpty() ? null : atual.getCliente())
                .valorAnterior(alteracoes.isEmpty() ? null : atual.getValor())
                .observacaoAnterior(alteracoes.isEmpty() ? null : atual.getObservacao())
                .createdAt(atual.getCreatedAt())
                .build());

        // Sem alteracao nao ha o que aditar nem o que avisar ao cliente
        if(!alteracoes.isEmpty()) {
            solicitarAditivo(updated, alteracoes);
            solicitarEmail(updated, a -> EmailSolicitadoMessage.aluguelAlterado(a, alteracoes));
        }

        return toResponseDTO(updated);
    }

    // Inicia o encerramento do contrato: gera o distrato e o aluguel fica pendente da assinatura dele. So vira inativo quando o distrato assinado e enviado
    @Transactional
    public AluguelResponseDTO encerrar(UUID id) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if(aluguel.getStatus() != StatusAluguel.ATIVO) {
            throw new ConflitoException("Apenas um contrato ativo pode ser encerrado");
        }

        // Um distrato de uma tentativa cancelada antes nao vale para esta: a tela espera o novo ficar pronto
        aluguel.setDistrato(null);
        aluguel.setStatus(StatusAluguel.PENDENTE_ASSINATURA_DISTRATO);
        AluguelEntity encerrado = aluguelRepository.save(aluguel);

        solicitarDistrato(encerrado);

        return toResponseDTO(encerrado);
    }

    // Desiste da assinatura pendente: o contrato nunca assinado deixa o aluguel inativo, o aditivo desfaz as alteracoes e o distrato mantem o aluguel ativo
    @Transactional
    public AluguelResponseDTO cancelarPendencia(UUID id) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        switch (aluguel.getStatus()) {
            case PENDENTE_ASSINATURA_CONTRATO -> aluguel.setStatus(StatusAluguel.INATIVO);
            case PENDENTE_ASSINATURA_DISTRATO -> {
                aluguel.setDistrato(null);
                aluguel.setStatus(StatusAluguel.ATIVO);
            }
            case PENDENTE_ASSINATURA_ADITIVO -> restaurarAntesDoAditivo(aluguel);
            default -> throw new ConflitoException("O aluguel não está aguardando assinatura");
        }

        return toResponseDTO(aluguelRepository.save(aluguel));
    }

    private void restaurarAntesDoAditivo(AluguelEntity aluguel) {
        // O box anterior fica reservado enquanto o aditivo esta pendente, entao ninguem o alugou e ele sempre pode ser restaurado
        if (aluguel.getBoxAnterior() != null) {
            aluguel.setBox(aluguel.getBoxAnterior());
        }
        if (aluguel.getClienteAnterior() != null) {
            aluguel.setCliente(aluguel.getClienteAnterior());
        }
        aluguel.setValor(aluguel.getValorAnterior());
        aluguel.setObservacao(aluguel.getObservacaoAnterior());
        aluguel.setBoxAnterior(null);
        aluguel.setClienteAnterior(null);
        aluguel.setValorAnterior(null);
        aluguel.setObservacaoAnterior(null);
        aluguel.setStatus(StatusAluguel.ATIVO);

        arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(aluguel.getId()).ifPresent(aditivo -> {
            aditivo.setPendenteAssinatura(false);
            aditivo.setCancelado(true);
            arquivoAluguelRepository.save(aditivo);
        });
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        AluguelEntity existente = aluguelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        aluguelRepository.save(existente);
    }

    // Apenas solicita o contrato original. A geracao e assincrona pelo nowbox-jobs e o envio so acontece depois do commit
    private void solicitarContrato(AluguelEntity aluguel) {
        eventPublisher.publishEvent(ContratoSolicitadoMessage.contrato(aluguel));
    }

    // Solicita o aditivo com as alteracoes feitas no contrato original, tambem de forma assincrona
    private void solicitarAditivo(AluguelEntity aluguel, List<Alteracao> alteracoes) {
        eventPublisher.publishEvent(ContratoSolicitadoMessage.aditivo(aluguel, alteracoes));
    }

    // Solicita o distrato do contrato, tambem de forma assincrona
    private void solicitarDistrato(AluguelEntity aluguel) {
        eventPublisher.publishEvent(ContratoSolicitadoMessage.distrato(aluguel));
    }

    // Compara o aluguel salvo com os dados recebidos e lista os campos que mudaram
    private List<Alteracao> identificarAlteracoes(AluguelEntity atual, BoxEntity novoBox, ClienteEntity novoCliente, AluguelCreateDTO novo) {
        List<Alteracao> alteracoes = new ArrayList<>();

        adicionarSeMudou(alteracoes, "Box", atual.getBox() != null ? atual.getBox().getNumero() : null, novoBox.getNumero());
        adicionarSeMudou(alteracoes, "Cliente", atual.getCliente() != null ? atual.getCliente().getNome() : null, novoCliente.getNome());
        adicionarSeMudou(alteracoes, "Valor", formatarValor(atual.getValor()), formatarValor(novo.getValor()));
        adicionarSeMudou(alteracoes, "Observação", textoOuTraco(atual.getObservacao()), textoOuTraco(novo.getObservacao()));

        return alteracoes;
    }

    private void adicionarSeMudou(List<Alteracao> alteracoes, String campo, String anterior, String novo) {
        if(!Objects.equals(anterior, novo)) {
            alteracoes.add(new Alteracao(campo, anterior, novo));
        }
    }

    private String formatarValor(BigDecimal valor) {
        return valor == null ? "-" : NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private String textoOuTraco(String texto) {
        return StringUtils.hasText(texto) ? texto.trim() : "-";
    }

    // Avisa o cliente do aluguel (registrado, alterado ou encerrado). O envio é assincrono pelo nowbox-jobs e só acontece depois do commit e é ignorado se o cliente nao tem email
    private void solicitarEmail(AluguelEntity aluguel, Function<AluguelEntity, EmailSolicitadoMessage> mensagem) {
        if(!StringUtils.hasText(aluguel.getCliente().getEmail())) {
            return;
        }

        eventPublisher.publishEvent(mensagem.apply(aluguel));
    }

    // Disponivel false indica que o box esta bloqueado para locacao
    private void validarBoxLiberado(BoxEntity box) {
        if(Boolean.FALSE.equals(box.getDisponivel())) {
            throw new ConflitoException("O box está bloqueado para locação");
        }
    }

    private UUID buscarIdAditivoPendente(AluguelEntity aluguel) {
        if (aluguel.getStatus() != StatusAluguel.PENDENTE_ASSINATURA_ADITIVO) {
            return null;
        }

        return arquivoAluguelRepository.findFirstByAluguelIdAndPendenteAssinaturaTrueOrderBySalvoEmDesc(aluguel.getId())
                .map(ArquivoAluguelEntity::getId)
                .orElse(null);
    }

    private AluguelResponseDTO toResponseDTO(AluguelEntity entidade) {
        return AluguelResponseDTO.builder()
                .id(entidade.getId())
                .box(entidade.getBox())
                .cliente(entidade.getCliente())
                .valor(entidade.getValor())
                .observacao(entidade.getObservacao())
                .status(entidade.getStatus())
                .contratoGerado(entidade.getContrato() != null)
                .distratoGerado(entidade.getDistrato() != null)
                .contratoAssinado(entidade.getContratoAssinado() != null)
                .distratoAssinado(entidade.getDistratoAssinado() != null)
                .idAditivoPendente(buscarIdAditivoPendente(entidade))
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
