
CREATE TABLE funcionarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    matricula VARCHAR(30) NOT NULL,
    nome_completo VARCHAR(200) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    data_admissao DATE NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_funcionarios PRIMARY KEY (id),

    CONSTRAINT uk_funcionarios_empresa_matricula
        UNIQUE (empresa_id, matricula),

    CONSTRAINT uk_funcionarios_empresa_cpf
        UNIQUE (empresa_id, cpf),

    CONSTRAINT fk_funcionarios_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id),

    CONSTRAINT ck_funcionarios_situacao
        CHECK (situacao IN ('ATIVO', 'AFASTADO', 'DESLIGADO'))
);