INSERT INTO tb_operacao (nome, codigo, id_modulo)
VALUES (
    'Sincronizar permissões do cargo',
    'MOD_PERMISSAO_OPE_SINCRONIZAR',
    (SELECT id FROM tb_modulo WHERE rota = '/permissoes')
);
