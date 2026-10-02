ALTER TABLE tb_aluguel ADD COLUMN id_arquivo_contrato_assinado UUID UNIQUE REFERENCES tb_arquivo (id);
ALTER TABLE tb_aluguel ADD COLUMN id_arquivo_distrato_assinado UUID UNIQUE REFERENCES tb_arquivo (id);
