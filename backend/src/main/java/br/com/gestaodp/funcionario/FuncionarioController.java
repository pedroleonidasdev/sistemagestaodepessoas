
package br.com.gestaodp.funcionario;

import br.com.gestaodp.funcionario.dto.FuncionarioResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse cadastrar(
            @PathVariable Long empresaId,
            @RequestBody Funcionario funcionario) {

        Funcionario cadastrado =
                funcionarioService.cadastrar(empresaId, funcionario);

        return FuncionarioResponse.from(cadastrado);
    }

    @GetMapping
    public List<FuncionarioResponse> listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) SituacaoFuncionario situacao,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) Long cargoId,
            @RequestParam(required = false) String nome) {

        if (situacao == null
                && departamentoId == null
                && cargoId == null
                && (nome == null || nome.isBlank())) {

            return funcionarioService.listarPorEmpresa(empresaId)
                    .stream()
                    .map(FuncionarioResponse::from)
                    .toList();
        }

        return funcionarioService.filtrar(
                        empresaId,
                        situacao,
                        departamentoId,
                        cargoId,
                        nome)
                .stream()
                .map(FuncionarioResponse::from)
                .toList();
    }

    @GetMapping("/{funcionarioId}")
    public FuncionarioResponse buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long funcionarioId) {

        return FuncionarioResponse.from(
                funcionarioService.buscarPorId(empresaId, funcionarioId)
        );
    }

    @PutMapping("/{funcionarioId}")
    public FuncionarioResponse atualizar(
            @PathVariable Long empresaId,
            @PathVariable Long funcionarioId,
            @RequestBody Funcionario funcionario) {

        Funcionario atualizado = funcionarioService.atualizar(
                empresaId,
                funcionarioId,
                funcionario
        );

        return FuncionarioResponse.from(atualizado);
    }

    @PatchMapping("/{funcionarioId}/situacao")
    public FuncionarioResponse alterarSituacao(
            @PathVariable Long empresaId,
            @PathVariable Long funcionarioId,
            @RequestBody SituacaoFuncionario situacao) {

        Funcionario atualizado = funcionarioService.alterarSituacao(
                empresaId,
                funcionarioId,
                situacao
        );

        return FuncionarioResponse.from(atualizado);
    }
}
