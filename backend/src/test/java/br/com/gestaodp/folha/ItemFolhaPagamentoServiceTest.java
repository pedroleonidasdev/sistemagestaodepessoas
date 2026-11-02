
package br.com.gestaodp.folha;

import br.com.gestaodp.funcionario.Funcionario;
import br.com.gestaodp.funcionario.FuncionarioRepository;
import br.com.gestaodp.funcionario.SituacaoFuncionario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemFolhaPagamentoServiceTest {

    @Mock
    private FolhaPagamentoService folhaService;

    @Mock
    private ItemFolhaPagamentoRepository itemRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private CalculoFolhaPagamentoService calculoService;

    @InjectMocks
    private ItemFolhaPagamentoService itemService;

    private FolhaPagamento folha;

    @BeforeEach
    void configurar() {
        folha = new FolhaPagamento();
        folha.setStatus("FECHADA");

        when(folhaService.buscarPorId(1L, 1L))
                .thenReturn(folha);
    }

    @Test
    void naoDeveAdicionarFuncionarioEmFolhaFechada() {
        assertThrows(
                IllegalStateException.class,
                () -> itemService.adicionar(1L, 1L, 2L)
        );

        verifyNoInteractions(funcionarioRepository);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void naoDeveRecalcularFolhaFechada() {
        assertThrows(
                IllegalStateException.class,
                () -> itemService.calcularFolha(1L, 1L)
        );

        verify(itemRepository, never()).findAllByFolhaId(any());
        verify(itemRepository, never()).saveAll(any());
        verifyNoInteractions(calculoService);
        verify(folhaService, never()).salvar(any());
    }

    @Test
    void naoDeveAdicionarFuncionarioInativo() {
        folha.setStatus("ABERTA");

        Funcionario funcionario = mock(Funcionario.class);

        when(funcionarioRepository.findByIdAndEmpresaId(2L, 1L))
                .thenReturn(Optional.of(funcionario));

        when(funcionario.getSituacao())
                .thenReturn(SituacaoFuncionario.AFASTADO);

        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.adicionar(1L, 1L, 2L)
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void naoDeveAdicionarFuncionarioDuplicado() {
        folha.setStatus("ABERTA");

        Funcionario funcionario = mock(Funcionario.class);

        when(funcionarioRepository.findByIdAndEmpresaId(2L, 1L))
                .thenReturn(Optional.of(funcionario));

        when(funcionario.getSituacao())
                .thenReturn(SituacaoFuncionario.ATIVO);

        when(itemRepository.existsByFolhaIdAndFuncionarioId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.adicionar(1L, 1L, 2L)
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void naoDeveAdicionarFuncionarioComSalarioNulo() {
        folha.setStatus("ABERTA");

        Funcionario funcionario = mock(Funcionario.class);

        when(funcionarioRepository.findByIdAndEmpresaId(2L, 1L))
                .thenReturn(Optional.of(funcionario));

        when(funcionario.getSituacao())
                .thenReturn(SituacaoFuncionario.ATIVO);

        when(itemRepository.existsByFolhaIdAndFuncionarioId(1L, 2L))
                .thenReturn(false);

        when(funcionario.getSalarioBase()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.adicionar(1L, 1L, 2L)
        );

        verify(itemRepository, never()).save(any());
    }

    @Test
    void naoDeveCalcularFolhaSemItens() {
        folha.setStatus("ABERTA");

        when(itemRepository.findAllByFolhaId(1L))
                .thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.calcularFolha(1L, 1L)
        );

        verify(itemRepository, never()).saveAll(any());
        verifyNoInteractions(calculoService);
        verify(folhaService, never()).salvar(any());
    }

    @Test
    void deveCalcularFolhaAbertaComItens() {
        folha.setStatus("ABERTA");

        ItemFolhaPagamento item = mock(ItemFolhaPagamento.class);

        when(itemRepository.findAllByFolhaId(1L))
                .thenReturn(List.of(item));

        itemService.calcularFolha(1L, 1L);

        verify(calculoService).calcular(item);
        verify(itemRepository).saveAll(List.of(item));

        assertEquals("CALCULADA", folha.getStatus());
        verify(folhaService).salvar(folha);
    }

    @Test
    void naoDeveEditarFolhaFechada() {
        EditarItemFolhaRequest request = mock(
                EditarItemFolhaRequest.class
        );

        assertThrows(
                IllegalStateException.class,
                () -> itemService.editar(1L, 1L, 1L, request)
        );

        verify(itemRepository, never()).save(any());
        verifyNoInteractions(calculoService);
    }

    @Test
    void naoDeveExcluirItemDeFolhaFechada() {
        assertThrows(
                IllegalStateException.class,
                () -> itemService.excluir(1L, 1L, 1L)
        );

        verify(itemRepository, never()).delete(any());
    }

    @Test
    void naoDeveEditarComValorNegativo() {
        folha.setStatus("ABERTA");

        ItemFolhaPagamento item = mock(ItemFolhaPagamento.class);

        EditarItemFolhaRequest request = mock(
                EditarItemFolhaRequest.class
        );

        when(itemRepository.findByIdAndFolhaId(1L, 1L))
                .thenReturn(Optional.of(item));

        when(request.getHorasExtras())
                .thenReturn(new BigDecimal("-10.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.editar(1L, 1L, 1L, request)
        );

        verify(itemRepository, never()).save(any());
    }
}
