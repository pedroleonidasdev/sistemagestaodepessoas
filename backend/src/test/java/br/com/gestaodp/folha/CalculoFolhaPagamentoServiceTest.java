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
}