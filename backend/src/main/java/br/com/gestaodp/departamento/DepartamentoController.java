
package br.com.gestaodp.departamento;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import br.com.gestaodp.departamento.dto.DepartamentoResponse;

@RestController
@RequestMapping("/api/empresas/{empresaId}/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    public record DepartamentoRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartamentoResponse criar(
            @PathVariable Long empresaId,
            @Valid @RequestBody DepartamentoRequest request) {

        return DepartamentoResponse.from(
            departamentoService.criar(
                empresaId, request.nome(), request.descricao()
            )
        );
    }

    @GetMapping
    public List<DepartamentoResponse> listar(
            @PathVariable Long empresaId) {

        return departamentoService.listar(empresaId)
            .stream()
            .map(DepartamentoResponse::from)
            .toList();
    }

    @GetMapping("/{departamentoId}")
    public DepartamentoResponse buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long departamentoId) {

        return DepartamentoResponse.from(
            departamentoService.buscarPorId(empresaId, departamentoId)
        );
    }

    @PutMapping("/{departamentoId}")
    public DepartamentoResponse atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long departamentoId,
            @Valid @RequestBody DepartamentoRequest request) {

        return DepartamentoResponse.from(
            departamentoService.atualizar(
                empresaId,
                departamentoId,
                request.nome(),
                request.descricao()
            )
        );
    }

    @PatchMapping("/{departamentoId}/situacao")
    public DepartamentoResponse alterarSituacao(
            @PathVariable Long empresaId,
            @PathVariable Long departamentoId,
            @RequestParam boolean ativo) {

        return DepartamentoResponse.from(
            departamentoService.alterarSituacao(
                empresaId, departamentoId, ativo
            )
        );
    }
}
