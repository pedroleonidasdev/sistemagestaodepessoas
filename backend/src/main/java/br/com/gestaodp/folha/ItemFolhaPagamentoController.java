
package br.com.gestaodp.folha;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(
    "/api/empresas/{empresaId}/folhas-pagamento/{folhaId}/itens"
)
public class ItemFolhaPagamentoController {

    private final ItemFolhaPagamentoService itemService;

    public ItemFolhaPagamentoController(
            ItemFolhaPagamentoService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemFolhaPagamentoResponse adicionar(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId,
            @RequestBody AdicionarFuncionarioFolhaRequest request) {

        if (request == null || request.getFuncionarioId() == null) {
            throw new IllegalArgumentException(
                    "Informe o ID do funcionário."
            );
        }

        return new ItemFolhaPagamentoResponse(
                itemService.adicionar(
                        empresaId,
                        folhaId,
                        request.getFuncionarioId()
                )
        );
    }
   @PostMapping("/calcular")
   @ResponseStatus(HttpStatus.NO_CONTENT)
    public void calcular(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId) {

    itemService.calcularFolha(empresaId, folhaId);
}
    @GetMapping
    public List<ItemFolhaPagamentoResponse> listar(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId) {

        return itemService.listar(empresaId, folhaId)
                .stream()
                .map(ItemFolhaPagamentoResponse::new)
                .toList();
    }


    @PutMapping("/{itemId}")
    public ItemFolhaPagamentoResponse editar(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId,
            @PathVariable Long itemId,
            @RequestBody EditarItemFolhaRequest request) {

        return new ItemFolhaPagamentoResponse(
                itemService.editar(
                        empresaId,
                        folhaId,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId,
            @PathVariable Long itemId) {

        itemService.excluir(empresaId, folhaId, itemId);
    }


}
