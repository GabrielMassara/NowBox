CREATE TABLE tb_unidade (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    endereco VARCHAR(200) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    id_estado UUID NOT NULL REFERENCES tb_estado (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_unidade_id_estado ON tb_unidade (id_estado);
