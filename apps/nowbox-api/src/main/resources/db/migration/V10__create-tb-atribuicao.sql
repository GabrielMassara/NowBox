CREATE TABLE tb_atribuicao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL REFERENCES tb_usuario (id),
    id_cargo UUID NOT NULL REFERENCES tb_cargo (id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_atribuicao_id_usuario ON tb_atribuicao (id_usuario);
CREATE INDEX idx_atribuicao_id_cargo ON tb_atribuicao (id_cargo);
