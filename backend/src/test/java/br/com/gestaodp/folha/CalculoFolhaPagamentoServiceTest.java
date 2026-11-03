package br.com.gestaodp.folha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoFolhaPagamentoServiceTest {

    private CalculoFolhaPagamentoService calculoService;
    private ItemFolhaPagamento item;

    @BeforeEach
    void configurar() {
        calculoService = new CalculoFolhaPagamentoService();

        item = new ItemFolhaPagamento();
        item.setSalarioBase(new BigDecimal("3500.00"));
        item.setHorasExtras(new BigDecimal("0.00"));
        item.setAdicionais(new BigDecimal("0.00"));
        item.setComissoes(new BigDecimal("0.00"));
        item.setOutrosProventos(new BigDecimal("0.00"));
        item.setOutrosDescontos(new BigDecimal("0.00"));
    }

    @Test
    void deveCalcularFolhaComSalarioBaseDe3500Reais() {
        calculoService.calcular(item);

        assertEquals(0, new BigDecimal("3500.00")
                .compareTo(item.getTotalProventos()));

        assertEquals(0, new BigDecimal("3500.00")
                .compareTo(item.getBaseInss()));

        assertEquals(0, new BigDecimal("308.60")
                .compareTo(item.getValorInss()));

        assertEquals(0, new BigDecimal("2892.80")
                .compareTo(item.getBaseIrrf()));

        assertEquals(0, new BigDecimal("0.00")
                .compareTo(item.getValorIrrf()));

        assertEquals(0, new BigDecimal("280.00")
                .compareTo(item.getValorFgts()));

        assertEquals(0, new BigDecimal("308.60")
                .compareTo(item.getTotalDescontos()));

        assertEquals(0, new BigDecimal("3191.40")
                .compareTo(item.getSalarioLiquido()));
    }

    @Test
    void deveSomarProventosEConsiderarOutrosDescontos() {
        item.setHorasExtras(new BigDecimal("100.00"));
        item.setAdicionais(new BigDecimal("50.00"));
        item.setComissoes(new BigDecimal("25.00"));
        item.setOutrosProventos(new BigDecimal("10.00"));
        item.setOutrosDescontos(new BigDecimal("20.00"));

        calculoService.calcular(item);

        assertEquals(0, new BigDecimal("3685.00")
                .compareTo(item.getTotalProventos()));

        assertEquals(0, new BigDecimal("330.80")
                .compareTo(item.getValorInss()));

        assertEquals(0, new BigDecimal("294.80")
                .compareTo(item.getValorFgts()));

assertEquals(0, new BigDecimal("350.80")
        .compareTo(item.getTotalDescontos()));

assertEquals(0, new BigDecimal("3334.20")
        .compareTo(item.getSalarioLiquido()));
}

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual),
                "Esperado " + esperado + " mas foi " + atual);
    }

    @Test
    void dependentesReduzemABaseEOImpostoDeRenda() {
        item.setSalarioBase(new BigDecimal("6000.00"));

        calculoService.calcular(item);

        assertValor("5358.49", item.getBaseIrrf());
        assertValor("385.10", item.getValorIrrf());

        item.setNumeroDependentes(2);

        calculoService.calcular(item);

        assertValor("4979.31", item.getBaseIrrf());
        assertValor("280.83", item.getValorIrrf());
        assertValor("922.34", item.getTotalDescontos());
        assertValor("5077.66", item.getSalarioLiquido());
    }

    @Test
    void faltasDescontamUmTrintaAvosEReduzemAsBases() {
        item.setSalarioBase(new BigDecimal("3000.00"));
        item.setDiasFaltas(5);

        calculoService.calcular(item);

        assertValor("3000.00", item.getTotalProventos());
        assertValor("500.00", item.getDescontoFaltas());
        assertValor("2500.00", item.getBaseInss());
        assertValor("200.69", item.getValorInss());
        assertValor("200.00", item.getValorFgts());
        assertValor("700.69", item.getTotalDescontos());
        assertValor("2299.31", item.getSalarioLiquido());
    }

    @Test
    void valeTransporteDescontaNoMaximoSeisPorCentoDoSalario() {
        item.setSalarioBase(new BigDecimal("2000.00"));
        item.setValeTransporte(new BigDecimal("200.00"));

        calculoService.calcular(item);

        assertValor("120.00", item.getDescontoValeTransporte());
        assertValor("275.69", item.getTotalDescontos());
        assertValor("1724.31", item.getSalarioLiquido());
    }

    @Test
    void valeTransporteMenorQueSeisPorCentoDescontaSoOCusto() {
        item.setSalarioBase(new BigDecimal("2000.00"));
        item.setValeTransporte(new BigDecimal("80.00"));

        calculoService.calcular(item);

        assertValor("80.00", item.getDescontoValeTransporte());
        assertValor("235.69", item.getTotalDescontos());
        assertValor("1764.31", item.getSalarioLiquido());
    }

    @Test
    void semDependentesFaltasOuValeTransporteMantemOCalculoAnterior() {
        calculoService.calcular(item);

        assertValor("0.00", item.getDescontoFaltas());
        assertValor("0.00", item.getDescontoValeTransporte());
        assertValor("3191.40", item.getSalarioLiquido());
    }
}
