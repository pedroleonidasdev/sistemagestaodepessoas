
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  listarFolha,
  listarFuncionarios,
  listarItensFolha,
  adicionarFuncionarioFolha,
  calcularFolha,
} from "./services/api";
import {
  editarItemFolha,
  excluirItemFolha,
  fecharFolha,
  baixarHolerite,
  baixarRelatorioFolha,
} from "./services/api";

import {
  Users,
  Plus,
  RefreshCw,
  Calculator,
  Wallet,
  CircleDollarSign,
  AlertCircle,
  CheckCircle2,
  Clock3,
  Pencil,
  Trash2,
  FileDown,
  LockKeyhole,
  X,
  Save,
} from "lucide-react";

import "./GestaoFolha.css";

const EMPRESA_ID = 1;

function numero(valor) {
  if (typeof valor === "number") {
    return Number.isFinite(valor) ? valor : 0;
  }

  if (valor == null || valor === "") {
    return 0;
  }

  if (typeof valor === "string") {
    const texto = valor.trim();

    if (texto.includes(",")) {
      return Number(texto.replace(/\./g, "").replace(",", ".")) || 0;
    }

    return Number(texto) || 0;
  }

  return 0;
}

function moeda(valor) {
  return numero(valor).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

function competenciaLegivel(competencia) {
  if (!competencia) return "Sem competência";

  const [ano, mes] = competencia.split("-");
  const data = new Date(Number(ano), Number(mes) - 1, 1);

  return data.toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });
}

function statusFolha(status) {
  return String(status ?? "").toUpperCase();
}

