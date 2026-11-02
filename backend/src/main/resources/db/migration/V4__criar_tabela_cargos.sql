
CREATE TABLE cargos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_cargos PRIMARY KEY (id),
    CONSTRAINT uk_cargos_empresa_nome UNIQUE (empresa_id, nome),
    CONSTRAINT fk_cargos_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
);
