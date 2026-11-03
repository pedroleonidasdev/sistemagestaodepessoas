import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  Pencil,
  Plus,
  Save,
  X,
} from "lucide-react";
import { obterEmpresaId } from "./services/api";
import "./GestaoFolha.css";
import "./Funcionarios.css";

const FORM_VAZIO = { nome: "", descricao: "" };

/**
 * Lista com cadastro, edição e ativação/inativação.
 * Serve para departamentos e cargos, que têm os mesmos campos.
 *
 * Props:
 *  - api: { listar, criar, atualizar, alterarSituacao }
 *  - singular / plural: "departamento" / "departamentos"
 *  - genero: "o" ou "a" (para as mensagens)
 */
function CadastroLista({ api, singular, plural, genero = "o" }) {
  const empresaIdRef = useRef(null);

  const [itens, setItens] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [processando, setProcessando] = useState(false);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");

  // null = fechado | "novo" = cadastro | objeto = edição
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(FORM_VAZIO);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro("");

    try {
      empresaIdRef.current = await obterEmpresaId();
      setItens(await api.listar(empresaIdRef.current));
    } catch (error) {
      setErro(error.message || `Não foi possível carregar os ${plural}.`);
    } finally {
      setCarregando(false);
    }
  }, [api, plural]);

  useEffect(() => {
    const temporizador = setTimeout(carregar, 0);
    return () => clearTimeout(temporizador);
  }, [carregar]);

  const ordenados = useMemo(
    () =>
      [...itens].sort((a, b) =>
        (a.nome || "").localeCompare(b.nome || "", "pt-BR")
      ),
    [itens]
  );

  function abrirNovo() {
    setEditando("novo");
    setForm(FORM_VAZIO);
    setErro("");
    setMensagem("");
  }

  function abrirEdicao(item) {
    setEditando(item);
    setForm({ nome: item.nome ?? "", descricao: item.descricao ?? "" });
    setErro("");
    setMensagem("");
  }

  function fechar() {
    setEditando(null);
    setForm(FORM_VAZIO);
  }

  async function salvar(event) {
    event.preventDefault();
    setErro("");
    setMensagem("");

    const dados = { nome: form.nome.trim(), descricao: form.descricao.trim() };

    if (!dados.nome) {
      setErro(`Informe o nome d${genero} ${singular}.`);
      return;
    }

    setProcessando(true);

    try {
      if (editando === "novo") {
        await api.criar(empresaIdRef.current, dados);
        setMensagem(`${dados.nome} foi cadastrad${genero}.`);
      } else {
        await api.atualizar(empresaIdRef.current, editando.id, dados);
        setMensagem(`${dados.nome} foi atualizad${genero}.`);
      }

      fechar();
      setItens(await api.listar(empresaIdRef.current));
    } catch (error) {
      setErro(error.message || `Não foi possível salvar ${genero} ${singular}.`);
    } finally {
      setProcessando(false);
    }
  }

  async function mudarSituacao(item, ativo) {
    if (ativo === item.ativo) return;

    setProcessando(true);
    setErro("");
    setMensagem("");

    try {
      await api.alterarSituacao(empresaIdRef.current, item.id, ativo);
      setItens((lista) =>
        lista.map((atual) =>
          atual.id === item.id ? { ...atual, ativo } : atual
        )
      );
      setMensagem(
        `${item.nome} ficou ${ativo ? "ativ" : "inativ"}${genero}.`
      );
    } catch (error) {
      setErro(error.message || "Não foi possível alterar a situação.");
    } finally {
      setProcessando(false);
    }
  }

  return (
    <div className="cd-bloco">
      <div className="cd-topo">
        <p className="cd-resumo">
          {carregando
            ? "Carregando..."
            : `${itens.length} ${itens.length === 1 ? singular : plural} cadastrad${genero}${itens.length === 1 ? "" : "s"}.`}
        </p>

        <button
          type="button"
          className="gf-button gf-button-primary"
          onClick={abrirNovo}
          disabled={carregando || processando || editando !== null}
        >
          <Plus size={17} />
          Nov{genero} {singular}
        </button>
      </div>

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
                  ? `Nov${genero} ${singular}`
                  : `Editar ${editando.nome}`}
              </h3>
            </div>

            <button
              type="button"
              className="gf-button gf-button-secondary gf-button-small"
              onClick={fechar}
              disabled={processando}
            >
              <X size={16} />
              Cancelar
            </button>
          </div>

          <form onSubmit={salvar}>
            <div className="gf-form-grid">
              <div className="gf-form-field">
                <label className="gf-label" htmlFor="cd-nome">
                  Nome
                </label>
                <input
                  id="cd-nome"
                  className="gf-input"
                  value={form.nome}
                  onChange={(e) =>
                    setForm((atual) => ({ ...atual, nome: e.target.value }))
                  }
                  maxLength={100}
                  disabled={processando}
                  required
                />
              </div>

              <div className="gf-form-field gf-span-2">
                <label className="gf-label" htmlFor="cd-descricao">
                  Descrição (opcional)
                </label>
                <input
                  id="cd-descricao"
                  className="gf-input"
                  value={form.descricao}
                  onChange={(e) =>
                    setForm((atual) => ({
                      ...atual,
                      descricao: e.target.value,
                    }))
                  }
                  maxLength={500}
                  disabled={processando}
                />
              </div>
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
        <div className="gf-table-wrap">
          <table className="gf-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Descrição</th>
                <th>Situação</th>
                <th>Ações</th>
              </tr>
            </thead>

            <tbody>
              {carregando ? (
                <tr>
                  <td colSpan="4" className="gf-empty">
                    Carregando...
                  </td>
                </tr>
              ) : ordenados.length === 0 ? (
                <tr>
                  <td colSpan="4" className="gf-empty">
                    Nenhum {singular} cadastrado ainda.
                  </td>
                </tr>
              ) : (
                ordenados.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <strong>{item.nome}</strong>
                    </td>
                    <td>{item.descricao || "—"}</td>
                    <td>
                      <select
                        className={`fn-situacao ${item.ativo ? "fn-situacao-ativo" : "fn-situacao-desligado"}`}
                        value={String(item.ativo)}
                        onChange={(e) =>
                          mudarSituacao(item, e.target.value === "true")
                        }
                        disabled={processando}
                        aria-label={`Situação de ${item.nome}`}
                      >
                        <option value="true">Ativo</option>
                        <option value="false">Inativo</option>
                      </select>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="gf-button gf-button-secondary gf-button-small"
                        onClick={() => abrirEdicao(item)}
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

export default CadastroLista;
