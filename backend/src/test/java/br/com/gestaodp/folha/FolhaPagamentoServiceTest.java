
package br.com.gestaodp.folha;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.empresa.EmpresaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolhaPagamentoServiceTest {

    @Mock
    private FolhaPagamentoRepository folhaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private FolhaPagamentoService folhaService;

    @SuppressWarnings("unused")
    private Empresa empresa;

    @BeforeEach
    void configurar() {
        empresa = new Empresa();
    }


@Test
void naoDeveSalvarFolhaFechada() {
    FolhaPagamento folha = mock(FolhaPagamento.class);
    Empresa empresa = mock(Empresa.class);

    when(folha.getId()).thenReturn(1L);
    when(folha.getEmpresa()).thenReturn(empresa);
    when(empresa.getId()).thenReturn(1L);

    FolhaPagamento existente = new FolhaPagamento();
    existente.setStatus("FECHADA");

    when(folhaRepository.findByIdAndEmpresaId(1L, 1L))
            .thenReturn(Optional.of(existente));

    assertThrows(
            IllegalStateException.class,
            () -> folhaService.salvar(folha)
    );

    verify(folhaRepository, never()).save(any());
}


    @Test
    void naoDeveFecharFolhaAberta() {
        FolhaPagamento folha = new FolhaPagamento();
        folha.setStatus("ABERTA");

        when(folhaRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(folha));

        assertThrows(
                IllegalStateException.class,
                () -> folhaService.fechar(1L, 1L)
        );

        verify(folhaRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveFecharFolhaCalculada() {
        FolhaPagamento folha = new FolhaPagamento();
        folha.setStatus("CALCULADA");

        when(folhaRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(folha));

        folhaService.fechar(1L, 1L);

        assertEquals("FECHADA", folha.getStatus());

        verify(folhaRepository).saveAndFlush(folha);
    }
}
