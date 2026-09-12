package com.nowbox.nowbox_api.modules.atribuicao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoCreateDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoFilterDTO;
import com.nowbox.nowbox_api.modules.atribuicao.dto.AtribuicaoResponseDTO;
import com.nowbox.nowbox_api.modules.atribuicao.entity.AtribuicaoEntity;
import com.nowbox.nowbox_api.modules.atribuicao.repository.IAtribuicaoRepository;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.usuario.entity.UsuarioEntity;
import com.nowbox.nowbox_api.modules.usuario.repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtribuicaoService {

    private final IAtribuicaoRepository atribuicaoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ICargoRepository cargoRepository;

    public Page<AtribuicaoResponseDTO> listAllByFilter(Pageable pageable, AtribuicaoFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idUsuario = null;
        UUID idCargo = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdUsuario()))) {
                idUsuario = filtro.getIdUsuario();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdCargo()))) {
                idCargo = filtro.getIdCargo();
            }
        }

        return atribuicaoRepository.findAllByFilter(idUsuario, idCargo, pageable).map(this::toResponseDTO);
    }

    public AtribuicaoResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<AtribuicaoEntity> encontrado = atribuicaoRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Atribuição não encontrada");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public AtribuicaoResponseDTO create(AtribuicaoCreateDTO atribuicao) throws NaoEncontradoException {

        Optional<UsuarioEntity> usuario = usuarioRepository.findByIdAndDeletedAtIsNull(atribuicao.getIdUsuario());

        // Se não encontrar o usuario
        if(usuario.isEmpty()) {
            throw new NaoEncontradoException("Usuário inválido");
        }

        Optional<CargoEntity> cargo = cargoRepository.findById(atribuicao.getIdCargo());

        // Se não encontrar o cargo
        if(cargo.isEmpty()) {
            throw new NaoEncontradoException("Cargo inválido");
        }

        AtribuicaoEntity created = atribuicaoRepository.save(AtribuicaoEntity.builder().usuario(usuario.get()).cargo(cargo.get()).build());

        return toResponseDTO(created);
    }

    @Transactional
    public AtribuicaoResponseDTO update(AtribuicaoCreateDTO atribuicao, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<AtribuicaoEntity> existente = atribuicaoRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Atribuição não encontrada");
        }

        // busca o usuario
        Optional<UsuarioEntity> usuario = usuarioRepository.findByIdAndDeletedAtIsNull(atribuicao.getIdUsuario());
        if(usuario.isEmpty()) {
            throw new NaoEncontradoException("Usuário não encontrado");
        }

        // busca o cargo
        Optional<CargoEntity> cargo = cargoRepository.findById(atribuicao.getIdCargo());
        if(cargo.isEmpty()) {
            throw new NaoEncontradoException("Cargo não encontrado");
        }

        AtribuicaoEntity updated = atribuicaoRepository.save(AtribuicaoEntity.builder()
                .id(id)
                .usuario(usuario.get())
                .cargo(cargo.get())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        AtribuicaoEntity existente = atribuicaoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Atribuição não encontrada"));

        existente.setDeletedAt(LocalDateTime.now());
        atribuicaoRepository.save(existente);
    }

    private AtribuicaoResponseDTO toResponseDTO(AtribuicaoEntity entidade) {
        return AtribuicaoResponseDTO.builder()
                .id(entidade.getId())
                .usuario(entidade.getUsuario())
                .cargo(entidade.getCargo())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
