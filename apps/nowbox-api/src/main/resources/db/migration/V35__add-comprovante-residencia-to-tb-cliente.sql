ALTER TABLE tb_cliente ADD COLUMN id_comprovante_residencia UUID NOT NULL REFERENCES tb_arquivo (id);

CREATE INDEX idx_cliente_id_comprovante_residencia ON tb_cliente (id_comprovante_residencia);

ALTER TABLE tb_arquivo_cliente ADD COLUMN tipo VARCHAR(30) NOT NULL DEFAULT 'IDENTIDADE';

DROP INDEX idx_arquivo_cliente_id_cliente_salvo_em;
CREATE INDEX idx_arquivo_cliente_id_cliente_tipo_salvo_em ON tb_arquivo_cliente (id_cliente, tipo, salvo_em DESC);
