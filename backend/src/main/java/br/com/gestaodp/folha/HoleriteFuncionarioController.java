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
public class HoleriteFuncionarioController {

    private final HoleriteFuncionarioService holeriteService;

    public HoleriteFuncionarioController(
            HoleriteFuncionarioService holeriteService) {
        this.holeriteService = holeriteService;
    }

    @GetMapping("/{folhaId}/itens/{itemId}/holerite.pdf")
    public ResponseEntity<byte[]> gerarHolerite(
            @PathVariable Long empresaId,
            @PathVariable Long folhaId,
            @PathVariable Long itemId) {

        byte[] pdf = holeriteService.gerarPdf(
                empresaId, folhaId, itemId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"holerite.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}