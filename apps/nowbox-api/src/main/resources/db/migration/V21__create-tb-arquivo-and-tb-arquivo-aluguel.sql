CREATE TABLE tb_arquivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bucket VARCHAR(100) NOT NULL,
    chave VARCHAR(500) NOT NULL UNIQUE,
    nome_original VARCHAR(200) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tb_arquivo_aluguel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_arquivo UUID NOT NULL UNIQUE REFERENCES tb_arquivo (id),
    id_aluguel UUID NOT NULL REFERENCES tb_aluguel (id),
    id_box UUID NOT NULL REFERENCES tb_box (id),
    salvo_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_arquivo_aluguel_id_aluguel_salvo_em ON tb_arquivo_aluguel (id_aluguel, salvo_em DESC);
CREATE INDEX idx_arquivo_aluguel_id_box_salvo_em ON tb_arquivo_aluguel (id_box, salvo_em DESC);
