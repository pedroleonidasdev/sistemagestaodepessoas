
package br.com.gestaodp.departamento;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.empresa.EmpresaRepository;

@Service
@Transactional
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final EmpresaRepository empresaRepository;

    public DepartamentoService(
            DepartamentoRepository departamentoRepository,
            EmpresaRepository empresaRepository) {
        this.departamentoRepository = departamentoRepository;
        this.empresaRepository = empresaRepository;
    }

    public Departamento criar(
            Long empresaId,
            String nome,
            String descricao) {

        Empresa empresa = buscarEmpresa(empresaId);
        String nomeValidado = validarNome(nome);

        if (departamentoRepository.existsByEmpresaIdAndNomeIgnoreCase(
                empresaId, nomeValidado)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe um departamento com esse nome nesta empresa."
            );
        }

        Departamento departamento = new Departamento();
        departamento.setEmpresa(empresa);
        departamento.setNome(nomeValidado);
        departamento.setDescricao(normalizarDescricao(descricao));
        departamento.setAtivo(true);

        return departamentoRepository.save(departamento);
    }

    @Transactional(readOnly = true)
    public List<Departamento> listar(Long empresaId) {
        buscarEmpresa(empresaId);
        return departamentoRepository
            .findAllByEmpresaIdOrderByNomeAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public Departamento buscarPorId(Long empresaId, Long departamentoId) {
        buscarEmpresa(empresaId);

        return departamentoRepository
            .findByIdAndEmpresaId(departamentoId, empresaId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Departamento não encontrado nesta empresa."
            ));
    }

    public Departamento atualizar(
            Long empresaId,
            Long departamentoId,
            String nome,
            String descricao) {

        Departamento departamento = buscarPorId(empresaId, departamentoId);
        String nomeValidado = validarNome(nome);

        if (departamentoRepository
                .existsByEmpresaIdAndNomeIgnoreCaseAndIdNot(
                    empresaId, nomeValidado, departamentoId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe outro departamento com esse nome nesta empresa."
            );
        }

        departamento.setNome(nomeValidado);
        departamento.setDescricao(normalizarDescricao(descricao));

        return departamentoRepository.save(departamento);
    }

    public Departamento alterarSituacao(
            Long empresaId,
            Long departamentoId,
            boolean ativo) {

        Departamento departamento = buscarPorId(empresaId, departamentoId);
        departamento.setAtivo(ativo);

        return departamentoRepository.save(departamento);
    }

    private Empresa buscarEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Empresa não encontrada."
            ));
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O nome do departamento é obrigatório."
            );
        }

        String nomeValidado = nome.trim();

        if (nomeValidado.length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O nome deve ter no máximo 100 caracteres."
            );
        }

        return nomeValidado;
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        String descricaoValidada = descricao.trim();

        if (descricaoValidada.length() > 500) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A descrição deve ter no máximo 500 caracteres."
            );
        }

        return descricaoValidada;
    }
}
