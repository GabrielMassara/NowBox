package com.nowbox.nowbox_api.modules.usuario.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioCreateDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioFilterDTO;
import com.nowbox.nowbox_api.modules.usuario.dto.UsuarioResponseDTO;
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
public class UsuarioService {

    private final IUsuarioRepository usuarioRepository;

    public Page<UsuarioResponseDTO> listAllByFilter(Pageable pageable, UsuarioFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        String email = null;
        String cpf = null;

        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getEmail())) {
                email = filtro.getEmail();
            }
            if(StringUtils.hasText(filtro.getCpf())) {
                cpf = filtro.getCpf();
            }
        }

        return usuarioRepository.findAllByFilter(nome, email, cpf, pageable).map(this::toResponseDTO);
    }

    public UsuarioResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<UsuarioEntity> encontrado = usuarioRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Usuário não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioCreateDTO usuario) {
        UsuarioEntity created = usuarioRepository.save(UsuarioEntity.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .sexo(usuario.getSexo())
                .senha(usuario.getSenha())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public UsuarioResponseDTO update(UsuarioCreateDTO usuario, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<UsuarioEntity> existente = usuarioRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Usuário não encontrado");
        }

        UsuarioEntity updated = usuarioRepository.save(UsuarioEntity.builder()
                .id(id)
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .sexo(usuario.getSexo())
                .senha(usuario.getSenha())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        UsuarioEntity existente = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        usuarioRepository.save(existente);
    }

    private UsuarioResponseDTO toResponseDTO(UsuarioEntity entidade) {
        return UsuarioResponseDTO.builder()
                .id(entidade.getId())
                .nome(entidade.getNome())
                .email(entidade.getEmail())
                .cpf(entidade.getCpf())
                .sexo(entidade.getSexo())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
