
package br.com.gestaodp.funcionario;

import br.com.gestaodp.empresa.Empresa;
import br.com.gestaodp.empresa.EmpresaRepository;
import br.com.gestaodp.departamento.Departamento;
import br.com.gestaodp.departamento.DepartamentoRepository;
import br.com.gestaodp.cargo.Cargo;
import br.com.gestaodp.cargo.CargoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final EmpresaRepository empresaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            EmpresaRepository empresaRepository,
            DepartamentoRepository departamentoRepository,
            CargoRepository cargoRepository) {

        this.funcionarioRepository = funcionarioRepository;
        this.empresaRepository = empresaRepository;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
    }

    @Transactional
    public Funcionario cadastrar(Long empresaId, Funcionario funcionario) {

        Empresa empresa = buscarEmpresa(empresaId);

        validarFuncionario(funcionario, empresaId, null);

        funcionario.setEmpresa(empresa);

        Funcionario salvo = funcionarioRepository.saveAndFlush(funcionario);

        return funcionarioRepository
                .findByIdAndEmpresaId(salvo.getId(), empresaId)
                .orElseThrow(() -> new IllegalStateException(
                        "Não foi possível recuperar o funcionário cadastrado."
                ));
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarPorEmpresa(Long empresaId) {

        buscarEmpresa(empresaId);

        return funcionarioRepository.findAllByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Funcionario buscarPorId(Long empresaId, Long funcionarioId) {

        buscarEmpresa(empresaId);

        return funcionarioRepository
                .findByIdAndEmpresaId(funcionarioId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funcionário não encontrado nesta empresa."
                ));
    }

    @Transactional
    public Funcionario atualizar(
            Long empresaId,
            Long funcionarioId,
            Funcionario dadosAtualizados) {

        Funcionario funcionario = buscarPorId(empresaId, funcionarioId);

        validarFuncionario(dadosAtualizados, empresaId, funcionarioId);

        funcionario.setMatricula(dadosAtualizados.getMatricula());
        funcionario.setNomeCompleto(dadosAtualizados.getNomeCompleto());
        funcionario.setCpf(dadosAtualizados.getCpf());
        funcionario.setSalarioBase(dadosAtualizados.getSalarioBase());
        funcionario.setDataAdmissao(dadosAtualizados.getDataAdmissao());
        funcionario.setSituacao(dadosAtualizados.getSituacao());

        funcionario.setDepartamento(
                dadosAtualizados.getDepartamento()
        );

        funcionario.setCargo(
                dadosAtualizados.getCargo()
        );

        Funcionario salvo = funcionarioRepository.saveAndFlush(funcionario);

        return funcionarioRepository
                .findByIdAndEmpresaId(salvo.getId(), empresaId)
                .orElseThrow(() -> new IllegalStateException(
                        "Não foi possível recuperar o funcionário atualizado."
                ));
    }

    @Transactional
    public Funcionario alterarSituacao(
            Long empresaId,
            Long funcionarioId,
            SituacaoFuncionario situacao) {

        if (situacao == null) {
            throw new IllegalArgumentException(
                    "A situação do funcionário deve ser informada."
            );
        }

        Funcionario funcionario = buscarPorId(empresaId, funcionarioId);

        funcionario.setSituacao(situacao);

        Funcionario salvo = funcionarioRepository.saveAndFlush(funcionario);

        return funcionarioRepository
                .findByIdAndEmpresaId(salvo.getId(), empresaId)
                .orElseThrow(() -> new IllegalStateException(
                        "Não foi possível recuperar o funcionário atualizado."
                ));
    }

    @Transactional(readOnly = true)
    public List<Funcionario> filtrar(
            Long empresaId,
            SituacaoFuncionario situacao,
            Long departamentoId,
            Long cargoId,
            String nome) {

        buscarEmpresa(empresaId);

        return funcionarioRepository.findAllByEmpresaId(empresaId)
                .stream()
                .filter(f -> situacao == null
                        || f.getSituacao() == situacao)
                .filter(f -> departamentoId == null
                        || (f.getDepartamento() != null
                        && departamentoId.equals(
                                f.getDepartamento().getId())))
                .filter(f -> cargoId == null
                        || (f.getCargo() != null
                        && cargoId.equals(
                                f.getCargo().getId())))
                .filter(f -> nome == null || nome.isBlank()
                        || (f.getNomeCompleto() != null
                        && f.getNomeCompleto()
                                .toLowerCase()
                                .contains(nome.trim().toLowerCase())))
                .toList();
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa não encontrada."
                ));
    }

    private void validarFuncionario(
            Funcionario funcionario,
            Long empresaId,
            Long funcionarioId) {

        if (funcionario == null) {
            throw new IllegalArgumentException(
                    "Os dados do funcionário devem ser informados."
            );
        }

        if (funcionario.getMatricula() == null
                || funcionario.getMatricula().isBlank()) {
            throw new IllegalArgumentException(
                    "A matrícula do funcionário é obrigatória."
            );
        }

        if (funcionario.getNomeCompleto() == null
                || funcionario.getNomeCompleto().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome completo do funcionário é obrigatório."
            );
        }

        if (funcionario.getCpf() == null
                || funcionario.getCpf().isBlank()) {
            throw new IllegalArgumentException(
                    "O CPF do funcionário é obrigatório."
            );
        }

        if (funcionario.getDataAdmissao() == null) {
            throw new IllegalArgumentException(
                    "A data de admissão é obrigatória."
            );
        }

        if (funcionario.getDataAdmissao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "A data de admissão não pode ser futura."
            );
        }

        if (funcionario.getSituacao() == null) {
            throw new IllegalArgumentException(
                    "A situação do funcionário é obrigatória."
            );
        }

        BigDecimal salarioBase = funcionario.getSalarioBase();

        if (salarioBase == null
                || salarioBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O salário-base não pode ser nulo ou negativo."
            );
        }

        String matricula = funcionario.getMatricula().trim();
        String cpf = funcionario.getCpf().replaceAll("\\D", "");

        if (!cpfValido(cpf)) {
            throw new IllegalArgumentException(
                    "O CPF informado é inválido."
            );
        }

        boolean matriculaExiste;

        if (funcionarioId == null) {
            matriculaExiste =
                    funcionarioRepository.existsByEmpresaIdAndMatricula(
                            empresaId, matricula);
        } else {
            matriculaExiste =
                    funcionarioRepository
                            .existsByEmpresaIdAndMatriculaAndIdNot(
                                    empresaId, matricula, funcionarioId);
        }

        if (matriculaExiste) {
            throw new IllegalArgumentException(
                    "Já existe um funcionário com esta matrícula na empresa."
            );
        }

        boolean cpfExiste;

        if (funcionarioId == null) {
            cpfExiste =
                    funcionarioRepository.existsByEmpresaIdAndCpf(
                            empresaId, cpf);
        } else {
            cpfExiste =
                    funcionarioRepository
                            .existsByEmpresaIdAndCpfAndIdNot(
                                    empresaId, cpf, funcionarioId);
        }

        if (cpfExiste) {
            throw new IllegalArgumentException(
                    "Já existe um funcionário com este CPF na empresa."
            );
        }

        funcionario.setMatricula(matricula);
        funcionario.setNomeCompleto(funcionario.getNomeCompleto().trim());
        funcionario.setCpf(cpf);

        validarDepartamento(funcionario, empresaId);
        validarCargo(funcionario, empresaId);
    }

    private boolean cpfValido(String cpf) {

        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        for (int posicao = 9; posicao < 11; posicao++) {
            int soma = 0;

            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }

            int digito = (soma * 10) % 11;

            if (digito == 10) {
                digito = 0;
            }

            if (digito != cpf.charAt(posicao) - '0') {
                return false;
            }
        }

        return true;
    }

    private void validarDepartamento(
            Funcionario funcionario,
            Long empresaId) {

        Departamento departamento = funcionario.getDepartamento();

        if (departamento == null || departamento.getId() == null) {
            funcionario.setDepartamento(null);
            return;
        }

        Departamento departamentoEncontrado =
                departamentoRepository
                        .findByIdAndEmpresaId(
                                departamento.getId(), empresaId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Departamento não encontrado nesta empresa."
                        ));

        if (Boolean.FALSE.equals(departamentoEncontrado.getAtivo())) {
            throw new IllegalArgumentException(
                    "Não é possível vincular um departamento inativo."
            );
        }

        funcionario.setDepartamento(departamentoEncontrado);
    }

    private void validarCargo(
            Funcionario funcionario,
            Long empresaId) {

        Cargo cargo = funcionario.getCargo();

        if (cargo == null || cargo.getId() == null) {
            funcionario.setCargo(null);
            return;
        }

        Cargo cargoEncontrado =
                cargoRepository
                        .findByIdAndEmpresaId(
                                cargo.getId(), empresaId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cargo não encontrado nesta empresa."
                        ));

        if (Boolean.FALSE.equals(cargoEncontrado.getAtivo())) {
            throw new IllegalArgumentException(
                    "Não é possível vincular um cargo inativo."
            );
        }

        funcionario.setCargo(cargoEncontrado);
    }
}
