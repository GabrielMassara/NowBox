-- Libera a operacao de cadastrar boxes em lote para o cargo Administrador (usuario administrador padrao)
INSERT INTO tb_permissao (id_cargo, id_operacao)
SELECT 'a0000000-0000-0000-0000-000000000002', id
FROM tb_operacao
WHERE codigo = 'MOD_BOX_OPE_CADASTRAR_LOTE';
