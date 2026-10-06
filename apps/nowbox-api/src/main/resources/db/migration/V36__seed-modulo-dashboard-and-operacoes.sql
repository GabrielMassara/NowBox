INSERT INTO tb_modulo (nome, rota, id_sessao) VALUES
    ('Dashboard', '/', (SELECT id FROM tb_sessao WHERE rota = '/negocio'));

INSERT INTO tb_operacao (nome, codigo, id_modulo) VALUES
    ('Consultar resumo do dashboard', 'MOD_DASHBOARD_OPE_RESUMO', (SELECT id FROM tb_modulo WHERE rota = '/')),
    ('Consultar ocupação dos boxes no dashboard', 'MOD_DASHBOARD_OPE_OCUPACAO', (SELECT id FROM tb_modulo WHERE rota = '/')),
    ('Consultar aluguéis por situação no dashboard', 'MOD_DASHBOARD_OPE_ALUGUEIS_STATUS', (SELECT id FROM tb_modulo WHERE rota = '/')),
    ('Consultar evolução dos aluguéis no dashboard', 'MOD_DASHBOARD_OPE_EVOLUCAO', (SELECT id FROM tb_modulo WHERE rota = '/')),
    ('Consultar pendências de assinatura no dashboard', 'MOD_DASHBOARD_OPE_PENDENCIAS', (SELECT id FROM tb_modulo WHERE rota = '/'));
