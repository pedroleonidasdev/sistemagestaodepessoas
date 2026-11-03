import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  Pencil,
  Plus,
  RefreshCw,
  Save,
  Search,
  X,
} from "lucide-react";
import {
  alterarSituacaoFuncionario,
  atualizarFuncionario,
  cadastrarFuncionario,
  listarFuncionarios,
  obterEmpresaId,
} from "./services/api";
import {
  cpfValido,
  dataBR,
  formatarCpf,
  hojeISO,
  moeda,
  somenteDigitos,
} from "./utils/formatacao";
import "./GestaoFolha.css";
import "./Funcionarios.css";

const SITUACOES = {
  ATIVO: "Ativo",
  AFASTADO: "Afastado",
  DESLIGADO: "Desligado",
};

const FORM_VAZIO = {
  matricula: "",
  nomeCompleto: "",
  cpf: "",
  salarioBase: "",
  dataAdmissao: "",
  situacao: "ATIVO",
};

function Funcionarios() {
  const empresaIdRef = useRef(null);

  const [funcionarios, setFuncionarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [processando, setProcessando] = useState(false);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");

  const [busca, setBusca] = useState("");
  const [filtro, setFiltro] = useState("TODOS");

  // null = formulário fechado | "novo" = cadastro | objeto = edição
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(FORM_VAZIO);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro("");

    try {
      empresaIdRef.current = await obterEmpresaId();
      const dados = await listarFuncionarios(empresaIdRef.current);
      setFuncionarios(Array.isArray(dados) ? dados : []);
    } catch (error) {
      setErro(error.message || "Não foi possível carregar os funcionários.");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    const temporizador = setTimeout(carregar, 0);
    return () => clearTimeout(temporizador);
  }, [carregar]);

  const visiveis = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    const termoCpf = somenteDigitos(busca);

    return funcionarios
      .filter((f) => filtro === "TODOS" || f.situacao === filtro)
      .filter((f) => {
        if (!termo) return true;

        return (
          (f.nomeCompleto || "").toLowerCase().includes(termo) ||
          (f.matricula || "").toLowerCase().includes(termo) ||
          (termoCpf.length > 0 && (f.cpf || "").includes(termoCpf))
        );
      })
      .sort((a, b) =>
        (a.nomeCompleto || "").localeCompare(b.nomeCompleto || "", "pt-BR")
      );
  }, [funcionarios, busca, filtro]);

  const contagem = useMemo(() => {
    const total = { ATIVO: 0, AFASTADO: 0, DESLIGADO: 0 };
    funcionarios.forEach((f) => {
      if (total[f.situacao] !== undefined) total[f.situacao] += 1;
    });
    return total;
  }, [funcionarios]);

  function abrirNovo() {
    setEditando("novo");
    setForm(FORM_VAZIO);
    setErro("");
    setMensagem("");
  }

  function abrirEdicao(funcionario) {
    setEditando(funcionario);
    setForm({
      matricula: funcionario.matricula ?? "",
      nomeCompleto: funcionario.nomeCompleto ?? "",
      cpf: formatarCpf(funcionario.cpf),
      salarioBase: String(funcionario.salarioBase ?? ""),
      dataAdmissao: String(funcionario.dataAdmissao ?? "").slice(0, 10),
      situacao: funcionario.situacao ?? "ATIVO",
    });
    setErro("");
    setMensagem("");
  }

  function fecharFormulario() {
    setEditando(null);
    setForm(FORM_VAZIO);
  }

  function alterarCampo(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }));
  }

  async function salvar(event) {
    event.preventDefault();
    setErro("");
    setMensagem("");

    const dados = {
      matricula: form.matricula.trim(),
      nomeCompleto: form.nomeCompleto.trim(),
      cpf: somenteDigitos(form.cpf),
      salarioBase: String(form.salarioBase).replace(",", "."),
      dataAdmissao: form.dataAdmissao,
      situacao: form.situacao,
    };

    if (!dados.matricula || !dados.nomeCompleto) {
      setErro("Informe a matrícula e o nome completo.");
      return;
    }

    if (!cpfValido(dados.cpf)) {
      setErro("O CPF informado é inválido.");
      return;
    }

    if (
      dados.salarioBase === "" ||
      !Number.isFinite(Number(dados.salarioBase)) ||
      Number(dados.salarioBase) < 0
    ) {
      setErro("Informe um salário base igual ou maior que zero.");
      return;
    }

    if (!dados.dataAdmissao || dados.dataAdmissao > hojeISO()) {
      setErro("Informe uma data de admissão que não seja futura.");
      return;
    }

    setProcessando(true);

    try {
      if (editando === "novo") {
        await cadastrarFuncionario(dados, empresaIdRef.current);
        setMensagem(`${dados.nomeCompleto} foi cadastrado.`);
      } else {
        await atualizarFuncionario(
          editando.id,
          {
            ...dados,
            departamentoId: editando.departamentoId,
            cargoId: editando.cargoId,
          },
          empresaIdRef.current
        );
        setMensagem(`Dados de ${dados.nomeCompleto} atualizados.`);
      }

      fecharFormulario();

      const lista = await listarFuncionarios(empresaIdRef.current);
      setFuncionarios(Array.isArray(lista) ? lista : []);
    } catch (error) {
      setErro(error.message || "Não foi possível salvar o funcionário.");
    } finally {
      setProcessando(false);
    }
  }

  async function mudarSituacao(funcionario, situacao) {
    if (situacao === funcionario.situacao) return;

    if (
      situacao === "DESLIGADO" &&
      !window.confirm(
        `Marcar ${funcionario.nomeCompleto} como desligado? ` +
          "Ele não poderá ser incluído em novas folhas."
      )
    ) {
      return;
    }

    setProcessando(true);
    setErro("");
    setMensagem("");

    try {
      const atualizado = await alterarSituacaoFuncionario(
        funcionario.id,
        situacao,
        empresaIdRef.current
      );

      setFuncionarios((lista) =>
        lista.map((item) =>
          item.id === funcionario.id
            ? { ...item, ...(atualizado ?? {}), situacao }
            : item
        )
      );

      setMensagem(
        `${funcionario.nomeCompleto}: situação alterada para ${SITUACOES[situacao].toLowerCase()}.`
      );
    } catch (error) {
      setErro(error.message || "Não foi possível alterar a situação.");
    } finally {
      setProcessando(false);
    }
  }

  return (
    <div className="gestao-folha">
      <header className="gf-header">
        <div>
          <h2>Funcionários</h2>
          <p>
            {carregando
              ? "Carregando..."
              : `${contagem.ATIVO} ativo(s), ${contagem.AFASTADO} afastado(s) e ${contagem.DESLIGADO} desligado(s).`}
          </p>
        </div>

        <div className="gf-toolbar-actions">
          <button
            type="button"
            className="gf-button gf-button-secondary"
            onClick={carregar}
            disabled={carregando || processando}
          >
            <RefreshCw size={17} />
            Atualizar
          </button>

          <button
            type="button"
            className="gf-button gf-button-primary"
            onClick={abrirNovo}
            disabled={carregando || processando || editando !== null}
          >
            <Plus size={17} />
            Novo funcionário
          </button>
        </div>
      </header>

      {erro && (
        <div className="gf-message gf-error" role="alert">
          <AlertCircle size={19} />
          <span>{erro}</span>
        </div>
      )}

      {mensagem && (
        <div className="gf-message gf-success" role="status">
          <CheckCircle2 size={19} />
          <span>{mensagem}</span>
        </div>
      )}

      {editando !== null && (
        <section className="gf-panel">
          <div className="gf-panel-heading">
            <div>
              <h3>
                {editando === "novo"
                  ? "Novo funcionário"
                  : `Editar ${editando.nomeCompleto}`}
              </h3>
              <p>Todos os campos são obrigatórios.</p>
            </div>

            <button
              type="button"
              className="gf-button gf-button-secondary gf-button-small"
              onClick={fecharFormulario}
              disabled={processando}
            >
              <X size={16} />
              Cancelar
            </button>
          </div>

          <form onSubmit={salvar}>
            <div className="gf-form-grid">
              <div className="gf-form-field">
                <label className="gf-label" htmlFor="fn-matricula">
                  Matrícula
                </label>
                <input
                  id="fn-matricula"
                  className="gf-input"
                  value={form.matricula}
                  onChange={(e) => alterarCampo("matricula", e.target.value)}
                  maxLength={30}
                  disabled={processando}
                  required
                />
              </div>

              <div className="gf-form-field gf-span-2">
                <label className="gf-label" htmlFor="fn-nome">
                  Nome completo
                </label>
                <input
                  id="fn-nome"
                  className="gf-input"
                  value={form.nomeCompleto}
                  onChange={(e) => alterarCampo("nomeCompleto", e.target.value)}
                  maxLength={200}
                  disabled={processando}
                  required
                />
              </div>

              <div className="gf-form-field">
                <label className="gf-label" htmlFor="fn-cpf">
                  CPF
                </label>
                <input
                  id="fn-cpf"
                  className="gf-input"
                  inputMode="numeric"
                  placeholder="000.000.000-00"
                  value={form.cpf}
                  onChange={(e) => alterarCampo("cpf", e.target.value)}
                  maxLength={14}
                  disabled={processando}
                  required
                />
              </div>

              <div className="gf-form-field">
                <label className="gf-label" htmlFor="fn-salario">
                  Salário base (R$)
                </label>
                <input
                  id="fn-salario"
                  className="gf-input"
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.salarioBase}
                  onChange={(e) => alterarCampo("salarioBase", e.target.value)}
                  disabled={processando}
                  required
                />
              </div>

              <div className="gf-form-field">
                <label className="gf-label" htmlFor="fn-admissao">
                  Data de admissão
                </label>
                <input
                  id="fn-admissao"
                  className="gf-input"
                  type="date"
                  max={hojeISO()}
                  value={form.dataAdmissao}
                  onChange={(e) => alterarCampo("dataAdmissao", e.target.value)}
                  disabled={processando}
                  required
                />
              </div>

              {editando !== "novo" && (
                <div className="gf-form-field">
                  <label className="gf-label" htmlFor="fn-situacao">
                    Situação
                  </label>
                  <select
                    id="fn-situacao"
                    className="gf-input"
                    value={form.situacao}
                    onChange={(e) => alterarCampo("situacao", e.target.value)}
                    disabled={processando}
                  >
                    {Object.entries(SITUACOES).map(([valor, rotulo]) => (
                      <option key={valor} value={valor}>
                        {rotulo}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            <button
              type="submit"
              className="gf-button gf-button-primary"
              disabled={processando}
            >
              <Save size={17} />
              {processando ? "Salvando..." : "Salvar"}
            </button>
          </form>
        </section>
      )}

      <section className="gf-panel">
        <div className="fn-filtros">
          <div className="fn-busca">
            <Search size={17} />
            <input
              className="gf-input"
              type="search"
              placeholder="Buscar por nome, matrícula ou CPF"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              aria-label="Buscar funcionário"
            />
          </div>

          <select
            className="gf-input fn-filtro-situacao"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            aria-label="Filtrar por situação"
          >
            <option value="TODOS">Todas as situações</option>
            {Object.entries(SITUACOES).map(([valor, rotulo]) => (
              <option key={valor} value={valor}>
                {rotulo}
              </option>
            ))}
          </select>
        </div>

        <div className="gf-table-wrap">
          <table className="gf-table">
            <thead>
              <tr>
                <th>Matrícula</th>
                <th>Funcionário</th>
                <th>Salário base</th>
                <th>Admissão</th>
                <th>Situação</th>
                <th>Ações</th>
              </tr>
            </thead>

            <tbody>
              {carregando ? (
                <tr>
                  <td colSpan="6" className="gf-empty">
                    Carregando funcionários...
                  </td>
                </tr>
              ) : visiveis.length === 0 ? (
                <tr>
                  <td colSpan="6" className="gf-empty">
                    {funcionarios.length === 0
                      ? "Nenhum funcionário cadastrado. Use o botão Novo funcionário."
                      : "Nenhum funcionário encontrado com esses filtros."}
                  </td>
                </tr>
              ) : (
                visiveis.map((f) => (
                  <tr key={f.id}>
                    <td>{f.matricula}</td>
                    <td>
                      <strong>{f.nomeCompleto}</strong>
                      <span className="gf-subtitle">{formatarCpf(f.cpf)}</span>
                    </td>
                    <td>{moeda(f.salarioBase)}</td>
                    <td>{dataBR(f.dataAdmissao)}</td>
                    <td>
                      <select
                        className={`fn-situacao fn-situacao-${String(f.situacao).toLowerCase()}`}
                        value={f.situacao}
                        onChange={(e) => mudarSituacao(f, e.target.value)}
                        disabled={processando}
                        aria-label={`Situação de ${f.nomeCompleto}`}
                      >
                        {Object.entries(SITUACOES).map(([valor, rotulo]) => (
                          <option key={valor} value={valor}>
                            {rotulo}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="gf-button gf-button-secondary gf-button-small"
                        onClick={() => abrirEdicao(f)}
                        disabled={processando || editando !== null}
                      >
                        <Pencil size={15} />
                        Editar
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default Funcionarios;
