CREATE TABLE tb_operacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    id_modulo UUID NOT NULL REFERENCES tb_modulo (id)
);

CREATE INDEX idx_operacao_id_modulo ON tb_operacao (id_modulo);
