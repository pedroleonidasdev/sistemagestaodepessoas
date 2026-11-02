let csrfToken = null;
let empresaAtualId = null;

const SAFE_METHODS = ["GET", "HEAD", "OPTIONS", "TRACE"];

async function obterTokenCsrf() {
  if (csrfToken) {
    return csrfToken;
  }

  const response = await fetch("/api/auth/csrf", {
    method: "GET",
    credentials: "include",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Não foi possível obter o token de segurança. HTTP ${response.status}`);
  }

  const data = await response.json();
  csrfToken = data.token;

  if (!csrfToken) {
    throw new Error("A API não forneceu o token de segurança.");
  }

  return csrfToken;
}

function limparTokenCsrf() {
  csrfToken = null;
}

/**
 * Função central para realizar requisições à API usando sessão.
 */
export async function apiFetch(path, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = {
    Accept: "application/json",
    ...options.headers,
  };

  if (options.body !== undefined && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  if (!SAFE_METHODS.includes(method)) {
    headers["X-XSRF-TOKEN"] = await obterTokenCsrf();
  }

  const response = await fetch(path, {
    ...options,
    method,
    credentials: "include",
    headers,
  });

  if (response.status === 401) {
    limparTokenCsrf();
  }

  if (!response.ok) {
    let message = `Erro HTTP ${response.status}`;

    try {
      const data = await response.json();
      message = data.mensagem || data.message || message;
    } catch {
      // A resposta pode não conter JSON.
    }

    throw new Error(message);
  }

  if ([204, 205].includes(response.status)) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";

  if (!contentType.includes("application/json")) {
    return null;
  }

  return response.json();
}

/**
 * Realiza login e inicia uma sessão autenticada.
 */
export async function login(username, password) {
  const token = await obterTokenCsrf();

  const response = await fetch("/api/auth/login", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      "X-XSRF-TOKEN": token,
    },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    limparTokenCsrf();

    let message = "Não foi possível realizar o login.";

    try {
      const data = await response.json();
      message = data.mensagem || data.message || message;
    } catch {
      // A resposta pode não conter JSON.
    }

    throw new Error(message);
  }

  limparTokenCsrf();
  await obterTokenCsrf();

  return response.json();
}

/**
 * Consulta se existe uma sessão autenticada.
 */
export function obterSessao() {
  return apiFetch("/api/auth/me");
}

/**
 * Encerra a sessão autenticada.
 */
export async function logout() {
  const result = await apiFetch("/api/auth/logout", {
    method: "POST",
  });

  limparTokenCsrf();
  empresaAtualId = null;
  return result;
}

/**
 * Lista as folhas de pagamento de uma empresa.
 */
export function listarFolha(empresaId = 1) {
  return apiFetch(`/api/empresas/${empresaId}/folhas-pagamento`);
}

/**
 * Alias para compatibilidade com outras telas.
 */
export function listarFolhas(empresaId = 1) {
  return listarFolha(empresaId);
}

/**
 * Cria uma folha para determinada competência.
 */
export function criarFolha(competencia, empresaId = 1) {
  return apiFetch(`/api/empresas/${empresaId}/folhas-pagamento`, {
    method: "POST",
    body: JSON.stringify({ competencia }),
  });
}

/**
 * Lista os funcionários cadastrados na empresa.
 */
export function listarFuncionarios(empresaId = 1) {
  return apiFetch(`/api/empresas/${empresaId}/funcionarios`);
}

/**
 * Lista os itens de uma folha específica.
 */
export function listarItensFolha(folhaId, empresaId = 1) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens`
  );
}

/**
 * Adiciona um funcionário à folha.
 */
export function adicionarFuncionarioFolha(
  folhaId,
  funcionarioId,
  empresaId = 1
) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens`,
    {
      method: "POST",
      body: JSON.stringify({
        funcionarioId: Number(funcionarioId),
      }),
    }
  );
}

/**
 * Calcula os valores dos itens da folha.
 */
export function calcularFolha(folhaId, empresaId = 1) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens/calcular`,
    {
      method: "POST",
    }
  );
}

/**
 * Edita os proventos e descontos adicionais de um item da folha.
 */
export function editarItemFolha(
  folhaId,
  itemId,
  dados,
  empresaId = 1
) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens/${itemId}`,
    {
      method: "PUT",
      body: JSON.stringify({
        horasExtras: dados.horasExtras,
        adicionais: dados.adicionais,
        comissoes: dados.comissoes,
        outrosProventos: dados.outrosProventos,
        outrosDescontos: dados.outrosDescontos,
      }),
    }
  );
}

/**
 * Exclui um item de uma folha ainda aberta.
 */
export function excluirItemFolha(folhaId, itemId, empresaId = 1) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens/${itemId}`,
    {
      method: "DELETE",
    }
  );
}

/**
 * Fecha uma folha de pagamento.
 */
export function fecharFolha(folhaId, empresaId = 1) {
  return apiFetch(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/fechar`,
    {
      method: "POST",
    }
  );
}

/**
 * Busca um PDF autenticado e inicia o download no navegador.
 */
async function baixarPdf(path, nomeArquivo) {
  const response = await fetch(path, {
    method: "GET",
    credentials: "include",
    headers: {
      Accept: "application/pdf",
    },
  });

  if (response.status === 401) {
    limparTokenCsrf();
    throw new Error("Sua sessão expirou. Entre novamente no sistema.");
  }

  if (!response.ok) {
    let message = `Erro HTTP ${response.status}`;

    try {
      const data = await response.json();
      message = data.mensagem || data.message || message;
    } catch {
      // A resposta de erro pode não conter JSON.
    }

    throw new Error(message);
  }

  const blob = await response.blob();

  if (blob.type && !blob.type.includes("application/pdf")) {
    throw new Error("A API não retornou um arquivo PDF.");
  }

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = nomeArquivo;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

/**
 * Baixa o holerite de um item específico.
 */
export function baixarHolerite(folhaId, itemId, empresaId = 1) {
  return baixarPdf(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/itens/${itemId}/holerite.pdf`,
    `holerite-${itemId}.pdf`
  );
}

/**
 * Baixa o relatório PDF de uma folha.
 */
export function baixarRelatorioFolha(folhaId, empresaId = 1) {
  return baixarPdf(
    `/api/empresas/${empresaId}/folhas-pagamento/${folhaId}/relatorio.pdf`,
    `relatorio-folha-${folhaId}.pdf`
  );
}

/**
 * Descobre a empresa usada pelo sistema: a primeira empresa ativa cadastrada.
 */
export async function obterEmpresaId() {
  if (empresaAtualId) {
    return empresaAtualId;
  }

  const empresas = await apiFetch("/api/empresas");
  const lista = Array.isArray(empresas) ? empresas : [];
  const empresa = lista.find((item) => item.ativo !== false) ?? lista[0];

  if (!empresa) {
    throw new Error(
      "Nenhuma empresa cadastrada. Cadastre uma empresa para continuar."
    );
  }

  empresaAtualId = empresa.id;
  return empresaAtualId;
}

/**
 * Cadastra um funcionário ativo na empresa.
 */
export function cadastrarFuncionario(dados, empresaId = 1) {
  return apiFetch(`/api/empresas/${empresaId}/funcionarios`, {
    method: "POST",
    body: JSON.stringify({
      matricula: dados.matricula,
      nomeCompleto: dados.nomeCompleto,
      cpf: dados.cpf,
      salarioBase: Number(dados.salarioBase),
      dataAdmissao: dados.dataAdmissao,
      situacao: "ATIVO",
    }),
  });
}
