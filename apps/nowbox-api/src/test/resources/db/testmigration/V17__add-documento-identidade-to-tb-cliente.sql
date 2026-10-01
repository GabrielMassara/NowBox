ALTER TABLE tb_cliente ADD COLUMN id_documento_identidade UUID NOT NULL REFERENCES tb_arquivo (id);

CREATE INDEX idx_cliente_id_documento_identidade ON tb_cliente (id_documento_identidade);
