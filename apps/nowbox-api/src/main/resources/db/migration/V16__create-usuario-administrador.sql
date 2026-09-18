INSERT INTO tb_unidade (id, nome, cnpj, endereco, numero, bairro, cep, cidade, id_estado)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Administração',
    '00000000000000',
    'Administração',
    'S/N',
    'Administração',
    '30000000',
    'Belo Horizonte',
    (SELECT id FROM tb_estado WHERE uf = 'MG')
);

INSERT INTO tb_cargo (id, nome, id_unidade)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'Administrador',
    'a0000000-0000-0000-0000-000000000001'
);

INSERT INTO tb_usuario (id, nome, email, cpf, sexo, senha)
VALUES (
    'a0000000-0000-0000-0000-000000000003',
    'Administrador',
    'admin@nowbox.com',
    '00000000000',
    'M',
    '$2a$10$VR/xNO114KCGycrAGIjHzuCQtRFpgyCvVNFSDzXUmAsfhp7Xdag0S'
);

INSERT INTO tb_atribuicao (id_usuario, id_cargo)
VALUES (
    'a0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000002'
);

-- Libera todas as operacoes cadastradas ate o momento para o cargo Administrador
INSERT INTO tb_permissao (id_cargo, id_operacao)
SELECT 'a0000000-0000-0000-0000-000000000002', id
FROM tb_operacao;
