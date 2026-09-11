package com.nowbox.nowbox_api.modules.permissao.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.operacao.entity.OperacaoEntity;
import com.nowbox.nowbox_api.modules.operacao.repository.IOperacaoRepository;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoCreateDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoFilterDTO;
import com.nowbox.nowbox_api.modules.permissao.dto.PermissaoResponseDTO;
import com.nowbox.nowbox_api.modules.permissao.entity.PermissaoEntity;
import com.nowbox.nowbox_api.modules.permissao.repository.IPermissaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissaoService {

    private final IPermissaoRepository permissaoRepository;
    private final ICargoRepository cargoRepository;
    private final IOperacaoRepository operacaoRepository;

    public Page<PermissaoResponseDTO> listAllByFilter(Pageable pageable, PermissaoFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        UUID idCargo = null;
        UUID idOperacao = null;

        if(filtro != null) {
            if(StringUtils.hasText(String.valueOf(filtro.getIdCargo()))) {
                idCargo = filtro.getIdCargo();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdOperacao()))) {
                idOperacao = filtro.getIdOperacao();
            }
        }

        return permissaoRepository.findAllByFilter(idCargo, idOperacao, pageable).map(e -> PermissaoResponseDTO.builder().id(e.getId()).cargo(e.getCargo()).operacao(e.getOperacao()).build());
    }

    public PermissaoResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<PermissaoEntity> encontrado = permissaoRepository.findById(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Permissão não encontrada");
        }

        return PermissaoResponseDTO.builder().id(encontrado.get().getId()).cargo(encontrado.get().getCargo()).operacao(encontrado.get().getOperacao()).build();
    }

    @Transactional
    public PermissaoResponseDTO create(PermissaoCreateDTO permissao) throws NaoEncontradoException {

        Optional<CargoEntity> cargo = cargoRepository.findById(permissao.getIdCargo());

        // Se não encontrar o cargo
        if(cargo.isEmpty()) {
            throw new NaoEncontradoException("Cargo inválido");
        }

        Optional<OperacaoEntity> operacao = operacaoRepository.findById(permissao.getIdOperacao());

        // Se não encontrar a operacao
        if(operacao.isEmpty()) {
            throw new NaoEncontradoException("Operação inválida");
        }

        PermissaoEntity created = permissaoRepository.save(PermissaoEntity.builder().cargo(cargo.get()).operacao(operacao.get()).build());

        return PermissaoResponseDTO.builder().id(created.getId()).cargo(created.getCargo()).operacao(created.getOperacao()).build();
    }

    @Transactional
    public PermissaoResponseDTO update(PermissaoCreateDTO permissao, UUID id) throws NaoEncontradoException {
        //verifica se existe o registro
        if(permissaoRepository.findById(id).isEmpty()) {
            throw new NaoEncontradoException("Permissão não encontrada");
        }

        // busca o cargo
        Optional<CargoEntity> cargo = cargoRepository.findById(permissao.getIdCargo());
        if(cargo.isEmpty()) {
            throw new NaoEncontradoException("Cargo não encontrado");
        }

        // busca a operacao
        Optional<OperacaoEntity> operacao = operacaoRepository.findById(permissao.getIdOperacao());
        if(operacao.isEmpty()) {
            throw new NaoEncontradoException("Operação não encontrada");
        }

        PermissaoEntity updated = permissaoRepository.save(PermissaoEntity.builder().id(id).cargo(cargo.get()).operacao(operacao.get()).build());

        return PermissaoResponseDTO.builder().id(updated.getId()).cargo(updated.getCargo()).operacao(updated.getOperacao()).build();
    }

    public void delete(UUID id) {
        permissaoRepository.deleteById(id);
    }
}