function GestaoFolha() {
  const [folhas, setFolhas] = useState([]);
  const [funcionarios, setFuncionarios] = useState([]);
  const [itens, setItens] = useState([]);

  const [folhaId, setFolhaId] = useState("");
  const [funcionarioId, setFuncionarioId] = useState("");

  const [carregando, setCarregando] = useState(true);
  const [processando, setProcessando] = useState(false);

  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [itemEditando, setItemEditando] = useState(null);
  const [formEdicao, setFormEdicao] = useState({});


  const folhaSelecionada = useMemo(
    () => folhas.find(
      (folha) => String(folha.id) === String(folhaId)
    ),
    [folhas, folhaId]
  );

  const folhaAberta =
    statusFolha(folhaSelecionada?.status) === "ABERTA";

  const carregarFolhas = useCallback(async () => {
    const dados = await listarFolha(EMPRESA_ID);
    return Array.isArray(dados) ? dados : [];
  }, []);

  const carregarFuncionarios = useCallback(async () => {
    const dados = await listarFuncionarios(EMPRESA_ID);
    setFuncionarios(Array.isArray(dados) ? dados : []);
  }, []);

  const carregarItens = useCallback(async (id) => {
    if (!id) {
      setItens([]);
      return;
    }

    const dados = await listarItensFolha(id, EMPRESA_ID);
    setItens(Array.isArray(dados) ? dados : []);
  }, []);

  const atualizarDados = useCallback(async () => {
    setCarregando(true);
    setErro("");
    setMensagem("");

    try {
      const [listaFolhas] = await Promise.all([
        carregarFolhas(),
        carregarFuncionarios(),
      ]);

      setFolhas(listaFolhas);

      const folhaAtual =
        listaFolhas.find(
          (folha) => String(folha.id) === String(folhaId)
        ) ??
        listaFolhas.find(
          (folha) => statusFolha(folha.status) === "ABERTA"
        ) ??
        listaFolhas[0];

      const novoId = String(folhaAtual?.id ?? "");

      setFolhaId(novoId);
      setFuncionarioId("");

      await carregarItens(novoId);
    } catch (error) {
      setErro(
        error.message || "Não foi possível carregar os dados."
      );
    } finally {
      setCarregando(false);
    }
  }, [
    carregarFolhas,
    carregarFuncionarios,
    carregarItens,
    folhaId,
  ]);

  useEffect(() => {
    const temporizador = setTimeout(() => {
      atualizarDados();
    }, 0);

    return () => clearTimeout(temporizador);
  }, [atualizarDados]);

  async function selecionarFolha(novoId) {
    setFolhaId(novoId);
    setFuncionarioId("");
    setMensagem("");
    setErro("");
    setCarregando(true);

    try {
      await carregarItens(novoId);
    } catch (error) {
      setErro(
        error.message || "Não foi possível carregar os itens."
      );
    } finally {
      setCarregando(false);
    }
  }

  async function adicionarFuncionario(event) {
    event.preventDefault();
    setErro("");
    setMensagem("");

    if (!folhaSelecionada || !folhaAberta) {
      setErro(
        "Selecione uma folha aberta para adicionar funcionários."
      );
      return;
    }

    if (!funcionarioId) {
      setErro("Selecione um funcionário.");
      return;
    }

    const jaAdicionado = itens.some(
      (item) =>
        String(item.funcionarioId) === String(funcionarioId)
    );

    if (jaAdicionado) {
      setErro("Esse funcionário já está nesta folha.");
      return;
    }

    setProcessando(true);

    try {
      await adicionarFuncionarioFolha(
        folhaSelecionada.id,
        funcionarioId,
        EMPRESA_ID
      );

      await carregarItens(folhaSelecionada.id);

      setFuncionarioId("");
      setMensagem("Funcionário adicionado à folha com sucesso.");
    } catch (error) {
      setErro(
        error.message || "Não foi possível adicionar o funcionário."
      );
    } finally {
      setProcessando(false);
    }
  }

  async function executarCalculo() {
    setErro("");
    setMensagem("");

    if (!folhaSelecionada || !folhaAberta) {
      setErro("Somente folhas abertas podem ser calculadas.");
      return;
    }

    if (itens.length === 0) {
      setErro("Adicione pelo menos um funcionário antes de calcular.");
      return;
    }

    setProcessando(true);

    try {
      await calcularFolha(folhaSelecionada.id, EMPRESA_ID);

      await carregarItens(folhaSelecionada.id);

      setMensagem("Cálculo concluído. Os valores foram atualizados.");
    } catch (error) {
      setErro(
        error.message || "Não foi possível calcular a folha."
      );
    } finally {
      setProcessando(false);
    }
  }


  function iniciarEdicao(item) {
    setItemEditando(item.id);
    setFormEdicao({
      horasExtras: numero(item.horasExtras),
      adicionais: numero(item.adicionais),
      comissoes: numero(item.comissoes),
      outrosProventos: numero(item.outrosProventos),
      outrosDescontos: numero(item.outrosDescontos),
    });
    setErro("");
    setMensagem("");
  }

  function cancelarEdicao() {
    setItemEditando(null);
    setFormEdicao({});
  }

  async function salvarEdicao(item) {
    if (!folhaSelecionada || !folhaAberta) return;

    for (const valor of Object.values(formEdicao)) {
      if (valor === "" || !Number.isFinite(Number(valor)) || Number(valor) < 0) {
        setErro("Preencha todos os valores com números iguais ou maiores que zero.");
        return;
      }
    }

    setProcessando(true);
    setErro("");
    setMensagem("");

    try {
      await editarItemFolha(
        folhaSelecionada.id,
        item.id,
        Object.fromEntries(
          Object.entries(formEdicao).map(([campo, valor]) => [
            campo,
            Number(valor),
          ])
        ),
        EMPRESA_ID
      );
      await carregarItens(folhaSelecionada.id);
      setItemEditando(null);
      setFormEdicao({});
      setMensagem("Lançamentos salvos. Confira os valores antes de fechar a folha.");
    } catch (error) {
      setErro(error.message || "Não foi possível salvar os lançamentos.");
    } finally {
      setProcessando(false);
    }
  }

  async function removerItem(item) {
    if (!folhaSelecionada || !folhaAberta) return;
    const nome = item.nomeFuncionario || item.funcionarioNome || "este funcionário";
    if (!window.confirm(`Deseja realmente remover ${nome} desta folha?`)) return;

    setProcessando(true);
    setErro("");
    setMensagem("");

    try {
      await excluirItemFolha(folhaSelecionada.id, item.id, EMPRESA_ID);
      await carregarItens(folhaSelecionada.id);
      setMensagem("Funcionário removido da folha.");
    } catch (error) {
      setErro(error.message || "Não foi possível remover o item.");
    } finally {
      setProcessando(false);
    }
  }

  async function executarFechamento() {
    if (!folhaSelecionada || statusFolha(folhaSelecionada.status) !== "CALCULADA") {
      setErro("Calcule a folha antes de fechá-la.");
      return;
    }

    if (!window.confirm("Fechar esta folha? Após o fechamento, os itens não poderão ser alterados.")) return;

    setProcessando(true);
    setErro("");
    setMensagem("");

    try {
      await fecharFolha(folhaSelecionada.id, EMPRESA_ID);
      await atualizarDados();
      setMensagem("Folha fechada com sucesso.");
    } catch (error) {
      setErro(error.message || "Não foi possível fechar a folha.");
    } finally {
      setProcessando(false);
    }
  }

  async function baixarArquivo(acao, descricao) {
    setErro("");
    setMensagem("");
    setProcessando(true);
    try {
      await acao();
      setMensagem(`${descricao}: download solicitado.`);
    } catch (error) {
      setErro(error.message || `Não foi possível baixar ${descricao.toLowerCase()}.`);
    } finally {
      setProcessando(false);
    }
  }

  const totalProventos = itens.reduce(
    (total, item) => total + numero(item.totalProventos),
    0
  );

  const totalDescontos = itens.reduce(
    (total, item) => total + numero(item.totalDescontos),
    0
  );

  const totalLiquido = itens.reduce(
    (total, item) => total + numero(item.salarioLiquido),
    0
  );

  const funcionariosDisponiveis = funcionarios.filter(
    (funcionario) => {
      const status = String(
        funcionario.situacao ?? funcionario.status ?? ""
      ).toUpperCase();

      const ativo = status === "ATIVO";

      const jaAdicionado = itens.some(
        (item) =>
          String(item.funcionarioId) === String(funcionario.id)
      );

      return ativo && !jaAdicionado;
    }
  );

  return (
    <div className="gestao-folhas">
      <header className="gf-header">
        <div>
          <span className="gf-eyebrow">
            DEPARTAMENTO PESSOAL
          </span>

          <h2>Gestão de folha de pagamento</h2>

          <p>
            Selecione uma competência para administrar os pagamentos.
          </p>
        </div>

        <button
          type="button"
          className="gf-button gf-button-secondary"
          onClick={atualizarDados}
          disabled={carregando || processando}
        >
          <RefreshCw size={17} />
          Atualizar
        </button>
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

      <section className="gf-panel">
        <div className="gf-panel-heading">
          <div>
            <h3>Competência</h3>
            <p>Escolha a folha que deseja consultar.</p>
          </div>

          <Wallet size={23} />
        </div>

        <label className="gf-label" htmlFor="gf-folha">
          Folha de pagamento
        </label>

        <select
          id="gf-folha"
          className="gf-input"
          value={folhaId}
          onChange={(event) => selecionarFolha(event.target.value)}
          disabled={carregando || folhas.length === 0}
        >
          {folhas.length === 0 && (
            <option value="">Nenhuma folha encontrada</option>
          )}

          {folhas.map((folha) => (
            <option key={folha.id} value={String(folha.id)}>
              {competenciaLegivel(folha.competencia)} — Folha #
              {folha.id} — {folha.status}
            </option>
          ))}
        </select>

        {folhaSelecionada && (
          <div className="gf-selected-status">
            {folhaAberta ? (
              <Clock3 size={17} />
            ) : (
              <CheckCircle2 size={17} />
            )}

            <span>
              Esta folha está{" "}
              <strong>{folhaSelecionada.status}</strong>.
              {!folhaAberta &&
                " Os comandos de alteração estão desabilitados."}
            </span>
          </div>
        )}
      </section>

      <section className="gf-summary">
        <article className="gf-summary-card">
          <span>Funcionários na folha</span>
          <strong>{carregando ? "…" : itens.length}</strong>
          <Users size={21} />
        </article>

        <article className="gf-summary-card">
          <span>Total de proventos</span>
          <strong>
            {carregando ? "…" : moeda(totalProventos)}
          </strong>
          <CircleDollarSign size={21} />
        </article>

        <article className="gf-summary-card">
          <span>Total de descontos</span>
          <strong>
            {carregando ? "…" : moeda(totalDescontos)}
          </strong>
          <Wallet size={21} />
        </article>

        <article className="gf-summary-card">
          <span>Total líquido</span>
          <strong>
            {carregando ? "…" : moeda(totalLiquido)}
          </strong>
          <CheckCircle2 size={21} />
        </article>
      </section>

      <section className="gf-panel">
        <div className="gf-panel-heading">
          <div>
            <h3>Adicionar funcionário</h3>
            <p>
              Somente funcionários ativos ainda não incluídos nesta folha.
            </p>
          </div>

          <Users size={23} />
        </div>

        <form
          className="gf-add-form"
          onSubmit={adicionarFuncionario}
        >
          <div className="gf-form-field">
            <label className="gf-label" htmlFor="gf-funcionario">
              Funcionário
            </label>

            <select
              id="gf-funcionario"
              className="gf-input"
              value={funcionarioId}
              onChange={(event) =>
                setFuncionarioId(event.target.value)
              }
              disabled={
                carregando ||
                processando ||
                !folhaAberta ||
                funcionariosDisponiveis.length === 0
              }
            >
              <option value="">
                {funcionariosDisponiveis.length > 0
                  ? "Selecione um funcionário..."
                  : "Nenhum funcionário ativo disponível"}
              </option>

              {funcionariosDisponiveis.map((funcionario) => (
                <option
                  key={funcionario.id}
                  value={String(funcionario.id)}
                >
                  {funcionario.nomeCompleto || funcionario.nome} —{" "}
                  {funcionario.matricula || "Sem matrícula"}
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            className="gf-button gf-button-primary"
            disabled={
              processando ||
              carregando ||
              !folhaAberta ||
              !funcionarioId
            }
          >
            <Plus size={18} />
            Adicionar
          </button>
        </form>

        {folhaAberta &&
          funcionariosDisponiveis.length === 0 &&
          !carregando && (
            <p className="gf-help">
              Não há funcionários ativos disponíveis para adicionar.
              Verifique os cadastros ou se todos já estão incluídos
              nesta folha.
            </p>
          )}
      </section>

      <section className="gf-panel">
        <div className="gf-panel-heading gf-items-heading">
          <div>
            <h3>Itens da folha</h3>
            <p>
              {itens.length} funcionário(s) nesta competência.
            </p>
          </div>

          <div className="gf-toolbar-actions">
            {folhaSelecionada && statusFolha(folhaSelecionada.status) === "CALCULADA" && (
              <button
                type="button"
                className="gf-button gf-button-primary"
                onClick={executarFechamento}
                disabled={carregando || processando}
              >
                <LockKeyhole size={17} />
                Fechar folha
              </button>
            )}
            {folhaSelecionada && (
              <button
                type="button"
                className="gf-button gf-button-secondary"
                onClick={() => baixarArquivo(
                  () => baixarRelatorioFolha(folhaSelecionada.id, EMPRESA_ID),
                  "Relatório PDF"
                )}
                disabled={carregando || processando}
              >
                <FileDown size={17} />
                Relatório PDF
              </button>
            )}
            <button
              type="button"
              className="gf-button gf-button-primary"
              onClick={executarCalculo}
            disabled={
              carregando ||
              processando ||
              !folhaAberta ||
              itens.length === 0
            }
          >
            <Calculator size={18} />
            {processando ? "Processando..." : "Calcular folha"}
            </button>
          </div>
        </div>

        <div className="gf-table-wrap">
          <table className="gf-table">
            <thead>
              <tr>
                <th>FUNCIONÁRIO</th>
                <th>SALÁRIO BASE</th>
                <th>PROVENTOS</th>
                <th>DESCONTOS</th>
                <th>LÍQUIDO</th>
                <th>SITUAÇÃO</th>
                <th>AÇÕES</th>
              </tr>
            </thead>

            <tbody>
              {carregando ? (
                <tr>
                  <td colSpan="7" className="gf-empty">
                    Carregando dados...
                  </td>
                </tr>
              ) : itens.length === 0 ? (
                <tr>
                  <td colSpan="7" className="gf-empty">
                    Nenhum funcionário foi adicionado a esta folha.
                  </td>
                </tr>
              ) : (
                itens.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <strong>
                        {item.nomeFuncionario ||
                          item.funcionarioNome ||
                          "—"}
                      </strong>

                      <span className="gf-subtitle">
                        Matrícula: {item.matricula || "—"}
                      </span>
                    </td>

                    <td>{moeda(item.salarioBase)}</td>
                    <td>{moeda(item.totalProventos)}</td>
                    <td>{moeda(item.totalDescontos)}</td>

                    <td className="gf-net-value">
                      {moeda(item.salarioLiquido)}
                    </td>

                    <td>
                      <span className="gf-calculation-status">
                        {numero(item.totalProventos) > 0
                          ? "Valores disponíveis"
                          : "Aguardando cálculo"}
                      </span>
                    </td>
                    <td>
                      <div className="gf-row-actions">
                        {folhaAberta && (
                          <>
                            <button
                              type="button"
                              className="gf-button gf-button-secondary gf-button-small"
                              onClick={() => iniciarEdicao(item)}
                              disabled={processando || itemEditando === item.id}
                              title="Editar lançamentos"
                            >
                              <Pencil size={15} />
                              Editar
                            </button>
                            <button
                              type="button"
                              className="gf-button gf-button-danger gf-button-small"
                              onClick={() => removerItem(item)}
                              disabled={processando || itemEditando === item.id}
                              title="Remover funcionário da folha"
                            >
                              <Trash2 size={15} />
                              Excluir
                            </button>
                          </>
                        )}
                        <button
                          type="button"
                          className="gf-button gf-button-secondary gf-button-small"
                          onClick={() => baixarArquivo(
                            () => baixarHolerite(folhaSelecionada.id, item.id, EMPRESA_ID),
                            "Holerite PDF"
                          )}
                          disabled={processando || !folhaSelecionada}
                          title="Baixar holerite"
                        >
                          <FileDown size={15} />
                          Holerite
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>

            {!carregando && itens.length > 0 && (
              <tfoot>
                <tr>
                  <td colSpan="2">Totais</td>
                  <td>{moeda(totalProventos)}</td>
                  <td>{moeda(totalDescontos)}</td>
                  <td className="gf-net-value">
                    {moeda(totalLiquido)}
                  </td>
                  <td>—</td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>


      {itemEditando !== null && (
        <section className="gf-panel gf-edit-panel">
          <div className="gf-panel-heading">
            <div>
              <h3>Editar lançamentos</h3>
              <p>Informe valores não negativos. O backend recalcula os totais.</p>
            </div>
            <button
              type="button"
              className="gf-button gf-button-secondary"
              onClick={cancelarEdicao}
              disabled={processando}
            >
              <X size={17} />
              Cancelar
            </button>
          </div>
          <div className="gf-edit-grid">
            {[
              ["horasExtras", "Horas extras (R$)"],
              ["adicionais", "Adicionais (R$)"],
              ["comissoes", "Comissões (R$)"],
              ["outrosProventos", "Outros proventos (R$)"],
              ["outrosDescontos", "Outros descontos (R$)"],
            ].map(([campo, rotulo]) => (
              <label className="gf-edit-field" key={campo}>
                <span className="gf-label">{rotulo}</span>
                <input
                  className="gf-input"
                  type="number"
                  min="0"
                  step="0.01"
                  value={formEdicao[campo] ?? ""}
                  onChange={(event) => setFormEdicao((atual) => ({
                    ...atual,
                    [campo]: event.target.value,
                  }))}
                  disabled={processando}
                />
              </label>
            ))}
          </div>
          <button
            type="button"
            className="gf-button gf-button-primary"
            onClick={() => salvarEdicao(itens.find((item) => item.id === itemEditando))}
            disabled={processando}
          >
            <Save size={17} />
            {processando ? "Salvando..." : "Salvar lançamentos"}
          </button>
        </section>
      )}

        {!folhaAberta && folhaSelecionada && (
          <p className="gf-help">
            Esta competência está fechada. A consulta permanece
            disponível, mas não é possível adicionar funcionários
            nem recalcular a folha por esta tela.
          </p>
        )}
      </section>
    </div>
  );
}

export default GestaoFolha;
