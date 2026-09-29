INSERT INTO tb_operacao (nome, codigo, id_modulo) VALUES
    ('Consultar contratos', 'MOD_ALUGUEL_OPE_CONSULTAR_CONTRATO', (SELECT id FROM tb_modulo WHERE rota = '/alugueis')),
    ('Baixar contrato', 'MOD_ALUGUEL_OPE_BAIXAR_CONTRATO', (SELECT id FROM tb_modulo WHERE rota = '/alugueis'));
