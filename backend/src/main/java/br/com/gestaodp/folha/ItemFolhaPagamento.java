
package br.com.gestaodp.folha;

import br.com.gestaodp.funcionario.Funcionario;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "item_folha_pagamento",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_folha_funcionario",
        columnNames = {"folha_id", "funcionario_id"}
    )
)
public class ItemFolhaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folha_id", nullable = false)
    private FolhaPagamento folha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "salario_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "horas_extras", nullable = false, precision = 12, scale = 2)
    private BigDecimal horasExtras = BigDecimal.ZERO;

    @Column(name = "adicionais", nullable = false, precision = 12, scale = 2)
    private BigDecimal adicionais = BigDecimal.ZERO;

    @Column(name = "comissoes", nullable = false, precision = 12, scale = 2)
    private BigDecimal comissoes = BigDecimal.ZERO;

    @Column(name = "outros_proventos", nullable = false, precision = 12, scale = 2)
    private BigDecimal outrosProventos = BigDecimal.ZERO;

    @Column(name = "outros_descontos", nullable = false, precision = 12, scale = 2)
    private BigDecimal outrosDescontos = BigDecimal.ZERO;

    @Column(name = "numero_dependentes", nullable = false)
    private Integer numeroDependentes = 0;

    @Column(name = "vale_transporte", nullable = false, precision = 12, scale = 2)
    private BigDecimal valeTransporte = BigDecimal.ZERO;

    @Column(name = "dias_faltas", nullable = false)
    private Integer diasFaltas = 0;

    @Column(name = "desconto_faltas", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoFaltas = BigDecimal.ZERO;

    @Column(name = "desconto_vale_transporte", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoValeTransporte = BigDecimal.ZERO;

    @Column(name = "total_proventos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProventos = BigDecimal.ZERO;

    @Column(name = "base_inss", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseInss = BigDecimal.ZERO;

    @Column(name = "valor_inss", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorInss = BigDecimal.ZERO;

    @Column(name = "base_irrf", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseIrrf = BigDecimal.ZERO;

    @Column(name = "valor_irrf", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorIrrf = BigDecimal.ZERO;

    @Column(name = "valor_fgts", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFgts = BigDecimal.ZERO;

    @Column(name = "total_descontos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDescontos = BigDecimal.ZERO;

    @Column(name = "salario_liquido", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioLiquido = BigDecimal.ZERO;

    public Long getId() { return id; }
    public FolhaPagamento getFolha() { return folha; }
    public void setFolha(FolhaPagamento folha) { this.folha = folha; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal v) { this.salarioBase = v; }
    public BigDecimal getHorasExtras() { return horasExtras; }
    public void setHorasExtras(BigDecimal v) { this.horasExtras = v; }
    public BigDecimal getAdicionais() { return adicionais; }
    public void setAdicionais(BigDecimal v) { this.adicionais = v; }
    public BigDecimal getComissoes() { return comissoes; }
    public void setComissoes(BigDecimal v) { this.comissoes = v; }
    public BigDecimal getOutrosProventos() { return outrosProventos; }
    public void setOutrosProventos(BigDecimal v) { this.outrosProventos = v; }

    public BigDecimal getTotalProventos() { return totalProventos; }
    public void setTotalProventos(BigDecimal v) { this.totalProventos = v; }
    public BigDecimal getBaseInss() { return baseInss; }
    public void setBaseInss(BigDecimal v) { this.baseInss = v; }
    public BigDecimal getValorInss() { return valorInss; }
    public void setValorInss(BigDecimal v) { this.valorInss = v; }
    public BigDecimal getBaseIrrf() { return baseIrrf; }
    public void setBaseIrrf(BigDecimal v) { this.baseIrrf = v; }
    public BigDecimal getValorIrrf() { return valorIrrf; }
    public void setValorIrrf(BigDecimal v) { this.valorIrrf = v; }
    public BigDecimal getValorFgts() { return valorFgts; }
    public void setValorFgts(BigDecimal v) { this.valorFgts = v; }
    public BigDecimal getOutrosDescontos() { return outrosDescontos; }
    public void setOutrosDescontos(BigDecimal v) { this.outrosDescontos = v; }
    public BigDecimal getTotalDescontos() { return totalDescontos; }
    public void setTotalDescontos(BigDecimal v) { this.totalDescontos = v; }
    public BigDecimal getSalarioLiquido() { return salarioLiquido; }
    public void setSalarioLiquido(BigDecimal v) { this.salarioLiquido = v; }

    public Integer getNumeroDependentes() { return numeroDependentes; }
    public void setNumeroDependentes(Integer v) { this.numeroDependentes = v; }
    public BigDecimal getValeTransporte() { return valeTransporte; }
    public void setValeTransporte(BigDecimal v) { this.valeTransporte = v; }
    public Integer getDiasFaltas() { return diasFaltas; }
    public void setDiasFaltas(Integer v) { this.diasFaltas = v; }
    public BigDecimal getDescontoFaltas() { return descontoFaltas; }
    public void setDescontoFaltas(BigDecimal v) { this.descontoFaltas = v; }
    public BigDecimal getDescontoValeTransporte() { return descontoValeTransporte; }
    public void setDescontoValeTransporte(BigDecimal v) { this.descontoValeTransporte = v; }
}
