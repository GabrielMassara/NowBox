CREATE TABLE tb_permissao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_cargo UUID NOT NULL REFERENCES tb_cargo (id),
    id_operacao UUID NOT NULL REFERENCES tb_operacao (id)
);

CREATE INDEX idx_permissao_id_cargo ON tb_permissao (id_cargo);
CREATE INDEX idx_permissao_id_operacao ON tb_permissao (id_operacao);
