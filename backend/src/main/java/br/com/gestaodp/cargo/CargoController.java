
package br.com.gestaodp.cargo;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import br.com.gestaodp.cargo.dto.CargoResponse;

@RestController
@RequestMapping("/api/empresas/{empresaId}/cargos")
public class CargoController {

    private final CargoService cargoService;

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    public record CargoRequest(
        @NotBlank(message = "O nome do cargo é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CargoResponse criar(
            @PathVariable Long empresaId,
            @Valid @RequestBody CargoRequest request) {

        return CargoResponse.from(
            cargoService.criar(
                empresaId, request.nome(), request.descricao()
            )
        );
    }

    @GetMapping
    public List<CargoResponse> listar(@PathVariable Long empresaId) {
        return cargoService.listar(empresaId)
            .stream()
            .map(CargoResponse::from)
            .toList();
    }

    @GetMapping("/{cargoId}")
    public CargoResponse buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long cargoId) {

        return CargoResponse.from(
            cargoService.buscarPorId(empresaId, cargoId)
        );
    }

    @PutMapping("/{cargoId}")
    public CargoResponse atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long cargoId,
            @Valid @RequestBody CargoRequest request) {

        return CargoResponse.from(
            cargoService.atualizar(
                empresaId,
                cargoId,
                request.nome(),
                request.descricao()
            )
        );
    }

    @PatchMapping("/{cargoId}/situacao")
    public CargoResponse alterarSituacao(
            @PathVariable Long empresaId,
            @PathVariable Long cargoId,
            @RequestParam boolean ativo) {

        return CargoResponse.from(
            cargoService.alterarSituacao(
                empresaId, cargoId, ativo
            )
        );
    }
}
