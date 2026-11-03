ALTER TABLE funcionarios
    ADD COLUMN numero_dependentes INT NOT NULL DEFAULT 0,
    ADD COLUMN vale_transporte_mensal DECIMAL(12,2) NOT NULL DEFAULT 0.00;

ALTER TABLE item_folha_pagamento
    ADD COLUMN numero_dependentes INT NOT NULL DEFAULT 0,
    ADD COLUMN vale_transporte DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN dias_faltas INT NOT NULL DEFAULT 0,
    ADD COLUMN desconto_faltas DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN desconto_vale_transporte DECIMAL(12,2) NOT NULL DEFAULT 0.00;
