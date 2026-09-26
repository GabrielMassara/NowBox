package com.nowbox.nowbox_api.modules.box.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import com.nowbox.nowbox_api.modules.box.dto.BoxCreateDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxFilterDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxLoteDTO;
import com.nowbox.nowbox_api.modules.box.dto.BoxResponseDTO;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.box.repository.IBoxRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.unidade.repository.IUnidadeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BoxService {

    private static final int LIMITE_LOTE = 200;
    private static final int TAMANHO_MAXIMO_NUMERO = 20;

    private final IBoxRepository boxRepository;
    private final IUnidadeRepository unidadeRepository;

    public Page<BoxResponseDTO> listAllByFilter(Pageable pageable, BoxFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idUnidade = null;
        String numero = null;
        Boolean disponivel = null;
        Boolean alugado = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdUnidade()))) {
                idUnidade = filtro.getIdUnidade();
            }
            if(StringUtils.hasText(filtro.getNumero())) {
                numero = filtro.getNumero();
            }
            if(filtro.getDisponivel() != null) {
                disponivel = filtro.getDisponivel();
            }
            if(filtro.getAlugado() != null) {
                alugado = filtro.getAlugado();
            }
        }

        return boxRepository.findAllByFilter(idUnidade, numero, disponivel, alugado, pageable).map(this::toResponseDTO);
    }

    public BoxResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<BoxEntity> encontrado = boxRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public BoxResponseDTO create(BoxCreateDTO box) throws NaoEncontradoException {

        Optional<UnidadeEntity> unidade = unidadeRepository.findByIdAndDeletedAtIsNull(box.getIdUnidade());

        // Se não encontrar a unidade
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade inválida");
        }

        BoxEntity created = boxRepository.save(BoxEntity.builder()
                .numero(box.getNumero())
                .unidade(unidade.get())
                .tamanho(box.getTamanho())
                .dimensoes(box.getDimensoes())
                .disponivel(liberadoPorPadrao(box.getDisponivel()))
                .preco(box.getPreco())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public List<BoxResponseDTO> createBatch(BoxLoteDTO lote) throws NaoEncontradoException {

        Optional<UnidadeEntity> unidade = unidadeRepository.findByIdAndDeletedAtIsNull(lote.getIdUnidade());

        // Se não encontrar a unidade
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade inválida");
        }

        List<String> numeros = gerarNumeros(lote);

        Set<String> existentes = new HashSet<>(boxRepository.findNumerosCadastrados(
                lote.getIdUnidade(), numeros.stream().map(String::toLowerCase).toList()));

        List<BoxEntity> novos = numeros.stream()
                .filter(numero -> !existentes.contains(numero.toLowerCase()))
                .map(numero -> BoxEntity.builder()
                        .numero(numero)
                        .unidade(unidade.get())
                        .tamanho(lote.getTamanho())
                        .dimensoes(lote.getDimensoes())
                        .disponivel(liberadoPorPadrao(lote.getDisponivel()))
                        .preco(lote.getPreco())
                        .build())
                .toList();

        return boxRepository.saveAll(novos).stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public BoxResponseDTO update(BoxCreateDTO box, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<BoxEntity> existente = boxRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Box não encontrado");
        }

        // busca a unidade
        Optional<UnidadeEntity> unidade = unidadeRepository.findByIdAndDeletedAtIsNull(box.getIdUnidade());
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade não encontrada");
        }

        BoxEntity updated = boxRepository.save(BoxEntity.builder()
                .id(id)
                .numero(box.getNumero())
                .unidade(unidade.get())
                .tamanho(box.getTamanho())
                .dimensoes(box.getDimensoes())
                .disponivel(box.getDisponivel())
                .preco(box.getPreco())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        BoxEntity existente = boxRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Box não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        boxRepository.save(existente);
    }

    private boolean liberadoPorPadrao(Boolean disponivel) {
        return disponivel == null || disponivel;
    }

    // Monta os numeros do intervalo, com prefixo e zeros a esquerda opcionais
    private List<String> gerarNumeros(BoxLoteDTO lote) {
        Integer inicial = lote.getNumeroInicial();
        Integer finalDoIntervalo = lote.getNumeroFinal();

        if(inicial == null || finalDoIntervalo == null || inicial < 0 || finalDoIntervalo < inicial) {
            throw new RequisicaoInvalidaException("Intervalo de numeração inválido");
        }

        if((long) finalDoIntervalo - inicial + 1 > LIMITE_LOTE) {
            throw new RequisicaoInvalidaException("O lote aceita no máximo " + LIMITE_LOTE + " boxes por vez");
        }

        String prefixo = lote.getPrefixo() == null ? "" : lote.getPrefixo().trim();
        int largura = Boolean.TRUE.equals(lote.getCompletarComZeros()) ? String.valueOf(finalDoIntervalo).length() : 0;

        List<String> numeros = IntStream.rangeClosed(inicial, finalDoIntervalo)
                .mapToObj(n -> prefixo + "0".repeat(Math.max(0, largura - String.valueOf(n).length())) + n)
                .toList();

        if(numeros.stream().anyMatch(numero -> numero.length() > TAMANHO_MAXIMO_NUMERO)) {
            throw new RequisicaoInvalidaException("O número do box pode ter no máximo " + TAMANHO_MAXIMO_NUMERO + " caracteres");
        }

        return numeros;
    }

    private BoxResponseDTO toResponseDTO(BoxEntity entidade) {
        return BoxResponseDTO.builder()
                .id(entidade.getId())
                .numero(entidade.getNumero())
                .unidade(entidade.getUnidade())
                .tamanho(entidade.getTamanho())
                .dimensoes(entidade.getDimensoes())
                .disponivel(entidade.getDisponivel())
                .preco(entidade.getPreco())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
