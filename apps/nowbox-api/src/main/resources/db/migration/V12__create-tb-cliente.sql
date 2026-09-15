CREATE TABLE tb_cliente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    profissao VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    rg VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    sexo VARCHAR(1) NOT NULL,
    nascimento DATE NOT NULL,
    endereco VARCHAR(200) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    id_estado UUID NOT NULL REFERENCES tb_estado (id),
    endereco_correspondencia BOOLEAN NOT NULL,
    senha VARCHAR(60) NOT NULL,
    senha_temporaria VARCHAR(60),
    senha_temporaria_status BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_cliente_id_estado ON tb_cliente (id_estado);
