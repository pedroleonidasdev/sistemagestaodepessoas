import { useEffect, useState } from "react";
import {
  LayoutDashboard,
  Wallet,
  LogOut,
  LockKeyhole,
  User,
  LoaderCircle,
} from "lucide-react";
import GestaoFolha from "./GestaoFolha";
import { login, obterSessao, logout } from "./services/api";
import "./App.css";

function TelaLogin({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [erro, setErro] = useState("");
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setErro("");

    if (!username.trim() || !password) {
      setErro("Informe o usuÃ¡rio e a senha.");
      return;
    }

    try {
      setCarregando(true);
      await login(username.trim(), password);
      await onLogin();
    } catch (error) {
      setErro(error.message || "NÃ£o foi possÃ­vel entrar no sistema.");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <div className="login-brand-icon">
          <Wallet size={30} />
        </div>

        <div className="login-heading">
          <span>GESTÃƒO DP</span>
          <h1>Bem-vindo de volta</h1>
          <p>Entre com suas credenciais para acessar o sistema.</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="login-username">UsuÃ¡rio</label>
          <div className="login-input-wrap">
            <User size={18} />
            <input
              id="login-username"
              type="text"
              autoComplete="username"
              placeholder="Seu usuÃ¡rio"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              disabled={carregando}
              required
            />
          </div>

          <label htmlFor="login-password">Senha</label>
          <div className="login-input-wrap">
            <LockKeyhole size={18} />
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              placeholder="Sua senha"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              disabled={carregando}
              required
            />
          </div>

          {erro && <div className="login-error" role="alert">{erro}</div>}

          <button
            className="login-submit"
            type="submit"
            disabled={carregando}
          >
            {carregando ? (
              <>
                <LoaderCircle size={18} className="login-spinner" />
                Entrando...
              </>
            ) : (
              "Entrar no sistema"
            )}
          </button>
        </form>

        <p className="login-footer">
          Sistema de Departamento Pessoal
        </p>
      </section>
    </main>
  );
}

function App() {
  const [pagina, setPagina] = useState("dashboard");
  const [usuario, setUsuario] = useState(null);
  const [verificandoSessao, setVerificandoSessao] = useState(true);
  const [erroSessao, setErroSessao] = useState("");

  async function verificarSessao() {
    try {
      const sessao = await obterSessao();
      setUsuario(sessao.username);
      setErroSessao("");
    } catch {
      setUsuario(null);
    } finally {
      setVerificandoSessao(false);
    }
  }

  useEffect(() => {
    let ativo = true;

    obterSessao()
      .then((sessao) => {
        if (ativo) {
          setUsuario(sessao.username);
          setErroSessao("");
        }
      })
      .catch(() => {
        if (ativo) {
          setUsuario(null);
        }
      })
      .finally(() => {
        if (ativo) {
          setVerificandoSessao(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, []);

  async function handleLogout() {
    try {
      await logout();
      setUsuario(null);
      setPagina("dashboard");
    } catch (error) {
      setErroSessao(error.message || "NÃ£o foi possÃ­vel encerrar a sessÃ£o.");
    }
  }

  if (verificandoSessao) {
    return (
      <main className="login-page">
        <div className="login-loading">
          <LoaderCircle size={28} className="login-spinner" />
          <span>Verificando sessÃ£o...</span>
        </div>
      </main>
    );
  }

  if (!usuario) {
    return <TelaLogin onLogin={verificarSessao} />;
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <Wallet size={28} />
          <div>
            <h1>GestÃ£o DP</h1>
            <span>Departamento Pessoal</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button
            type="button"
            className={`nav-item ${pagina === "dashboard" ? "active" : ""}`}
            onClick={() => setPagina("dashboard")}
          >
            <LayoutDashboard size={19} />
            Dashboard
          </button>

          <button
            type="button"
            className={`nav-item ${pagina === "folha" ? "active" : ""}`}
            onClick={() => setPagina("folha")}
          >
            <Wallet size={19} />
            Folha de pagamento
          </button>
        </nav>

        <div className="sidebar-footer">
          Sistema de Departamento Pessoal
        </div>

        <div className="login-user-area">
          <div className="login-user-info">
            <div className="login-user-avatar">
              {usuario.substring(0, 1).toUpperCase()}
            </div>
            <div>
              <strong>{usuario}</strong>
              <span>UsuÃ¡rio conectado</span>
            </div>
          </div>

          <button
            type="button"
            className="logout-button"
            onClick={handleLogout}
            title="Sair do sistema"
          >
            <LogOut size={18} />
            Sair
          </button>

          {erroSessao && (
            <p className="login-session-error" role="alert">{erroSessao}</p>
          )}
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <h2>
              {pagina === "dashboard" ? "Dashboard" : "Folha de pagamento"}
            </h2>
            <p>GestÃ£o de Departamento Pessoal</p>
          </div>
        </header>

        {pagina === "dashboard" ? (
          <section className="dashboard-content">
            <div className="welcome-card">
              <span>GESTÃƒO DP</span>
              <h2>Bem-vindo ao sistema!</h2>
              <p>
                Gerencie as folhas de pagamento da sua empresa
                em um sÃ³ lugar.
              </p>

              <button
                type="button"
                className="primary-button"
                onClick={() => setPagina("folha")}
              >
                <Wallet size={18} />
                Acessar folha de pagamento
              </button>
            </div>
          </section>
        ) : (
          <GestaoFolha />
        )}
      </main>
    </div>
  );
}

export default App;
