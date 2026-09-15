package com.nowbox.nowbox_api.modules.cliente.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteCreateDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteFilterDTO;
import com.nowbox.nowbox_api.modules.cliente.dto.ClienteResponseDTO;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.repository.IClienteRepository;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.estado.repository.IEstadoRepository;
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
public class ClienteService {

    private final IClienteRepository clienteRepository;
    private final IEstadoRepository estadoRepository;

    public Page<ClienteResponseDTO> listAllByFilter(Pageable pageable, ClienteFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idEstado = null;
        String nome = null;
        String cpf = null;
        String email = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdEstado()))) {
                idEstado = filtro.getIdEstado();
            }
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(filtro.getCpf())) {
                cpf = filtro.getCpf();
            }
            if(StringUtils.hasText(filtro.getEmail())) {
                email = filtro.getEmail();
            }
        }

        return clienteRepository.findAllByFilter(idEstado, nome, cpf, email, pageable).map(this::toResponseDTO);
    }

    public ClienteResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<ClienteEntity> encontrado = clienteRepository.findByIdAndDeletedAtIsNull(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        return toResponseDTO(encontrado.get());
    }

    @Transactional
    public ClienteResponseDTO create(ClienteCreateDTO cliente) throws NaoEncontradoException {

        Optional<EstadoEntity> estado = estadoRepository.findById(cliente.getIdEstado());

        // Se não encontrar o estado
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado inválido");
        }

        ClienteEntity created = clienteRepository.save(ClienteEntity.builder()
                .nome(cliente.getNome())
                .profissao(cliente.getProfissao())
                .cpf(cliente.getCpf())
                .rg(cliente.getRg())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .sexo(cliente.getSexo())
                .nascimento(cliente.getNascimento())
                .endereco(cliente.getEndereco())
                .numero(cliente.getNumero())
                .complemento(cliente.getComplemento())
                .bairro(cliente.getBairro())
                .cep(cliente.getCep())
                .cidade(cliente.getCidade())
                .estado(estado.get())
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .build());

        return toResponseDTO(created);
    }

    @Transactional
    public ClienteResponseDTO update(ClienteCreateDTO cliente, UUID id) throws NaoEncontradoException {
        // verifica se existe o registro
        Optional<ClienteEntity> existente = clienteRepository.findByIdAndDeletedAtIsNull(id);
        if(existente.isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }

        // busca o estado
        Optional<EstadoEntity> estado = estadoRepository.findById(cliente.getIdEstado());
        if(estado.isEmpty()) {
            throw new NaoEncontradoException("Estado não encontrado");
        }

        ClienteEntity updated = clienteRepository.save(ClienteEntity.builder()
                .id(id)
                .nome(cliente.getNome())
                .profissao(cliente.getProfissao())
                .cpf(cliente.getCpf())
                .rg(cliente.getRg())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .sexo(cliente.getSexo())
                .nascimento(cliente.getNascimento())
                .endereco(cliente.getEndereco())
                .numero(cliente.getNumero())
                .complemento(cliente.getComplemento())
                .bairro(cliente.getBairro())
                .cep(cliente.getCep())
                .cidade(cliente.getCidade())
                .estado(estado.get())
                .enderecoCorrespondencia(cliente.getEnderecoCorrespondencia())
                .senha(cliente.getSenha())
                .senhaTemporaria(cliente.getSenhaTemporaria())
                .senhaTemporariaStatus(cliente.getSenhaTemporariaStatus())
                .createdAt(existente.get().getCreatedAt())
                .build());

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id) throws NaoEncontradoException {
        ClienteEntity existente = clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        existente.setDeletedAt(LocalDateTime.now());
        clienteRepository.save(existente);
    }

    private ClienteResponseDTO toResponseDTO(ClienteEntity entidade) {
        return ClienteResponseDTO.builder()
                .id(entidade.getId())
                .estado(entidade.getEstado())
                .nome(entidade.getNome())
                .profissao(entidade.getProfissao())
                .cpf(entidade.getCpf())
                .rg(entidade.getRg())
                .email(entidade.getEmail())
                .telefone(entidade.getTelefone())
                .sexo(entidade.getSexo())
                .nascimento(entidade.getNascimento())
                .endereco(entidade.getEndereco())
                .numero(entidade.getNumero())
                .complemento(entidade.getComplemento())
                .bairro(entidade.getBairro())
                .cep(entidade.getCep())
                .cidade(entidade.getCidade())
                .enderecoCorrespondencia(entidade.getEnderecoCorrespondencia())
                .senhaTemporariaStatus(entidade.getSenhaTemporariaStatus())
                .createdAt(entidade.getCreatedAt())
                .deletedAt(entidade.getDeletedAt())
                .build();
    }
}
