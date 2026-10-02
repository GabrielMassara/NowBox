ALTER TABLE tb_arquivo_aluguel ADD COLUMN pendente_assinatura BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tb_arquivo_aluguel ADD COLUMN id_arquivo_assinado UUID UNIQUE REFERENCES tb_arquivo (id);
