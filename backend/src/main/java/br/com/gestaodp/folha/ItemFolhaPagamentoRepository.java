
package br.com.gestaodp.folha;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ItemFolhaPagamentoRepository
        extends JpaRepository<ItemFolhaPagamento, Long> {

    @EntityGraph(attributePaths = {"folha", "funcionario"})
    List<ItemFolhaPagamento> findAllByFolhaId(Long folhaId);

    boolean existsByFolhaIdAndFuncionarioId(
            Long folhaId,
            Long funcionarioId
    );

 @EntityGraph(attributePaths = {"folha", "funcionario"})
Optional<ItemFolhaPagamento> findByIdAndFolhaId(
        Long id, Long folhaId);
}
