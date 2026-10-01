ALTER TABLE tb_aluguel ADD COLUMN id_arquivo_contrato UUID UNIQUE REFERENCES tb_arquivo (id);

ALTER TABLE tb_arquivo_aluguel ADD COLUMN descricao TEXT;
