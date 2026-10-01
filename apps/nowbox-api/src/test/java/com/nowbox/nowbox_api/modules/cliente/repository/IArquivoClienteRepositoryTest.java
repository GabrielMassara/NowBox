package com.nowbox.nowbox_api.modules.cliente.repository;

import com.nowbox.nowbox_api.modules.cliente.ClienteTestFixtures;
import com.nowbox.nowbox_api.modules.cliente.entity.ArquivoClienteEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IArquivoClienteRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IArquivoClienteRepository arquivoClienteRepository;

    private ClienteEntity cliente(EstadoEntity estado, String cpf) {
        ClienteEntity cliente = ClienteEntity.builder()
                .nome("Cliente " + cpf).profissao("Engenheiro").cpf(cpf).rg("111111111")
                .email(cpf + "@test.com").telefone("11911111111").sexo("M")
                .nascimento(LocalDate.of(1990, 1, 1)).endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado)
                .documentoIdentidade(ClienteTestFixtures.documento(em)).enderecoCorrespondencia(true).senha("senha123").senhaTemporariaStatus(false).build();
        em.persist(cliente);
        return cliente;
    }

    private ArquivoClienteEntity historico(ClienteEntity cliente, LocalDateTime salvoEm) {
        ArquivoClienteEntity historico = ArquivoClienteEntity.builder().arquivo(ClienteTestFixtures.documento(em)).cliente(cliente).salvoEm(salvoEm).build();
        em.persist(historico);
        return historico;
    }

    @Test
    @DisplayName("Return the documentos of a cliente from the newest to the oldest")
    void historicoDoCliente() {
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        em.persist(estado);
        ClienteEntity cliente = cliente(estado, "11111111111");
        ClienteEntity outro = cliente(estado, "22222222222");

        LocalDateTime agora = LocalDateTime.now();
        ArquivoClienteEntity antigo = historico(cliente, agora.minusDays(2));
        ArquivoClienteEntity novo = historico(cliente, agora);
        ArquivoClienteEntity intermediario = historico(cliente, agora.minusDays(1));
        historico(outro, agora);

        Page<ArquivoClienteEntity> result = arquivoClienteRepository.findByClienteIdOrderBySalvoEmDesc(cliente.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(ArquivoClienteEntity::getId).containsExactly(novo.getId(), intermediario.getId(), antigo.getId());
    }

    @Test
    @DisplayName("Not find a documento of the history through another cliente")
    void buscaPorCliente() {
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        em.persist(estado);
        ClienteEntity cliente = cliente(estado, "11111111111");
        ClienteEntity outro = cliente(estado, "22222222222");
        ArquivoClienteEntity historico = historico(cliente, LocalDateTime.now());

        assertThat(arquivoClienteRepository.findByIdAndClienteId(historico.getId(), cliente.getId())).isPresent();
        assertThat(arquivoClienteRepository.findByIdAndClienteId(historico.getId(), outro.getId())).isEmpty();
    }
}
