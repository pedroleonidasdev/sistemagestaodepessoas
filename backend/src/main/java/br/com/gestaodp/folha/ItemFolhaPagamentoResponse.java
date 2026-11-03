
package br.com.gestaodp.folha;

import java.math.BigDecimal;

public class ItemFolhaPagamentoResponse {

    private Long id;
    private Long folhaId;
    private Long funcionarioId;
    private String nomeFuncionario;
    private String matricula;

    private BigDecimal salarioBase;
    private BigDecimal horasExtras;
    private BigDecimal adicionais;
    private BigDecimal comissoes;
    private BigDecimal outrosProventos;
    private BigDecimal totalProventos;

    private BigDecimal baseInss;
    private BigDecimal valorInss;
    private BigDecimal baseIrrf;
    private BigDecimal valorIrrf;
    private BigDecimal valorFgts;

    private Integer numeroDependentes;
    private BigDecimal valeTransporte;
    private Integer diasFaltas;
    private BigDecimal descontoFaltas;
    private BigDecimal descontoValeTransporte;

    private BigDecimal outrosDescontos;
    private BigDecimal totalDescontos;
    private BigDecimal salarioLiquido;

    public ItemFolhaPagamentoResponse(ItemFolhaPagamento item) {
        this.id = item.getId();
        this.folhaId = item.getFolha().getId();
        this.funcionarioId = item.getFuncionario().getId();
        this.nomeFuncionario = item.getFuncionario().getNomeCompleto();
        this.matricula = item.getFuncionario().getMatricula();

        this.salarioBase = item.getSalarioBase();
        this.horasExtras = item.getHorasExtras();
        this.adicionais = item.getAdicionais();
        this.comissoes = item.getComissoes();
        this.outrosProventos = item.getOutrosProventos();
        this.totalProventos = item.getTotalProventos();

        this.baseInss = item.getBaseInss();
        this.valorInss = item.getValorInss();
        this.baseIrrf = item.getBaseIrrf();
        this.valorIrrf = item.getValorIrrf();
        this.valorFgts = item.getValorFgts();

        this.numeroDependentes = item.getNumeroDependentes();
        this.valeTransporte = item.getValeTransporte();
        this.diasFaltas = item.getDiasFaltas();
        this.descontoFaltas = item.getDescontoFaltas();
        this.descontoValeTransporte = item.getDescontoValeTransporte();

        this.outrosDescontos = item.getOutrosDescontos();
        this.totalDescontos = item.getTotalDescontos();
        this.salarioLiquido = item.getSalarioLiquido();
    }

    public Long getId() { return id; }
    public Long getFolhaId() { return folhaId; }
    public Long getFuncionarioId() { return funcionarioId; }
    public String getNomeFuncionario() { return nomeFuncionario; }
    public String getMatricula() { return matricula; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public BigDecimal getHorasExtras() { return horasExtras; }
    public BigDecimal getAdicionais() { return adicionais; }
    public BigDecimal getComissoes() { return comissoes; }
    public BigDecimal getOutrosProventos() { return outrosProventos; }
    public BigDecimal getTotalProventos() { return totalProventos; }

    public BigDecimal getBaseInss() { return baseInss; }
    public BigDecimal getValorInss() { return valorInss; }
    public BigDecimal getBaseIrrf() { return baseIrrf; }
    public BigDecimal getValorIrrf() { return valorIrrf; }
    public BigDecimal getValorFgts() { return valorFgts; }

    public Integer getNumeroDependentes() { return numeroDependentes; }
    public BigDecimal getValeTransporte() { return valeTransporte; }
    public Integer getDiasFaltas() { return diasFaltas; }
    public BigDecimal getDescontoFaltas() { return descontoFaltas; }
    public BigDecimal getDescontoValeTransporte() { return descontoValeTransporte; }

    public BigDecimal getOutrosDescontos() { return outrosDescontos; }
    public BigDecimal getTotalDescontos() { return totalDescontos; }
    public BigDecimal getSalarioLiquido() { return salarioLiquido; }
}
