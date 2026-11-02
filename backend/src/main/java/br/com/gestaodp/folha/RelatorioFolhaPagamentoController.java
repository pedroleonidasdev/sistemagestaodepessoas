
package br.com.gestaodp.folha;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas/{empresaId}/folhas-pagamento")
public class RelatorioFolhaPagamentoController {

    private final RelatorioFolhaPagamentoService relatorioService;

    public RelatorioFolhaPagamentoController(
            RelatorioFolhaPagamentoService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/{folhaId}/relatorio.pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId) {

        byte[] pdf = relatorioService.gerarPdf(empresaId, folhaId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"folha-pagamento.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
