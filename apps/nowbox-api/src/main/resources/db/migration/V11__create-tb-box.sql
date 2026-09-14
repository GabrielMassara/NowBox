CREATE TABLE tb_box (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero VARCHAR(20) NOT NULL,
    id_unidade UUID NOT NULL REFERENCES tb_unidade (id),
    tamanho NUMERIC(8,2) NOT NULL,
    dimensoes VARCHAR(100) NOT NULL,
    disponivel BOOLEAN NOT NULL,
    preco NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_box_id_unidade ON tb_box (id_unidade);
