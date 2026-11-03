import { useEffect, useState } from "react";
import { ArrowRight, CalendarDays, Users, Wallet } from "lucide-react";
import {
  listarFolha,
  listarFuncionarios,
  listarItensFolha,
  obterEmpresaId,
} from "./services/api";
import { competenciaLegivel, moeda, numero } from "./utils/formatacao";

function Dashboard({ usuario, onNavegar }) {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");
  const [carregando, setCarregando] = useState(true);

  const dataHoje = new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });

  useEffect(() => {
    let ativo = true;

    async function carregar() {
      try {
        const empresaId = await obterEmpresaId();

        const [folhas, funcionarios] = await Promise.all([
          listarFolha(empresaId),
          listarFuncionarios(empresaId),
        ]);

        const listaFolhas = Array.isArray(folhas) ? folhas : [];
        const listaFuncionarios = Array.isArray(funcionarios)
          ? funcionarios
          : [];

        const ultima = listaFolhas[0] ?? null;
        let liquidoUltima = 0;

        if (ultima) {
          const itens = await listarItensFolha(ultima.id, empresaId);

          liquidoUltima = (Array.isArray(itens) ? itens : []).reduce(
            (total, item) => total + numero(item.salarioLiquido),
            0
          );
        }

        if (ativo) {
          setDados({
            ativos: listaFuncionarios.filter((f) => f.situacao === "ATIVO")
              .length,
            emAndamento: listaFolhas.filter((f) => f.status !== "FECHADA")
              .length,
            ultima,
            liquidoUltima,
            totalFuncionarios: listaFuncionarios.length,
          });
        }
      } catch (error) {
        if (ativo) {
          setErro(error.message || "Não foi possível carregar o resumo.");
        }
      } finally {
        if (ativo) {
          setCarregando(false);
        }
      }
    }

    carregar();

    return () => {
      ativo = false;
    };
  }, []);

  const valor = (conteudo) => (carregando ? "…" : conteudo);

  let dica = "";

  if (dados && dados.totalFuncionarios === 0) {
    dica = "Comece cadastrando os funcionários da empresa.";
  } else if (dados && !dados.ultima) {
    dica = "Abra a primeira folha de pagamento para calcular os salários.";
  }

  return (
    <section className="dashboard-content">
      <div className="welcome-card">
        <div className="welcome-text">
          <p className="welcome-date">
            <CalendarDays size={15} />
            {dataHoje}
          </p>

          <h2>Olá, {usuario}. Bem-vindo ao sistema!</h2>

          <p className="welcome-description">
            Gerencie funcionários e folhas de pagamento da sua empresa em um
            só lugar.
          </p>

          <div className="welcome-actions">
            <button
              type="button"
              className="primary-button"
              onClick={() => onNavegar("folha")}
            >
              <Wallet size={18} />
              Acessar folha de pagamento
              <ArrowRight size={16} />
            </button>

            <button
              type="button"
              className="ghost-button"
              onClick={() => onNavegar("funcionarios")}
            >
              <Users size={18} />
              Ver funcionários
            </button>
          </div>
        </div>
      </div>

      {erro && (
        <p className="dash-erro" role="alert">
          {erro}
        </p>
      )}

      {!erro && (
        <div className="dash-grid">
          <article className="dash-card">
            <span className="dash-label">Funcionários ativos</span>
            <strong className="dash-value">{valor(dados?.ativos ?? 0)}</strong>
            <span className="dash-note">Podem entrar nas folhas</span>
          </article>

          <article className="dash-card">
            <span className="dash-label">Folhas em andamento</span>
            <strong className="dash-value">
              {valor(dados?.emAndamento ?? 0)}
            </strong>
            <span className="dash-note">Abertas ou calculadas</span>
          </article>

          <article className="dash-card">
            <span className="dash-label">Última folha</span>
            <strong className="dash-value dash-value-text">
              {valor(
                dados?.ultima
                  ? competenciaLegivel(dados.ultima.competencia)
                  : "Nenhuma"
              )}
            </strong>
            <span className="dash-note">
              {dados?.ultima ? `Situação: ${dados.ultima.status}` : "Sem folhas abertas"}
            </span>
          </article>

          <article className="dash-card">
            <span className="dash-label">Líquido da última folha</span>
            <strong className="dash-value">
              {valor(moeda(dados?.liquidoUltima ?? 0))}
            </strong>
            <span className="dash-note">
              {dados?.ultima?.status === "ABERTA"
                ? "Ainda não calculada"
                : "Soma dos salários líquidos"}
            </span>
          </article>
        </div>
      )}

      {dica && <p className="dash-dica">{dica}</p>}
    </section>
  );
}

export default Dashboard;
