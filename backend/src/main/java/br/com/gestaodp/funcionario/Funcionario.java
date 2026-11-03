
package br.com.gestaodp.funcionario;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.departamento.Departamento;
import br.com.gestaodp.cargo.Cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "funcionarios",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_funcionarios_empresa_matricula",
            columnNames = {"empresa_id", "matricula"}
        ),
        @UniqueConstraint(
            name = "uk_funcionarios_empresa_cpf",
            columnNames = {"empresa_id", "cpf"}
        )
    }
)
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Column(nullable = false, length = 30)
    private String matricula;

    @Column(name = "nome_completo", nullable = false, length = 200)
    private String nomeCompleto;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(
        name = "salario_base",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal salarioBase = BigDecimal.ZERO;

    @Column(name = "numero_dependentes", nullable = false)
    private Integer numeroDependentes = 0;

    @Column(
        name = "vale_transporte_mensal",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal valeTransporteMensal = BigDecimal.ZERO;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoFuncionario situacao = SituacaoFuncionario.ATIVO;

    @Column(
        name = "criado_em",
        insertable = false,
        updatable = false
    )
    private LocalDateTime criadoEm;

    @Column(
        name = "atualizado_em",
        insertable = false,
        updatable = false
    )
    private LocalDateTime atualizadoEm;

    public Funcionario() {
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        if (salarioBase == null
                || salarioBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "O salário-base não pode ser nulo ou negativo."
            );
        }

        this.salarioBase = salarioBase.setScale(
            2,
            RoundingMode.HALF_UP
        );
    }

    public Integer getNumeroDependentes() {
        return numeroDependentes;
    }

    public void setNumeroDependentes(Integer numeroDependentes) {
        if (numeroDependentes == null) {
            this.numeroDependentes = 0;
            return;
        }

        if (numeroDependentes < 0 || numeroDependentes > 30) {
            throw new IllegalArgumentException(
                "O número de dependentes deve ficar entre 0 e 30."
            );
        }

        this.numeroDependentes = numeroDependentes;
    }

    public BigDecimal getValeTransporteMensal() {
        return valeTransporteMensal;
    }

    public void setValeTransporteMensal(BigDecimal valeTransporteMensal) {
        if (valeTransporteMensal == null) {
            this.valeTransporteMensal = BigDecimal.ZERO.setScale(2);
            return;
        }

        if (valeTransporteMensal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "O valor do vale-transporte não pode ser negativo."
            );
        }

        this.valeTransporteMensal = valeTransporteMensal.setScale(
            2,
            RoundingMode.HALF_UP
        );
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public SituacaoFuncionario getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoFuncionario situacao) {
        this.situacao = situacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
