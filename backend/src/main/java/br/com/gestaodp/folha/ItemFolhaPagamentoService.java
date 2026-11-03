
package br.com.gestaodp.folha;

import br.com.gestaodp.funcionario.Funcionario;
import br.com.gestaodp.funcionario.FuncionarioRepository;
import br.com.gestaodp.funcionario.SituacaoFuncionario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ItemFolhaPagamentoService {

    private final FolhaPagamentoService folhaService;
    private final ItemFolhaPagamentoRepository itemRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CalculoFolhaPagamentoService calculoService;

    public ItemFolhaPagamentoService(
            FolhaPagamentoService folhaService,
            ItemFolhaPagamentoRepository itemRepository,
            FuncionarioRepository funcionarioRepository,
            CalculoFolhaPagamentoService calculoService) {

        this.folhaService = folhaService;
        this.itemRepository = itemRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.calculoService = calculoService;
    }

    @Transactional
    public ItemFolhaPagamento adicionar(
            Long empresaId,
            Long folhaId,
            Long funcionarioId) {

        FolhaPagamento folha =
                folhaService.buscarPorId(empresaId, folhaId);

        if (!"ABERTA".equals(folha.getStatus())) {
            throw new IllegalStateException(
                    "Só é possível adicionar funcionários a uma folha aberta."
            );
        }

        Funcionario funcionario = funcionarioRepository
                .findByIdAndEmpresaId(funcionarioId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funcionário não encontrado nesta empresa."
                ));

        // Impede a inclusão de funcionários afastados ou inativos.
        if (funcionario.getSituacao() != SituacaoFuncionario.ATIVO) {
            throw new IllegalArgumentException(
                    "Só é possível incluir funcionários ativos na folha."
            );
        }

        if (itemRepository.existsByFolhaIdAndFuncionarioId(
                folhaId, funcionarioId)) {

            throw new IllegalArgumentException(
                    "Este funcionário já foi incluído nesta folha."
            );
        }

        BigDecimal salarioBase = funcionario.getSalarioBase();

        if (salarioBase == null
                || salarioBase.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "O salário-base do funcionário é inválido."
            );
        }

        ItemFolhaPagamento item = new ItemFolhaPagamento();

        item.setFolha(folha);
        item.setFuncionario(funcionario);
        item.setSalarioBase(
                salarioBase.setScale(2, RoundingMode.HALF_UP)
        );

        item.setNumeroDependentes(
                funcionario.getNumeroDependentes() == null
                        ? 0
                        : funcionario.getNumeroDependentes()
        );
        item.setValeTransporte(
                funcionario.getValeTransporteMensal() == null
                        ? BigDecimal.ZERO.setScale(2)
                        : funcionario.getValeTransporteMensal()
        );
        item.setDiasFaltas(0);
        item.setDescontoFaltas(BigDecimal.ZERO.setScale(2));
        item.setDescontoValeTransporte(BigDecimal.ZERO.setScale(2));

        item.setHorasExtras(BigDecimal.ZERO.setScale(2));
        item.setAdicionais(BigDecimal.ZERO.setScale(2));
        item.setComissoes(BigDecimal.ZERO.setScale(2));
        item.setOutrosProventos(BigDecimal.ZERO.setScale(2));
        item.setOutrosDescontos(BigDecimal.ZERO.setScale(2));

        item.setTotalProventos(item.getSalarioBase());
        item.setBaseInss(BigDecimal.ZERO.setScale(2));
        item.setValorInss(BigDecimal.ZERO.setScale(2));
        item.setBaseIrrf(BigDecimal.ZERO.setScale(2));
        item.setValorIrrf(BigDecimal.ZERO.setScale(2));
        item.setValorFgts(BigDecimal.ZERO.setScale(2));
        item.setTotalDescontos(BigDecimal.ZERO.setScale(2));
        item.setSalarioLiquido(item.getSalarioBase());

        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ItemFolhaPagamento> listar(
            Long empresaId,
            Long folhaId) {

        folhaService.buscarPorId(empresaId, folhaId);

        return itemRepository.findAllByFolhaId(folhaId);
    }

    @Transactional
    public void calcularFolha(Long empresaId, Long folhaId) {

        FolhaPagamento folha =
                folhaService.buscarPorId(empresaId, folhaId);

        if ("FECHADA".equals(folha.getStatus())) {
            throw new IllegalStateException(
                    "Não é possível calcular ou alterar uma folha fechada."
            );
        }

        if (!"ABERTA".equals(folha.getStatus())
                && !"CALCULADA".equals(folha.getStatus())) {

            throw new IllegalStateException(
                    "O status da folha não permite o cálculo."
            );
        }

        List<ItemFolhaPagamento> itens =
                itemRepository.findAllByFolhaId(folhaId);

        if (itens.isEmpty()) {
            throw new IllegalArgumentException(
                    "Adicione pelo menos um funcionário antes de calcular."
            );
        }

        for (ItemFolhaPagamento item : itens) {
            calculoService.calcular(item);
        }

        itemRepository.saveAll(itens);

        folha.setStatus("CALCULADA");
        folhaService.salvar(folha);
    }

    @Transactional
    public ItemFolhaPagamento editar(
            Long empresaId,
            Long folhaId,
            Long itemId,
            EditarItemFolhaRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Informe os lançamentos para edição."
            );
        }

        FolhaPagamento folha =
                folhaService.buscarPorId(empresaId, folhaId);

        if (!"ABERTA".equals(folha.getStatus())) {
            throw new IllegalStateException(
                    "Só é possível editar itens de uma folha aberta."
            );
        }

        ItemFolhaPagamento item = itemRepository
                .findByIdAndFolhaId(itemId, folhaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item não encontrado nesta folha."
                ));

        BigDecimal horasExtras = validarValor(
                request.getHorasExtras(), "Horas extras");
        BigDecimal adicionais = validarValor(
                request.getAdicionais(), "Adicionais");
        BigDecimal comissoes = validarValor(
                request.getComissoes(), "Comissões");
        BigDecimal outrosProventos = validarValor(
                request.getOutrosProventos(), "Outros proventos");
        BigDecimal outrosDescontos = validarValor(
                request.getOutrosDescontos(), "Outros descontos");

        item.setHorasExtras(horasExtras);
        item.setAdicionais(adicionais);
        item.setComissoes(comissoes);
        item.setOutrosProventos(outrosProventos);
        item.setOutrosDescontos(outrosDescontos);

        // Campos novos: se não vierem na requisição, mantém o valor atual.
        if (request.getDiasFaltas() != null) {
            if (request.getDiasFaltas() < 0 || request.getDiasFaltas() > 30) {
                throw new IllegalArgumentException(
                        "Os dias de falta devem ficar entre 0 e 30."
                );
            }

            item.setDiasFaltas(request.getDiasFaltas());
        }

        if (request.getNumeroDependentes() != null) {
            if (request.getNumeroDependentes() < 0
                    || request.getNumeroDependentes() > 30) {
                throw new IllegalArgumentException(
                        "O número de dependentes deve ficar entre 0 e 30."
                );
            }

            item.setNumeroDependentes(request.getNumeroDependentes());
        }

        if (request.getValeTransporte() != null) {
            item.setValeTransporte(
                    validarValor(request.getValeTransporte(),
                            "Vale-transporte")
            );
        }

        calculoService.calcular(item);

        return itemRepository.save(item);
    }

    @Transactional
    public void excluir(
            Long empresaId,
            Long folhaId,
            Long itemId) {

        FolhaPagamento folha =
                folhaService.buscarPorId(empresaId, folhaId);

        if (!"ABERTA".equals(folha.getStatus())) {
            throw new IllegalStateException(
                    "Só é possível excluir itens de uma folha aberta."
            );
        }

        ItemFolhaPagamento item = itemRepository
                .findByIdAndFolhaId(itemId, folhaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item não encontrado nesta folha."
                ));

        itemRepository.delete(item);
    }

    @Transactional
    public void excluirFolha(Long empresaId, Long folhaId) {

        FolhaPagamento folha =
                folhaService.buscarPorId(empresaId, folhaId);

        if ("FECHADA".equals(folha.getStatus())) {
            throw new IllegalStateException(
                    "Não é possível excluir uma folha fechada."
            );
        }

        List<ItemFolhaPagamento> itens =
                itemRepository.findAllByFolhaId(folhaId);

        itemRepository.deleteAll(itens);
        itemRepository.flush();

        folhaService.excluir(empresaId, folhaId);
    }

    private BigDecimal validarValor(
            BigDecimal valor,
            String campo) {

        if (valor == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    campo + " não pode ser negativo."
            );
        }

        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
