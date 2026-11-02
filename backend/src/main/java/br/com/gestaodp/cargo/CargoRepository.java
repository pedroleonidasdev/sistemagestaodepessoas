
package br.com.gestaodp.cargo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    @EntityGraph(attributePaths = "empresa")
    List<Cargo> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

    @EntityGraph(attributePaths = "empresa")
    Optional<Cargo> findByIdAndEmpresaId(Long id, Long empresaId);

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
