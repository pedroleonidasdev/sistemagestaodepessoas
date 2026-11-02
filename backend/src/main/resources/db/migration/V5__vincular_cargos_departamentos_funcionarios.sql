
ALTER TABLE funcionarios
    ADD COLUMN departamento_id BIGINT NULL,
    ADD COLUMN cargo_id BIGINT NULL;

ALTER TABLE funcionarios
    ADD CONSTRAINT fk_funcionarios_departamento
        FOREIGN KEY (departamento_id)
        REFERENCES departamentos (id);

ALTER TABLE funcionarios
    ADD CONSTRAINT fk_funcionarios_cargo
        FOREIGN KEY (cargo_id)
        REFERENCES cargos (id);

CREATE INDEX idx_funcionarios_departamento
    ON funcionarios (departamento_id);

CREATE INDEX idx_funcionarios_cargo
    ON funcionarios (cargo_id);
