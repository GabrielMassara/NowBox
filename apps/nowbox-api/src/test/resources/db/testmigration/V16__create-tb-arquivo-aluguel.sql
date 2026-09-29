CREATE TABLE tb_arquivo_aluguel (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    id_arquivo UUID NOT NULL UNIQUE REFERENCES tb_arquivo (id),
    id_aluguel UUID NOT NULL REFERENCES tb_aluguel (id),
    id_box UUID NOT NULL REFERENCES tb_box (id),
    salvo_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_arquivo_aluguel_id_aluguel_salvo_em ON tb_arquivo_aluguel (id_aluguel, salvo_em DESC);
CREATE INDEX idx_arquivo_aluguel_id_box_salvo_em ON tb_arquivo_aluguel (id_box, salvo_em DESC);
