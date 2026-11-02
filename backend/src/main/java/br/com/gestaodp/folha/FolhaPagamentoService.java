
package br.com.gestaodp.folha;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.empresa.EmpresaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class FolhaPagamentoService {

    private static final Pattern PADRAO_COMPETENCIA =
            Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");

    private final FolhaPagamentoRepository folhaRepository;
    private final EmpresaRepository empresaRepository;

    public FolhaPagamentoService(
            FolhaPagamentoRepository folhaRepository,
            EmpresaRepository empresaRepository) {

        this.folhaRepository = folhaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public FolhaPagamento abrir(
            Long empresaId,
            AbrirFolhaRequest request) {

        if (request == null
                || request.getCompetencia() == null
                || !PADRAO_COMPETENCIA.matcher(
                        request.getCompetencia().trim()).matches()) {

            throw new IllegalArgumentException(
                    "A competência deve estar no formato AAAA-MM, "
                    + "por exemplo, 2026-10."
            );
        }

        String competencia = request.getCompetencia().trim();

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa não encontrada."
                ));

        if (folhaRepository.existsByEmpresaIdAndCompetencia(
                empresaId, competencia)) {

            throw new IllegalArgumentException(
                    "Já existe uma folha de pagamento para esta "
                    + "empresa e competência."
            );
        }

        FolhaPagamento folha = new FolhaPagamento();
        folha.setEmpresa(empresa);
        folha.setCompetencia(competencia);

        try {
            FolhaPagamento folhaSalva =
                    folhaRepository.saveAndFlush(folha);

            return folhaRepository.findByIdAndEmpresaId(
                    folhaSalva.getId(),
                    empresaId
            ).orElseThrow(() -> new IllegalStateException(
                    "A folha foi salva, mas não pôde ser consultada."
            ));

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível abrir a folha. "
                    + "Verifique se já existe uma folha para esta competência."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<FolhaPagamento> listarPorEmpresa(Long empresaId) {
        validarEmpresa(empresaId);

        return folhaRepository
                .findAllByEmpresaIdOrderByCompetenciaDesc(empresaId);
    }

    @Transactional(readOnly = true)
    public FolhaPagamento buscarPorId(
            Long empresaId,
            Long folhaId) {

        validarEmpresa(empresaId);

        return folhaRepository
                .findByIdAndEmpresaId(folhaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Folha de pagamento não encontrada nesta empresa."
                ));
    }


@Transactional
public FolhaPagamento salvar(FolhaPagamento folha) {
    if (folha == null || folha.getId() == null) {
        throw new IllegalArgumentException(
                "A folha de pagamento é inválida."
        );
    }

    FolhaPagamento existente = folhaRepository
            .findByIdAndEmpresaId(
                    folha.getId(),
                    folha.getEmpresa().getId()
            )
            .orElseThrow(() -> new IllegalArgumentException(
                    "Folha de pagamento não encontrada."
            ));

    if ("FECHADA".equals(existente.getStatus())) {
        throw new IllegalStateException(
                "Não é permitido alterar uma folha fechada."
        );
    }

    return folhaRepository.save(folha);
}


    private void validarEmpresa(Long empresaId) {
        if (empresaId == null
                || !empresaRepository.existsById(empresaId)) {

            throw new IllegalArgumentException(
                    "Empresa não encontrada."
            );
        }
    }

@Transactional
public void fechar(Long empresaId, Long folhaId) {
    FolhaPagamento folha = folhaRepository
            .findByIdAndEmpresaId(folhaId, empresaId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Folha de pagamento não encontrada para esta empresa."));

    if (!"CALCULADA".equals(folha.getStatus())) {
        throw new IllegalStateException(
                "Somente folhas calculadas podem ser fechadas.");
    }

    folha.setStatus("FECHADA");
    folhaRepository.saveAndFlush(folha);
}

}
