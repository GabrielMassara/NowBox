CREATE TABLE tb_modulo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_sessao UUID NOT NULL REFERENCES tb_sessao (id),
    nome VARCHAR(100) NOT NULL,
    rota VARCHAR(200) NOT NULL
);

CREATE INDEX idx_modulo_id_sessao ON tb_modulo (id_sessao);
