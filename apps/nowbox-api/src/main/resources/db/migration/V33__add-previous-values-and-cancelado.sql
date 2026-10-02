ALTER TABLE tb_aluguel ADD COLUMN id_box_anterior UUID REFERENCES tb_box (id);
ALTER TABLE tb_aluguel ADD COLUMN id_cliente_anterior UUID REFERENCES tb_cliente (id);
ALTER TABLE tb_aluguel ADD COLUMN valor_anterior NUMERIC(10,2);
ALTER TABLE tb_aluguel ADD COLUMN observacao_anterior TEXT;

ALTER TABLE tb_arquivo_aluguel ADD COLUMN cancelado BOOLEAN NOT NULL DEFAULT FALSE;
