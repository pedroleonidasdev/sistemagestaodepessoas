
package br.com.gestaodp.departamento.dto;

import br.com.gestaodp.departamento.Departamento;
import java.time.LocalDateTime;

public record DepartamentoResponse(
    Long id,
    Long empresaId,
    String razaoSocialEmpresa,
    String nome,
    String descricao,
    Boolean ativo,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {

    public static DepartamentoResponse from(Departamento departamento) {
        return new DepartamentoResponse(
            departamento.getId(),
            departamento.getEmpresa().getId(),
            departamento.getEmpresa().getRazaoSocial(),
            departamento.getNome(),
            departamento.getDescricao(),
            departamento.getAtivo(),
            departamento.getCriadoEm(),
            departamento.getAtualizadoEm()
        );
    }
}
