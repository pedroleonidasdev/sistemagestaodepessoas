
package br.com.gestaodp.folha;

import br.com.gestaodp.empresa.Empresa;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class RelatorioFolhaPagamentoService {

    private final FolhaPagamentoRepository folhaRepository;
    private final ItemFolhaPagamentoRepository itemRepository;

    @SuppressWarnings("deprecation")
private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public RelatorioFolhaPagamentoService(
            FolhaPagamentoRepository folhaRepository,
            ItemFolhaPagamentoRepository itemRepository) {
        this.folhaRepository = folhaRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long empresaId, Long folhaId) {
        FolhaPagamento folha = folhaRepository
                .findByIdAndEmpresaId(folhaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Folha de pagamento não encontrada para esta empresa."));

        Empresa empresa = folha.getEmpresa();

        List<ItemFolhaPagamento> itens =
                itemRepository.findAllByFolhaId(folhaId);

        if (itens.isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível gerar o PDF de uma folha sem funcionários.");
        }

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
            PdfWriter.getInstance(documento, saida);
            documento.open();

            Font titulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 16, Font.BOLD);
            Font subtitulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 11, Font.BOLD);
            Font normal = FontFactory.getFont(
                    FontFactory.HELVETICA, 9, Font.NORMAL);

            Paragraph cabecalho = new Paragraph(
                    "DEMONSTRATIVO DA FOLHA DE PAGAMENTO", titulo);
            cabecalho.setAlignment(Element.ALIGN_CENTER);
            documento.add(cabecalho);
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph(
                    "Razão social: " + texto(empresa.getRazaoSocial()), normal));
            documento.add(new Paragraph(
                    "Nome fantasia: " + texto(empresa.getNomeFantasia()), normal));
            documento.add(new Paragraph(
                    "CNPJ: " + texto(empresa.getCnpj()), normal));
            documento.add(new Paragraph(
                    "Competência: " + folha.getCompetencia()
                            + "    Status: " + folha.getStatus(), normal));
            documento.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(11);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{
                    2.6f, 1.3f, 1.1f, 1.1f, 1.1f,
                    1.2f, 1.2f, 1.1f, 1.1f, 1.2f, 1.4f
            });

            String[] colunas = {
                    "Funcionário", "Salário-base", "Horas extras",
                    "Adicionais", "Comissões", "Outros proventos",
                    "Faltas e VT", "INSS", "IRRF", "Outros descontos",
                    "Salário líquido"
            };

            for (String coluna : colunas) {
                adicionarCelula(tabela, coluna, true);
            }

            BigDecimal totalSalarioBase = BigDecimal.ZERO;
            BigDecimal totalHorasExtras = BigDecimal.ZERO;
            BigDecimal totalAdicionais = BigDecimal.ZERO;
            BigDecimal totalComissoes = BigDecimal.ZERO;
            BigDecimal totalOutrosProventos = BigDecimal.ZERO;
            BigDecimal totalProventos = BigDecimal.ZERO;
            BigDecimal totalFaltasVt = BigDecimal.ZERO;
            BigDecimal totalInss = BigDecimal.ZERO;
            BigDecimal totalIrrf = BigDecimal.ZERO;
            BigDecimal totalOutrosDescontos = BigDecimal.ZERO;
            BigDecimal totalDescontos = BigDecimal.ZERO;
            BigDecimal totalFgts = BigDecimal.ZERO;
            BigDecimal totalLiquido = BigDecimal.ZERO;

            for (ItemFolhaPagamento item : itens) {
                adicionarCelula(
                        tabela, texto(item.getFuncionario().getNomeCompleto()), false);
                adicionarCelula(tabela, formatar(item.getSalarioBase()), false);
                adicionarCelula(tabela, formatar(item.getHorasExtras()), false);
                adicionarCelula(tabela, formatar(item.getAdicionais()), false);
                adicionarCelula(tabela, formatar(item.getComissoes()), false);
                adicionarCelula(tabela, formatar(item.getOutrosProventos()), false);
                adicionarCelula(tabela, formatar(
                        somar(item.getDescontoFaltas(),
                                item.getDescontoValeTransporte())), false);
                adicionarCelula(tabela, formatar(item.getValorInss()), false);
                adicionarCelula(tabela, formatar(item.getValorIrrf()), false);
                adicionarCelula(tabela, formatar(item.getOutrosDescontos()), false);
                adicionarCelula(tabela, formatar(item.getSalarioLiquido()), false);

                totalSalarioBase = somar(totalSalarioBase, item.getSalarioBase());
                totalHorasExtras = somar(totalHorasExtras, item.getHorasExtras());
                totalAdicionais = somar(totalAdicionais, item.getAdicionais());
                totalComissoes = somar(totalComissoes, item.getComissoes());
                totalOutrosProventos = somar(
                        totalOutrosProventos, item.getOutrosProventos());
                totalProventos = somar(totalProventos, item.getTotalProventos());
                totalFaltasVt = somar(totalFaltasVt,
                        somar(item.getDescontoFaltas(),
                                item.getDescontoValeTransporte()));
                totalInss = somar(totalInss, item.getValorInss());
                totalIrrf = somar(totalIrrf, item.getValorIrrf());
                totalOutrosDescontos = somar(
                        totalOutrosDescontos, item.getOutrosDescontos());
                totalDescontos = somar(totalDescontos, item.getTotalDescontos());
                totalFgts = somar(totalFgts, item.getValorFgts());
                totalLiquido = somar(totalLiquido, item.getSalarioLiquido());
            }

            documento.add(tabela);
            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("RESUMO FINANCEIRO", subtitulo));
            documento.add(new Paragraph(
                    "Total de salários-base: " + formatar(totalSalarioBase), normal));
            documento.add(new Paragraph(
                    "Total de horas extras: " + formatar(totalHorasExtras), normal));
            documento.add(new Paragraph(
                    "Total de adicionais: " + formatar(totalAdicionais), normal));
            documento.add(new Paragraph(
                    "Total de comissões: " + formatar(totalComissoes), normal));
            documento.add(new Paragraph(
                    "Total de outros proventos: "
                            + formatar(totalOutrosProventos), normal));
            documento.add(new Paragraph(
                    "Total de proventos: " + formatar(totalProventos), normal));
            documento.add(new Paragraph(
                    "Total de faltas e vale-transporte: "
                            + formatar(totalFaltasVt), normal));
            documento.add(new Paragraph(
                    "Total de INSS: " + formatar(totalInss), normal));
            documento.add(new Paragraph(
                    "Total de IRRF: " + formatar(totalIrrf), normal));
            documento.add(new Paragraph(
                    "Total de outros descontos: "
                            + formatar(totalOutrosDescontos), normal));
            documento.add(new Paragraph(
                    "Total de descontos: " + formatar(totalDescontos), normal));
            documento.add(new Paragraph(
                    "Total de FGTS (informativo): " + formatar(totalFgts), normal));
            documento.add(new Paragraph(
                    "TOTAL LÍQUIDO DA FOLHA: " + formatar(totalLiquido), subtitulo));

            documento.close();
            return saida.toByteArray();

        } catch (DocumentException e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o PDF da folha de pagamento.", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(
                    "Erro ao finalizar o arquivo PDF.", e);
        }
    }

    private void adicionarCelula(
            PdfPTable tabela, String texto, boolean cabecalho) {

        PdfPCell celula = new PdfPCell(new Phrase(texto(texto)));
        celula.setPadding(5);

        if (cabecalho) {
            celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        }

        tabela.addCell(celula);
    }

    private String formatar(BigDecimal valor) {
        return moeda.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private BigDecimal somar(BigDecimal total, BigDecimal valor) {
        return total.add(valor == null ? BigDecimal.ZERO : valor);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor;
    }
}
