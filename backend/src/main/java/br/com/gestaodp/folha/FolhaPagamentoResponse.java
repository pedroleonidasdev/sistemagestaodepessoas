package br.com.gestaodp.folha;

import java.time.LocalDateTime;

public class FolhaPagamentoResponse {

    private Long id;
    private Long empresaId;
    private String competencia;
    private String status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public FolhaPagamentoResponse(
            Long id,
            Long empresaId,
            String competencia,
            String status,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm) {
        this.id = id;
        this.empresaId = empresaId;
        this.competencia = competencia;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static FolhaPagamentoResponse from(FolhaPagamento folha) {
        return new FolhaPagamentoResponse(
                folha.getId(),
                folha.getEmpresa().getId(),
                folha.getCompetencia(),
                folha.getStatus(),
                folha.getCriadoEm(),
                folha.getAtualizadoEm()
        );
    }

    public Long getId() { return id; }
    public Long getEmpresaId() { return empresaId; }
    public String getCompetencia() { return competencia; }
    public String getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
