package com.nowbox.nowbox_api.modules.cargo.service;

import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoCreateDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoFilterDTO;
import com.nowbox.nowbox_api.modules.cargo.dto.CargoResponseDTO;
import com.nowbox.nowbox_api.modules.cargo.entity.CargoEntity;
import com.nowbox.nowbox_api.modules.cargo.repository.ICargoRepository;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import com.nowbox.nowbox_api.modules.unidade.repository.IUnidadeRepository;
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
public class CargoService {

    private final ICargoRepository cargoRepository;
    private final IUnidadeRepository unidadeRepository;

    public Page<CargoResponseDTO> listAllByFilter(Pageable pageable, CargoFilterDTO filtro) {

        // Verificacao dos parametros passados para filtro
        String nome = null;
        UUID idUnidade = null;

        if(filtro != null) {
            if(StringUtils.hasText(filtro.getNome())) {
                nome = filtro.getNome();
            }
            if(StringUtils.hasText(String.valueOf(filtro.getIdUnidade()))) {
                idUnidade = filtro.getIdUnidade();
            }
        }

        return cargoRepository.findAllByFilter(idUnidade, nome, pageable).map(e -> CargoResponseDTO.builder().id(e.getId()).nome(e.getNome()).unidade(e.getUnidade()).build());
    }

    public CargoResponseDTO find(Pageable page, UUID id) throws NaoEncontradoException {
        Optional<CargoEntity> encontrado = cargoRepository.findById(id);

        if(encontrado.isEmpty()) {
            throw new NaoEncontradoException("Cargo não encontrado");
        }

        return CargoResponseDTO.builder().id(encontrado.get().getId()).nome(encontrado.get().getNome()).unidade(encontrado.get().getUnidade()).build();
    }

    @Transactional
    public CargoResponseDTO create(CargoCreateDTO cargo) throws NaoEncontradoException {

        Optional<UnidadeEntity> unidade = unidadeRepository.findById(cargo.getIdUnidade());

        // Se não encontrar a unidade
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade inválida");
        }

        CargoEntity created = cargoRepository.save(CargoEntity.builder().nome(cargo.getNome()).unidade(unidade.get()).build());

        return CargoResponseDTO.builder().id(created.getId()).nome(created.getNome()).unidade(created.getUnidade()).build();
    }

    @Transactional
    public CargoResponseDTO update(CargoCreateDTO cargo, UUID id) throws NaoEncontradoException {
        //verifica se existe o registro
        if(cargoRepository.findById(id).isEmpty()) {
            throw new NaoEncontradoException("Cargo não encontrado");
        }

        // busca a unidade
        Optional<UnidadeEntity> unidade = unidadeRepository.findById(cargo.getIdUnidade());
        if(unidade.isEmpty()) {
            throw new NaoEncontradoException("Unidade não encontrada");
        }

        CargoEntity updated = cargoRepository.save(CargoEntity.builder().id(id).nome(cargo.getNome()).unidade(unidade.get()).build());

        return CargoResponseDTO.builder().id(updated.getId()).nome(updated.getNome()).unidade(updated.getUnidade()).build();
    }

    public void delete(UUID id) {
        cargoRepository.deleteById(id);
    }
}
