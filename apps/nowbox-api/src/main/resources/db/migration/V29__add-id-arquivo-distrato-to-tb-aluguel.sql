ALTER TABLE tb_aluguel ADD COLUMN id_arquivo_distrato UUID UNIQUE REFERENCES tb_arquivo (id);
