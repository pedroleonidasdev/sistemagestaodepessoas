
package br.com.gestaodp.cargo.dto;

import br.com.gestaodp.cargo.Cargo;
import java.time.LocalDateTime;

public record CargoResponse(
    Long id,
    Long empresaId,
    String razaoSocialEmpresa,
    String nome,
    String descricao,
    Boolean ativo,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {

    public static CargoResponse from(Cargo cargo) {
        return new CargoResponse(
            cargo.getId(),
            cargo.getEmpresa().getId(),
            cargo.getEmpresa().getRazaoSocial(),
            cargo.getNome(),
            cargo.getDescricao(),
            cargo.getAtivo(),
            cargo.getCriadoEm(),
            cargo.getAtualizadoEm()
        );
    }
}
