package br.com.gestaodp.folha;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.funcionario.Funcionario;
import com.lowagie.text.Document;
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
import java.util.Locale;

@Service
public class HoleriteFuncionarioService {

    private final FolhaPagamentoRepository folhaRepository;
    private final ItemFolhaPagamentoRepository itemRepository;

    @SuppressWarnings("deprecation")
    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public HoleriteFuncionarioService(
            FolhaPagamentoRepository folhaRepository,
            ItemFolhaPagamentoRepository itemRepository) {
        this.folhaRepository = folhaRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long empresaId, Long folhaId, Long itemId) {

        FolhaPagamento folha = folhaRepository
                .findByIdAndEmpresaId(folhaId, empresaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Folha de pagamento não encontrada."));

        ItemFolhaPagamento item = itemRepository
                .findByIdAndFolhaId(itemId, folhaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário não encontrado nesta folha."));

        Empresa empresa = folha.getEmpresa();
        Funcionario funcionario = item.getFuncionario();

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            Document documento = new Document(PageSize.A4, 40, 40, 35, 35);
            PdfWriter.getInstance(documento, saida);
            documento.open();

            Font titulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 16, Font.BOLD);

            Font subtitulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 11, Font.BOLD);

            Font normal = FontFactory.getFont(
                    FontFactory.HELVETICA, 10);

            Paragraph cabecalho = new Paragraph(
                    "DEMONSTRATIVO DE PAGAMENTO", titulo);
            cabecalho.setAlignment(Element.ALIGN_CENTER);
            cabecalho.setSpacingAfter(18);
            documento.add(cabecalho);

            documento.add(new Paragraph(
                    "Razão social: " + texto(empresa.getRazaoSocial()), normal));
            documento.add(new Paragraph(
                    "Nome fantasia: " + texto(empresa.getNomeFantasia()), normal));
            documento.add(new Paragraph(
                    "CNPJ: " + texto(empresa.getCnpj()), normal));
            documento.add(new Paragraph(
                    "Competência: " + folha.getCompetencia(), normal));
            documento.add(new Paragraph(
                    "Status da folha: " + folha.getStatus(), normal));

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("DADOS DO FUNCIONÁRIO", subtitulo));

            documento.add(new Paragraph(
                    "Nome: " + texto(funcionario.getNomeCompleto()), normal));
            documento.add(new Paragraph(
                    "CPF: " + texto(funcionario.getCpf()), normal));
            documento.add(new Paragraph(
                    "Matrícula: " + texto(funcionario.getMatricula()), normal));

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("PROVENTOS", subtitulo));

            PdfPTable proventos = new PdfPTable(2);
            proventos.setWidthPercentage(100);
            proventos.setWidths(new float[]{3, 1.5f});

            adicionarLinha(proventos, "Descrição", "Valor", true);
            adicionarLinha(proventos, "Salário-base",
                    formatar(item.getSalarioBase()), false);
            adicionarLinha(proventos, "Horas extras",
                    formatar(item.getHorasExtras()), false);
            adicionarLinha(proventos, "Adicionais",
                    formatar(item.getAdicionais()), false);
            adicionarLinha(proventos, "Comissões",
                    formatar(item.getComissoes()), false);
            adicionarLinha(proventos, "Outros proventos",
                    formatar(item.getOutrosProventos()), false);
            adicionarLinha(proventos, "TOTAL DE PROVENTOS",
                    formatar(item.getTotalProventos()), true);

            documento.add(proventos);

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("DESCONTOS", subtitulo));

            PdfPTable descontos = new PdfPTable(2);
            descontos.setWidthPercentage(100);
            descontos.setWidths(new float[]{3, 1.5f});

            adicionarLinha(descontos, "Descrição", "Valor", true);
            adicionarLinha(descontos, "INSS",
                    formatar(item.getValorInss()), false);
            adicionarLinha(descontos, "IRRF",
                    formatar(item.getValorIrrf()), false);
            adicionarLinha(descontos, "Outros descontos",
                    formatar(item.getOutrosDescontos()), false);
            adicionarLinha(descontos, "TOTAL DE DESCONTOS",
                    formatar(item.getTotalDescontos()), true);

            documento.add(descontos);

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("RESUMO", subtitulo));

            documento.add(new Paragraph(
                    "Base de INSS: " + formatar(item.getBaseInss()), normal));
            documento.add(new Paragraph(
                    "Base de IRRF: " + formatar(item.getBaseIrrf()), normal));
            documento.add(new Paragraph(
                    "FGTS (informativo): " + formatar(item.getValorFgts()), normal));

            Paragraph liquido = new Paragraph(
                    "SALÁRIO LÍQUIDO: " + formatar(item.getSalarioLiquido()),
                    subtitulo);
            liquido.setSpacingBefore(10);
            documento.add(liquido);

            documento.close();
            return saida.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o holerite em PDF.", e);
        }
    }

    private void adicionarLinha(
            PdfPTable tabela, String descricao, String valor,
            boolean destaque) {

        Font fonte = FontFactory.getFont(
                FontFactory.HELVETICA,
                9,
                destaque ? Font.BOLD : Font.NORMAL);

        PdfPCell celulaDescricao = new PdfPCell(new Phrase(descricao, fonte));
        celulaDescricao.setPadding(6);

        PdfPCell celulaValor = new PdfPCell(new Phrase(valor, fonte));
        celulaValor.setPadding(6);
        celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabela.addCell(celulaDescricao);
        tabela.addCell(celulaValor);
    }

    private String formatar(BigDecimal valor) {
        return moeda.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank()
                ? "Não informado"
                : valor;
    }
}