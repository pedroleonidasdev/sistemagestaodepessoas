package br.com.gestaodp.folha;

import java.math.BigDecimal;

public class EditarItemFolhaRequest {

    private BigDecimal horasExtras;
    private BigDecimal adicionais;
    private BigDecimal comissoes;
    private BigDecimal outrosProventos;
    private BigDecimal outrosDescontos;

    public BigDecimal getHorasExtras() {
        return horasExtras;
    }

    public void setHorasExtras(BigDecimal horasExtras) {
        this.horasExtras = horasExtras;
    }

    public BigDecimal getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(BigDecimal adicionais) {
        this.adicionais = adicionais;
    }

    public BigDecimal getComissoes() {
        return comissoes;
    }

    public void setComissoes(BigDecimal comissoes) {
        this.comissoes = comissoes;
    }

    public BigDecimal getOutrosProventos() {
        return outrosProventos;
    }

    public void setOutrosProventos(BigDecimal outrosProventos) {
        this.outrosProventos = outrosProventos;
    }

    public BigDecimal getOutrosDescontos() {
        return outrosDescontos;
    }

    public void setOutrosDescontos(BigDecimal outrosDescontos) {
        this.outrosDescontos = outrosDescontos;
    }
}
