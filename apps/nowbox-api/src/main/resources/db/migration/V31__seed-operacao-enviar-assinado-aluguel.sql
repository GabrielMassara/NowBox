INSERT INTO tb_operacao (nome, codigo, id_modulo) VALUES
    ('Enviar contrato e distrato assinados', 'MOD_ALUGUEL_OPE_ENVIAR_ASSINADO', (SELECT id FROM tb_modulo WHERE rota = '/alugueis'));

INSERT INTO tb_permissao (id_cargo, id_operacao)
SELECT 'a0000000-0000-0000-0000-000000000002', id
FROM tb_operacao
WHERE codigo = 'MOD_ALUGUEL_OPE_ENVIAR_ASSINADO';
