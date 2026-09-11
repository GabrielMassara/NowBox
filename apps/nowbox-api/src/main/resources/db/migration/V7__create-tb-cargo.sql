CREATE TABLE tb_cargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    id_unidade UUID NOT NULL REFERENCES tb_unidade (id)
);

CREATE INDEX idx_cargo_id_unidade ON tb_cargo (id_unidade);
