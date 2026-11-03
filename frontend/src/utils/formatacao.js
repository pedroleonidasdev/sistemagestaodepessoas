export function numero(valor) {
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

export function moeda(valor) {
  return numero(valor).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });
}

export function competenciaLegivel(competencia) {
  if (!competencia) return "Sem competência";

  const [ano, mes] = competencia.split("-");
  const data = new Date(Number(ano), Number(mes) - 1, 1);

  return data.toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });
}

/** Data de hoje no fuso local, no formato AAAA-MM-DD. */
export function hojeISO() {
  const data = new Date();
  data.setMinutes(data.getMinutes() - data.getTimezoneOffset());
  return data.toISOString().slice(0, 10);
}

/** Converte AAAA-MM-DD em DD/MM/AAAA sem passar por fuso horário. */
export function dataBR(iso) {
  if (!iso) return "—";
  const [ano, mes, dia] = String(iso).slice(0, 10).split("-");
  return `${dia}/${mes}/${ano}`;
}

export function somenteDigitos(texto) {
  return String(texto ?? "").replace(/\D/g, "");
}

export function formatarCpf(cpf) {
  const d = somenteDigitos(cpf);

  if (d.length !== 11) return cpf || "—";

  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

/** Confere tamanho, dígitos repetidos e dígitos verificadores do CPF. */
export function cpfValido(valor) {
  const cpf = somenteDigitos(valor);

  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;

  for (const posicao of [9, 10]) {
    let soma = 0;

    for (let i = 0; i < posicao; i++) {
      soma += Number(cpf[i]) * (posicao + 1 - i);
    }

    let digito = (soma * 10) % 11;
    if (digito === 10) digito = 0;

    if (digito !== Number(cpf[posicao])) return false;
  }

  return true;
}
