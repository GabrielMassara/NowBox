CREATE TABLE tb_sessao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    rota VARCHAR(100) NOT NULL UNIQUE
);
