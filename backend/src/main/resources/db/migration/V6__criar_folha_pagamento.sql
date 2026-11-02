
CREATE TABLE folha_pagamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    competencia CHAR(7) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_folha_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id),

    CONSTRAINT uk_folha_empresa_competencia
        UNIQUE (empresa_id, competencia),

    CONSTRAINT chk_folha_status
        CHECK (status IN ('ABERTA', 'CALCULADA', 'FECHADA'))
);

CREATE TABLE item_folha_pagamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    folha_id BIGINT NOT NULL,
    funcionario_id BIGINT NOT NULL,

    salario_base DECIMAL(12,2) NOT NULL,
    horas_extras DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    adicionais DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    comissoes DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    outros_proventos DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    outros_descontos DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    total_proventos DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    base_inss DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    valor_inss DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    base_irrf DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    valor_irrf DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    valor_fgts DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_descontos DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    salario_liquido DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    PRIMARY KEY (id),

    CONSTRAINT fk_item_folha
        FOREIGN KEY (folha_id)
        REFERENCES folha_pagamento(id),

    CONSTRAINT fk_item_funcionario
        FOREIGN KEY (funcionario_id)
        REFERENCES funcionarios(id),

    CONSTRAINT uk_folha_funcionario
        UNIQUE (folha_id, funcionario_id)
);
