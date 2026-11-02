
package br.com.gestaodp.empresa;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Empresa cadastrar(@Valid @RequestBody Empresa empresa) {
        return empresaService.cadastrar(empresa);
    }

    @GetMapping
    public List<Empresa> listarTodas() {
        return empresaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Empresa buscarPorId(@PathVariable Long id) {
        return empresaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Empresa atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Empresa empresa) {
        return empresaService.atualizar(id, empresa);
    }

    @PatchMapping("/{id}/status")
    public Empresa alterarStatus(
            @PathVariable Long id,
            @RequestParam boolean ativo) {
        return empresaService.alterarStatus(id, ativo);
    }
}
