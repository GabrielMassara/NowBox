package com.nowbox.nowbox_api.modules.contrato.repository;

import com.nowbox.nowbox_api.modules.cliente.ClienteTestFixtures;
import com.nowbox.nowbox_api.modules.aluguel.entity.AluguelEntity;
import com.nowbox.nowbox_api.modules.box.entity.BoxEntity;
import com.nowbox.nowbox_api.modules.cliente.entity.ClienteEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoAluguelEntity;
import com.nowbox.nowbox_api.modules.contrato.entity.ArquivoEntity;
import com.nowbox.nowbox_api.modules.estado.entity.EstadoEntity;
import com.nowbox.nowbox_api.modules.unidade.entity.UnidadeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IArquivoAluguelRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    IArquivoAluguelRepository arquivoAluguelRepository;

    private UnidadeEntity unidade() {
        EstadoEntity estado = EstadoEntity.builder().nome("Estado").uf("XX").build();
        em.persist(estado);
        UnidadeEntity unidade = UnidadeEntity.builder().nome("Unidade").cnpj("11111111111111").endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado).build();
        em.persist(unidade);
        return unidade;
    }

    private ClienteEntity cliente(EstadoEntity estado) {
        ClienteEntity cliente = ClienteEntity.builder()
                .nome("Cliente 1").profissao("Engenheiro").cpf("11111111111").rg("111111111")
                .email("cliente1@test.com").telefone("11911111111").sexo("M")
                .nascimento(LocalDate.of(1990, 1, 1)).endereco("Rua 1").numero("1")
                .bairro("Bairro 1").cep("11111111").cidade("Cidade 1").estado(estado)
                .documentoIdentidade(ClienteTestFixtures.documento(em)).enderecoCorrespondencia(true).senha("senha123").senhaTemporariaStatus(false).build();
        em.persist(cliente);
        return cliente;
    }

    private BoxEntity box(UnidadeEntity unidade, String numero) {
        BoxEntity box = BoxEntity.builder().numero(numero).unidade(unidade).tamanho(BigDecimal.valueOf(10.5)).dimensoes("2x5").disponivel(true).preco(BigDecimal.valueOf(150.00)).build();
        em.persist(box);
        return box;
    }

    private AluguelEntity aluguel(BoxEntity box, ClienteEntity cliente) {
        AluguelEntity aluguel = AluguelEntity.builder().box(box).cliente(cliente).valor(BigDecimal.valueOf(150.00)).status(true).build();
        em.persist(aluguel);
        return aluguel;
    }

    private ArquivoAluguelEntity contrato(AluguelEntity aluguel, BoxEntity box, String chave, LocalDateTime salvoEm) {
        ArquivoEntity arquivo = ArquivoEntity.builder().bucket("nowbox-contratos").chave(chave).nomeOriginal(chave + ".pdf")
                .contentType("application/pdf").tamanho(100L).build();
        em.persist(arquivo);
        ArquivoAluguelEntity vinculo = ArquivoAluguelEntity.builder().arquivo(arquivo).aluguel(aluguel).box(box).salvoEm(salvoEm).build();
        em.persist(vinculo);
        return vinculo;
    }

    @Test
    @DisplayName("Return the aditivos of an aluguel from the newest to the oldest")
    void historicoDoAluguel() {
        UnidadeEntity unidade = unidade();
        BoxEntity box = box(unidade, "101");
        ClienteEntity cliente = cliente(unidade.getEstado());
        AluguelEntity aluguel = aluguel(box, cliente);
        BoxEntity outroBox = box(unidade, "102");
        AluguelEntity outroAluguel = aluguel(outroBox, cliente);

        LocalDateTime agora = LocalDateTime.now();
        contrato(aluguel, box, "antigo", agora.minusDays(2));
        contrato(aluguel, box, "novo", agora);
        contrato(aluguel, box, "intermediario", agora.minusDays(1));
        contrato(outroAluguel, outroBox, "de-outro", agora);

        Page<ArquivoAluguelEntity> historico = arquivoAluguelRepository.findByAluguelIdOrderBySalvoEmDesc(aluguel.getId(), PageRequest.of(0, 10));

        assertThat(historico.getContent()).extracting(c -> c.getArquivo().getChave()).containsExactly("novo", "intermediario", "antigo");
    }
}
