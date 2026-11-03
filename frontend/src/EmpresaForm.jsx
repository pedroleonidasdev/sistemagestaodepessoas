import { useCallback, useEffect, useRef, useState } from "react";
import { AlertCircle, CheckCircle2, Save } from "lucide-react";
import { listarEmpresas, salvarEmpresa } from "./services/api";
import { cnpjValido, formatarCnpj, somenteDigitos } from "./utils/formatacao";
import "./GestaoFolha.css";

const FORM_VAZIO = { razaoSocial: "", nomeFantasia: "", cnpj: "" };

function EmpresaForm() {
  const empresaRef = useRef(null);

  const [form, setForm] = useState(FORM_VAZIO);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [existe, setExiste] = useState(false);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro("");

    try {
      const lista = await listarEmpresas();
      const empresa = lista.find((item) => item.ativo !== false) ?? lista[0];

      empresaRef.current = empresa ?? null;
      setExiste(Boolean(empresa));

      setForm(
        empresa
          ? {
              razaoSocial: empresa.razaoSocial ?? "",
              nomeFantasia: empresa.nomeFantasia ?? "",
              cnpj: formatarCnpj(empresa.cnpj),
            }
          : FORM_VAZIO
      );
    } catch (error) {
      setErro(error.message || "Não foi possível carregar a empresa.");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    const temporizador = setTimeout(carregar, 0);
    return () => clearTimeout(temporizador);
  }, [carregar]);

  function alterar(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }));
  }

  async function salvar(event) {
    event.preventDefault();
    setErro("");
    setMensagem("");

    const dados = {
      razaoSocial: form.razaoSocial.trim(),
      nomeFantasia: form.nomeFantasia.trim(),
      cnpj: somenteDigitos(form.cnpj),
    };

    if (!dados.razaoSocial) {
      setErro("Informe a razão social.");
      return;
    }

    if (!cnpjValido(dados.cnpj)) {
      setErro("O CNPJ informado é inválido.");
      return;
    }

    setSalvando(true);

    try {
      const salva = await salvarEmpresa(dados, empresaRef.current?.id);
      empresaRef.current = salva;
      setExiste(true);
      setForm({
        razaoSocial: salva.razaoSocial ?? dados.razaoSocial,
        nomeFantasia: salva.nomeFantasia ?? "",
        cnpj: formatarCnpj(salva.cnpj ?? dados.cnpj),
      });
      setMensagem(
        existe ? "Dados da empresa atualizados." : "Empresa cadastrada."
      );
    } catch (error) {
      setErro(error.message || "Não foi possível salvar a empresa.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <section className="gf-panel">
      <div className="gf-panel-heading">
        <div>
          <h3>Dados da empresa</h3>
          <p>
            {existe
              ? "Estes dados aparecem nos holerites e nos relatórios."
              : "Nenhuma empresa cadastrada ainda. Preencha os dados abaixo."}
          </p>
        </div>
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

      <form onSubmit={salvar}>
        <div className="gf-form-grid">
          <div className="gf-form-field gf-span-2">
            <label className="gf-label" htmlFor="em-razao">
              Razão social
            </label>
            <input
              id="em-razao"
              className="gf-input"
              value={form.razaoSocial}
              onChange={(e) => alterar("razaoSocial", e.target.value)}
              maxLength={200}
              disabled={carregando || salvando}
              required
            />
          </div>

          <div className="gf-form-field">
            <label className="gf-label" htmlFor="em-cnpj">
              CNPJ
            </label>
            <input
              id="em-cnpj"
              className="gf-input"
              inputMode="numeric"
              placeholder="00.000.000/0000-00"
              value={form.cnpj}
              onChange={(e) => alterar("cnpj", e.target.value)}
              maxLength={18}
              disabled={carregando || salvando}
              required
            />
          </div>

          <div className="gf-form-field gf-span-2">
            <label className="gf-label" htmlFor="em-fantasia">
              Nome fantasia (opcional)
            </label>
            <input
              id="em-fantasia"
              className="gf-input"
              value={form.nomeFantasia}
              onChange={(e) => alterar("nomeFantasia", e.target.value)}
              maxLength={200}
              disabled={carregando || salvando}
            />
          </div>
        </div>

        <button
          type="submit"
          className="gf-button gf-button-primary"
          disabled={carregando || salvando}
        >
          <Save size={17} />
          {salvando
            ? "Salvando..."
            : existe
              ? "Salvar alterações"
              : "Cadastrar empresa"}
        </button>
      </form>
    </section>
  );
}

export default EmpresaForm;
