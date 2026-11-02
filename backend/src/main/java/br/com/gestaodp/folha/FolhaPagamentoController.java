package br.com.gestaodp.folha;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/folhas-pagamento")
public class FolhaPagamentoController {

    private final FolhaPagamentoService folhaService;

    public FolhaPagamentoController(FolhaPagamentoService folhaService) {
        this.folhaService = folhaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolhaPagamentoResponse abrir(
            @PathVariable Long empresaId,
            @RequestBody AbrirFolhaRequest request) {
        return FolhaPagamentoResponse.from(
                folhaService.abrir(empresaId, request)
        );
    }


    @GetMapping
    public List<FolhaPagamentoResponse> listar(
            @PathVariable Long empresaId) {
        return folhaService.listarPorEmpresa(empresaId)
                .stream()
                .map(FolhaPagamentoResponse::from)
                .toList();
    }

@PostMapping("/{folhaId}/fechar")
public ResponseEntity<Void> fechar(
        @PathVariable Long empresaId,
        @PathVariable Long folhaId) {

    folhaService.fechar(empresaId, folhaId);
    return ResponseEntity.noContent().build();
}

    @GetMapping("/{folhaId}")
    public FolhaPagamentoResponse buscarPorId(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId) {
        return FolhaPagamentoResponse.from(
                folhaService.buscarPorId(empresaId, folhaId)
        );
    }
}
