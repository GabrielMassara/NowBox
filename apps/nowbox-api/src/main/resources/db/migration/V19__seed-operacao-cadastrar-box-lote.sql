INSERT INTO tb_operacao (nome, codigo, id_modulo)
VALUES (
    'Cadastrar boxes em lote',
    'MOD_BOX_OPE_CADASTRAR_LOTE',
    (SELECT id FROM tb_modulo WHERE rota = '/boxes')
);
