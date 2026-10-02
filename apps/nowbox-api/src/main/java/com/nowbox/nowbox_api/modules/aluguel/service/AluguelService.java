package com.nowbox.nowbox_api.modules.aluguel.service;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelCreateDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelFilterDTO;
import com.nowbox.nowbox_api.modules.aluguel.dto.AluguelResponseDTO;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.aluguel.repository.IAluguelRepository;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
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
    private final ApplicationEventPublisher eventPublisher;

    public Page<AluguelResponseDTO> listAllByFilter(Pageable pageable, AluguelFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idBox = null;
        UUID idCliente = null;
        Boolean status = null;

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

        // Um box so pode ter um aluguel ativo por vez
        if(aluguelRepository.existsByBoxIdAndStatusTrueAndDeletedAtIsNull(aluguel.getIdBox())) {
            throw new ConflitoException("O box já possui um aluguel ativo");
        }

        // Todo aluguel nasce ativo. A situacao so muda pelo encerramento do contrato
        AluguelEntity created = aluguelRepository.save(AluguelEntity.builder()
                .box(box.get())
                .cliente(cliente.get())
                .valor(aluguel.getValor())
                .observacao(aluguel.getObservacao())
                .status(true)
                .build());

        solicitarContrato(created);
        solicitarEmail(created, EmailSolicitadoMessage::aluguelRegistrado);

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

        // Um contrato encerrado nao pode ser alterado nem reativado
        if(!Boolean.TRUE.equals(atual.getStatus())) {
            throw new ConflitoException("O contrato deste aluguel foi encerrado e não pode ser alterado");
        }

        // Um box bloqueado nao pode ser alugado. So valida quando o aluguel troca de box, para que o bloqueio do box atual nao impeça editar o aluguel que ja estava ativo nele
        boolean mudouDeBox = atual.getBox() == null || !aluguel.getIdBox().equals(atual.getBox().getId());
        if(mudouDeBox) {
            validarBoxLiberado(box.get());
        }

        // Um box so pode ter um aluguel ativo por vez, desconsiderando o proprio aluguel
        if(aluguelRepository.existsByBoxIdAndStatusTrueAndDeletedAtIsNullAndIdNot(aluguel.getIdBox(), id)) {
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
                .status(atual.getStatus())
                .contrato(atual.getContrato())
                .distrato(atual.getDistrato())
                .createdAt(atual.getCreatedAt())
                .build());

        // Sem alteracao nao ha o que aditar nem o que avisar ao cliente
        if(!alteracoes.isEmpty()) {
            solicitarAditivo(updated, alteracoes);
            solicitarEmail(updated, a -> EmailSolicitadoMessage.aluguelAlterado(a, alteracoes));
        }

        return toResponseDTO(updated);
    }

    // Encerra o contrato: o aluguel fica inativo e gera um distrato. Nao existe o caminho inverso, um aluguel encerrado nao volta a ficar ativo
    @Transactional
    public AluguelResponseDTO encerrar(UUID id) throws NaoEncontradoException {
        AluguelEntity aluguel = aluguelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Aluguel não encontrado"));

        if(!Boolean.TRUE.equals(aluguel.getStatus())) {
            throw new ConflitoException("O contrato deste aluguel já foi encerrado");
        }

        aluguel.setStatus(false);
        AluguelEntity encerrado = aluguelRepository.save(aluguel);

        solicitarDistrato(encerrado);
        solicitarEmail(encerrado, EmailSolicitadoMessage::aluguelEncerrado);

        return toResponseDTO(encerrado);
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

    private AluguelResponseDTO toResponseDTO(AluguelEntity entidade) {
        return AluguelResponseDTO.builder()
                .id(entidade.getId())
                .box(entidade.getBox())
                .cliente(entidade.getCliente())
                .valor(entidade.getValor())
                .observacao(entidade.getObservacao())
                .status(entidade.getStatus())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
