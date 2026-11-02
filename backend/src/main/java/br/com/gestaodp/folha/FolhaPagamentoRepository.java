package br.com.gestaodp.folha;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolhaPagamentoRepository
        extends JpaRepository<FolhaPagamento, Long> {

    List<FolhaPagamento> findAllByEmpresaIdOrderByCompetenciaDesc(
            Long empresaId
    );

    Optional<FolhaPagamento> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    boolean existsByEmpresaIdAndCompetencia(
            Long empresaId,
            String competencia
    );

}