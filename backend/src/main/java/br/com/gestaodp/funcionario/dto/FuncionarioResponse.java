
package br.com.gestaodp.funcionario.dto;

import br.com.gestaodp.funcionario.Funcionario;
import br.com.gestaodp.funcionario.SituacaoFuncionario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FuncionarioResponse(
        Long id,
        Long empresaId,
        String razaoSocialEmpresa,
        String matricula,
        String nomeCompleto,
        String cpf,
        BigDecimal salarioBase,
        Integer numeroDependentes,
        BigDecimal valeTransporteMensal,
        LocalDate dataAdmissao,
        SituacaoFuncionario situacao,
        Long departamentoId,
        String nomeDepartamento,
        Long cargoId,
        String nomeCargo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static FuncionarioResponse from(Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getEmpresa().getId(),
                funcionario.getEmpresa().getRazaoSocial(),
                funcionario.getMatricula(),
                funcionario.getNomeCompleto(),
                funcionario.getCpf(),
                funcionario.getSalarioBase(),
                funcionario.getNumeroDependentes(),
                funcionario.getValeTransporteMensal(),
                funcionario.getDataAdmissao(),
                funcionario.getSituacao(),
                funcionario.getDepartamento() != null
                        ? funcionario.getDepartamento().getId()
                        : null,
                funcionario.getDepartamento() != null
                        ? funcionario.getDepartamento().getNome()
                        : null,
                funcionario.getCargo() != null
                        ? funcionario.getCargo().getId()
                        : null,
                funcionario.getCargo() != null
                        ? funcionario.getCargo().getNome()
                        : null,
                funcionario.getCriadoEm(),
                funcionario.getAtualizadoEm()
        );
    }
}
