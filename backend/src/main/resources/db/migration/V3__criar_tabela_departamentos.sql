
CREATE TABLE departamentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_departamentos PRIMARY KEY (id),

    CONSTRAINT uk_departamentos_empresa_nome
        UNIQUE (empresa_id, nome),

    CONSTRAINT fk_departamentos_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas (id)
);
