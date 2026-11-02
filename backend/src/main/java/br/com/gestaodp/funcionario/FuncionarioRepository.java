
package br.com.gestaodp.funcionario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    List<Funcionario> findAllByEmpresaId(Long empresaId);

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    List<Funcionario> findAllByEmpresaIdAndSituacao(
            Long empresaId,
            SituacaoFuncionario situacao
    );

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    List<Funcionario> findAllByEmpresaIdAndDepartamentoId(
            Long empresaId,
            Long departamentoId
    );

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    List<Funcionario> findAllByEmpresaIdAndCargoId(
            Long empresaId,
            Long cargoId
    );

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    List<Funcionario> findAllByEmpresaIdAndNomeCompletoContainingIgnoreCase(
            Long empresaId,
            String nomeCompleto
    );

    @EntityGraph(attributePaths = {"empresa", "departamento", "cargo"})
    Optional<Funcionario> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    boolean existsByEmpresaIdAndMatricula(
            Long empresaId, String matricula);

    boolean existsByEmpresaIdAndCpf(
            Long empresaId, String cpf);

    boolean existsByEmpresaIdAndMatriculaAndIdNot(
            Long empresaId, String matricula, Long id);

    boolean existsByEmpresaIdAndCpfAndIdNot(
            Long empresaId, String cpf, Long id);
}
