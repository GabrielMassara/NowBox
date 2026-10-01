CREATE TABLE tb_arquivo_cliente (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    id_arquivo UUID NOT NULL UNIQUE REFERENCES tb_arquivo (id),
    id_cliente UUID NOT NULL REFERENCES tb_cliente (id),
    salvo_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_arquivo_cliente_id_cliente_salvo_em ON tb_arquivo_cliente (id_cliente, salvo_em DESC);
