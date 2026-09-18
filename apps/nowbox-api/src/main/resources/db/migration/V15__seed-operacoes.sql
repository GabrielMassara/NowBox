INSERT INTO tb_sessao (nome, rota) VALUES
    ('Administração', '/administracao'),
    ('Gerência', '/gerencia'),
    ('Negócio', '/negocio');

INSERT INTO tb_modulo (nome, rota, id_sessao) VALUES
    ('Módulos', '/modulos', (SELECT id FROM tb_sessao WHERE rota = '/administracao')),
    ('Operações', '/operacoes-sistema', (SELECT id FROM tb_sessao WHERE rota = '/administracao')),
    ('Estados', '/estados', (SELECT id FROM tb_sessao WHERE rota = '/administracao')),
    ('Sessões', '/sessoes', (SELECT id FROM tb_sessao WHERE rota = '/administracao')),

    ('Cargos', '/cargos', (SELECT id FROM tb_sessao WHERE rota = '/gerencia')),
    ('Permissões', '/permissoes', (SELECT id FROM tb_sessao WHERE rota = '/gerencia')),
    ('Atribuições', '/atribuicoes', (SELECT id FROM tb_sessao WHERE rota = '/gerencia')),
    ('Usuários', '/usuarios', (SELECT id FROM tb_sessao WHERE rota = '/gerencia')),
    ('Unidades', '/unidades', (SELECT id FROM tb_sessao WHERE rota = '/gerencia')),

    ('Boxes', '/boxes', (SELECT id FROM tb_sessao WHERE rota = '/negocio')),
    ('Aluguéis', '/alugueis', (SELECT id FROM tb_sessao WHERE rota = '/negocio')),
    ('Clientes', '/clientes', (SELECT id FROM tb_sessao WHERE rota = '/negocio'));

INSERT INTO tb_operacao (nome, codigo, id_modulo) VALUES
    ('Consultar módulos', 'MOD_MODULO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/modulos')),
    ('Cadastrar módulo', 'MOD_MODULO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/modulos')),
    ('Atualizar módulo', 'MOD_MODULO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/modulos')),
    ('Excluir módulo', 'MOD_MODULO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/modulos')),

    ('Consultar operações', 'MOD_OPERACAO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/operacoes-sistema')),
    ('Cadastrar operação', 'MOD_OPERACAO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/operacoes-sistema')),
    ('Atualizar operação', 'MOD_OPERACAO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/operacoes-sistema')),
    ('Excluir operação', 'MOD_OPERACAO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/operacoes-sistema')),

    ('Consultar estados', 'MOD_ESTADO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/estados')),

    ('Consultar sessões', 'MOD_SESSAO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/sessoes')),
    ('Cadastrar sessão', 'MOD_SESSAO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/sessoes')),
    ('Atualizar sessão', 'MOD_SESSAO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/sessoes')),
    ('Excluir sessão', 'MOD_SESSAO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/sessoes')),

    ('Consultar cargos', 'MOD_CARGO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/cargos')),
    ('Cadastrar cargo', 'MOD_CARGO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/cargos')),
    ('Atualizar cargo', 'MOD_CARGO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/cargos')),
    ('Excluir cargo', 'MOD_CARGO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/cargos')),

    ('Consultar permissões', 'MOD_PERMISSAO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/permissoes')),
    ('Cadastrar permissão', 'MOD_PERMISSAO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/permissoes')),
    ('Atualizar permissão', 'MOD_PERMISSAO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/permissoes')),
    ('Excluir permissão', 'MOD_PERMISSAO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/permissoes')),

    ('Consultar atribuições', 'MOD_ATRIBUICAO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/atribuicoes')),
    ('Cadastrar atribuição', 'MOD_ATRIBUICAO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/atribuicoes')),
    ('Atualizar atribuição', 'MOD_ATRIBUICAO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/atribuicoes')),
    ('Excluir atribuição', 'MOD_ATRIBUICAO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/atribuicoes')),

    ('Consultar usuários', 'MOD_USUARIO_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/usuarios')),
    ('Cadastrar usuário', 'MOD_USUARIO_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/usuarios')),
    ('Atualizar usuário', 'MOD_USUARIO_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/usuarios')),
    ('Excluir usuário', 'MOD_USUARIO_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/usuarios')),

    ('Consultar unidades', 'MOD_UNIDADE_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/unidades')),
    ('Cadastrar unidade', 'MOD_UNIDADE_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/unidades')),
    ('Atualizar unidade', 'MOD_UNIDADE_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/unidades')),
    ('Excluir unidade', 'MOD_UNIDADE_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/unidades')),

    ('Consultar boxes', 'MOD_BOX_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/boxes')),
    ('Cadastrar box', 'MOD_BOX_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/boxes')),
    ('Atualizar box', 'MOD_BOX_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/boxes')),
    ('Excluir box', 'MOD_BOX_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/boxes')),

    ('Consultar aluguéis', 'MOD_ALUGUEL_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/alugueis')),
    ('Cadastrar aluguel', 'MOD_ALUGUEL_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/alugueis')),
    ('Atualizar aluguel', 'MOD_ALUGUEL_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/alugueis')),
    ('Excluir aluguel', 'MOD_ALUGUEL_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/alugueis')),

    ('Consultar clientes', 'MOD_CLIENTE_OPE_CONSULTAR', (SELECT id FROM tb_modulo WHERE rota = '/clientes')),
    ('Cadastrar cliente', 'MOD_CLIENTE_OPE_CADASTRAR', (SELECT id FROM tb_modulo WHERE rota = '/clientes')),
    ('Atualizar cliente', 'MOD_CLIENTE_OPE_ATUALIZAR', (SELECT id FROM tb_modulo WHERE rota = '/clientes')),
    ('Excluir cliente', 'MOD_CLIENTE_OPE_EXCLUIR', (SELECT id FROM tb_modulo WHERE rota = '/clientes'));
