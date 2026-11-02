
package br.com.gestaodp.cargo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.empresa.EmpresaRepository;

@Service
@Transactional
public class CargoService {

    private final CargoRepository cargoRepository;
    private final EmpresaRepository empresaRepository;

    public CargoService(
            CargoRepository cargoRepository,
            EmpresaRepository empresaRepository) {
        this.cargoRepository = cargoRepository;
        this.empresaRepository = empresaRepository;
    }

    public Cargo criar(Long empresaId, String nome, String descricao) {
        Empresa empresa = buscarEmpresa(empresaId);
        String nomeValidado = validarNome(nome);

        if (cargoRepository.existsByEmpresaIdAndNomeIgnoreCase(
                empresaId, nomeValidado)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe um cargo com esse nome nesta empresa."
            );
        }

        Cargo cargo = new Cargo();
        cargo.setEmpresa(empresa);
        cargo.setNome(nomeValidado);
        cargo.setDescricao(normalizarDescricao(descricao));
        cargo.setAtivo(true);

        return cargoRepository.save(cargo);
    }

    @Transactional(readOnly = true)
    public List<Cargo> listar(Long empresaId) {
        buscarEmpresa(empresaId);
        return cargoRepository.findAllByEmpresaIdOrderByNomeAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public Cargo buscarPorId(Long empresaId, Long cargoId) {
        buscarEmpresa(empresaId);

        return cargoRepository.findByIdAndEmpresaId(cargoId, empresaId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cargo não encontrado nesta empresa."
            ));
    }

    public Cargo atualizar(
            Long empresaId,
            Long cargoId,
            String nome,
            String descricao) {

        Cargo cargo = buscarPorId(empresaId, cargoId);
        String nomeValidado = validarNome(nome);

        if (cargoRepository.existsByEmpresaIdAndNomeIgnoreCaseAndIdNot(
                empresaId, nomeValidado, cargoId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe outro cargo com esse nome nesta empresa."
            );
        }

        cargo.setNome(nomeValidado);
        cargo.setDescricao(normalizarDescricao(descricao));

        return cargoRepository.save(cargo);
    }

    public Cargo alterarSituacao(
            Long empresaId,
            Long cargoId,
            boolean ativo) {

        Cargo cargo = buscarPorId(empresaId, cargoId);
        cargo.setAtivo(ativo);

        return cargoRepository.save(cargo);
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
                "O nome do cargo é obrigatório."
            );
        }

        String validado = nome.trim();

        if (validado.length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O nome deve ter no máximo 100 caracteres."
            );
        }

        return validado;
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        String validada = descricao.trim();

        if (validada.length() > 500) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A descrição deve ter no máximo 500 caracteres."
            );
        }

        return validada;
    }
}
