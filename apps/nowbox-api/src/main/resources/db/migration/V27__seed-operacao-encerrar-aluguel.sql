INSERT INTO tb_operacao (nome, codigo, id_modulo) VALUES
    ('Encerrar aluguel', 'MOD_ALUGUEL_OPE_ENCERRAR', (SELECT id FROM tb_modulo WHERE rota = '/alugueis'));
