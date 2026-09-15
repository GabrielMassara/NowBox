CREATE TABLE tb_aluguel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_box UUID NOT NULL REFERENCES tb_box (id),
    id_cliente UUID NOT NULL REFERENCES tb_cliente (id),
    valor NUMERIC(10,2) NOT NULL,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_aluguel_id_box ON tb_aluguel (id_box);
CREATE INDEX idx_aluguel_id_cliente ON tb_aluguel (id_cliente);
