import { useState } from "react";
import CadastroLista from "./CadastroLista";
import EmpresaForm from "./EmpresaForm";
import { cargosApi, departamentosApi } from "./services/api";
import "./GestaoFolha.css";
import "./Cadastros.css";

const ABAS = [
  { id: "empresa", rotulo: "Empresa" },
  { id: "departamentos", rotulo: "Departamentos" },
  { id: "cargos", rotulo: "Cargos" },
];

function Cadastros() {
  const [aba, setAba] = useState("empresa");

  return (
    <div className="gestao-folha">
      <header className="gf-header">
        <div>
          <h2>Cadastros</h2>
          <p>Dados da empresa, departamentos e cargos dos funcionários.</p>
        </div>
      </header>

      <div className="cd-abas" role="tablist" aria-label="Cadastros">
        {ABAS.map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={aba === item.id}
            className={`cd-aba ${aba === item.id ? "ativa" : ""}`}
            onClick={() => setAba(item.id)}
          >
            {item.rotulo}
          </button>
        ))}
      </div>

      {aba === "empresa" && <EmpresaForm />}

      {aba === "departamentos" && (
        <CadastroLista
          api={departamentosApi}
          singular="departamento"
          plural="departamentos"
          genero="o"
        />
      )}

      {aba === "cargos" && (
        <CadastroLista
          api={cargosApi}
          singular="cargo"
          plural="cargos"
          genero="o"
        />
      )}
    </div>
  );
}

export default Cadastros;
