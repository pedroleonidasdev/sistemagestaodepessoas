
package br.com.gestaodp.folha;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculoFolhaPagamentoService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentual(BigDecimal base, String taxa) {
        return dinheiro(base.multiply(new BigDecimal(taxa)));
    }

    public void calcular(ItemFolhaPagamento item) {

        BigDecimal salario = dinheiro(item.getSalarioBase());
        BigDecimal horasExtras = dinheiro(item.getHorasExtras());
        BigDecimal adicionais = dinheiro(item.getAdicionais());
        BigDecimal comissoes = dinheiro(item.getComissoes());
        BigDecimal outrosProventos = dinheiro(item.getOutrosProventos());
        BigDecimal outrosDescontos = dinheiro(item.getOutrosDescontos());

        BigDecimal bruto = salario
                .add(horasExtras)
                .add(adicionais)
                .add(comissoes)
                .add(outrosProventos);

        BigDecimal inss = calcularInss(bruto);

        // Desconto simplificado mensal utilizado no cálculo do IRRF.
        BigDecimal descontoSimplificado = new BigDecimal("607.20");
        BigDecimal deducaoIrrf = inss.max(descontoSimplificado);

        BigDecimal baseIrrf =
                bruto.subtract(deducaoIrrf).max(ZERO);

        BigDecimal irrf = calcularIrrf(bruto, baseIrrf);

        // FGTS informativo: não é descontado do salário do empregado.
        BigDecimal fgts = percentual(bruto, "0.08");

        BigDecimal descontos = inss
                .add(irrf)
                .add(outrosDescontos);

        BigDecimal liquido = bruto.subtract(descontos);

        item.setTotalProventos(dinheiro(bruto));
        item.setBaseInss(dinheiro(bruto));
        item.setValorInss(inss);
        item.setBaseIrrf(dinheiro(baseIrrf));
        item.setValorIrrf(irrf);
        item.setValorFgts(fgts);
        item.setTotalDescontos(dinheiro(descontos));
        item.setSalarioLiquido(dinheiro(liquido));
    }

    private BigDecimal calcularInss(BigDecimal remuneracao) {

        BigDecimal base = remuneracao.min(
                new BigDecimal("8475.55")
        );

        BigDecimal total = ZERO;

        BigDecimal limite1 = new BigDecimal("1621.00");
        BigDecimal limite2 = new BigDecimal("2902.84");
        BigDecimal limite3 = new BigDecimal("4354.27");

        // Primeira faixa: 7,5%.
        BigDecimal faixa1 = base.min(limite1);

        total = total.add(
                faixa1.multiply(new BigDecimal("0.075"))
        );

        // Segunda faixa: 9%.
        if (base.compareTo(limite1) > 0) {
            BigDecimal faixa2 = base.min(limite2).subtract(limite1);

            total = total.add(
                    faixa2.multiply(new BigDecimal("0.09"))
            );
        }

        // Terceira faixa: 12%.
        if (base.compareTo(limite2) > 0) {
            BigDecimal faixa3 = base.min(limite3).subtract(limite2);

            total = total.add(
                    faixa3.multiply(new BigDecimal("0.12"))
            );
        }

        // Quarta faixa: 14%.
        if (base.compareTo(limite3) > 0) {
            BigDecimal faixa4 = base.subtract(limite3);

            total = total.add(
                    faixa4.multiply(new BigDecimal("0.14"))
            );
        }

        return dinheiro(total);
    }

    private BigDecimal calcularIrrf(
            BigDecimal rendimentoTributavel,
            BigDecimal base) {

        BigDecimal imposto;

        if (base.compareTo(new BigDecimal("2428.80")) <= 0) {
            imposto = ZERO;

        } else if (base.compareTo(new BigDecimal("2826.65")) <= 0) {
            imposto = base.multiply(new BigDecimal("0.075"))
                    .subtract(new BigDecimal("182.16"));

        } else if (base.compareTo(new BigDecimal("3751.05")) <= 0) {
            imposto = base.multiply(new BigDecimal("0.15"))
                    .subtract(new BigDecimal("394.16"));

        } else if (base.compareTo(new BigDecimal("4664.68")) <= 0) {
            imposto = base.multiply(new BigDecimal("0.225"))
                    .subtract(new BigDecimal("675.49"));

        } else {
            imposto = base.multiply(new BigDecimal("0.275"))
                    .subtract(new BigDecimal("908.73"));
        }

        imposto = dinheiro(imposto.max(ZERO));

        BigDecimal reducao = ZERO;

        if (rendimentoTributavel.compareTo(
                new BigDecimal("5000.00")) <= 0) {

            reducao = imposto.min(
                    new BigDecimal("312.89")
            );

        } else if (rendimentoTributavel.compareTo(
                new BigDecimal("7350.00")) <= 0) {

            reducao = new BigDecimal("978.62")
                    .subtract(
                            rendimentoTributavel.multiply(
                                    new BigDecimal("0.133145")
                            )
                    )
                    .max(ZERO);

            reducao = dinheiro(reducao.min(imposto));
        }

        return dinheiro(
                imposto.subtract(reducao).max(ZERO)
        );
    }
}
