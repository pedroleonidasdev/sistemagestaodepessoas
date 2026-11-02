
package br.com.gestaodp.departamento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository
        extends JpaRepository<Departamento, Long> {

    @EntityGraph(attributePaths = "empresa")
    List<Departamento> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

    @EntityGraph(attributePaths = "empresa")
    Optional<Departamento> findByIdAndEmpresaId(
        Long id,
        Long empresaId
    );

    boolean existsByEmpresaIdAndNomeIgnoreCase(
        Long empresaId,
        String nome
    );

    boolean existsByEmpresaIdAndNomeIgnoreCaseAndIdNot(
        Long empresaId,
        String nome,
        Long id
    );
}
