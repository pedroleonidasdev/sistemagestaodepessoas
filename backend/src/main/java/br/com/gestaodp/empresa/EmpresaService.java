
package br.com.gestaodp.empresa;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Empresa cadastrar(Empresa empresa) {
        validarDados(empresa);

        String cnpj = normalizarCnpj(empresa.getCnpj());

        if (empresaRepository.existsByCnpj(cnpj)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe uma empresa cadastrada com este CNPJ."
            );
        }

        empresa.setCnpj(cnpj);
        empresa.setAtivo(true);

        return empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Empresa não encontrada."
            ));
    }

    public Empresa atualizar(Long id, Empresa dados) {
        validarDados(dados);

        Empresa empresa = buscarPorId(id);
        String cnpj = normalizarCnpj(dados.getCnpj());

        empresaRepository.findByCnpj(cnpj)
            .filter(existente -> !existente.getId().equals(id))
            .ifPresent(existente -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O CNPJ informado já pertence a outra empresa."
                );
            });

        empresa.setRazaoSocial(dados.getRazaoSocial().trim());
        empresa.setNomeFantasia(normalizarNomeFantasia(dados.getNomeFantasia()));
        empresa.setCnpj(cnpj);

        if (dados.getAtivo() != null) {
            empresa.setAtivo(dados.getAtivo());
        }

        return empresaRepository.save(empresa);
    }

    public Empresa alterarStatus(Long id, boolean ativo) {
        Empresa empresa = buscarPorId(id);
        empresa.setAtivo(ativo);
        return empresaRepository.save(empresa);
    }

    private void validarDados(Empresa empresa) {
        if (empresa == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Os dados da empresa são obrigatórios."
            );
        }

        if (empresa.getRazaoSocial() == null
                || empresa.getRazaoSocial().isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A razão social é obrigatória."
            );
        }

        if (empresa.getRazaoSocial().trim().length() > 200) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A razão social deve ter no máximo 200 caracteres."
            );
        }

        if (empresa.getNomeFantasia() != null
                && empresa.getNomeFantasia().trim().length() > 200) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O nome fantasia deve ter no máximo 200 caracteres."
            );
        }

        String cnpj = normalizarCnpj(empresa.getCnpj());

        if (cnpj.length() != 14 || !cnpj.matches("\\d{14}")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O CNPJ deve conter 14 dígitos numéricos."
            );
        }

        if (cnpj.matches("(\\d)\\1{13}") || !cnpjValido(cnpj)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O CNPJ informado é inválido."
            );
        }
    }

    private String normalizarCnpj(String cnpj) {
        if (cnpj == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O CNPJ é obrigatório."
            );
        }

        return cnpj.replaceAll("\\D", "");
    }

    private String normalizarNomeFantasia(String nomeFantasia) {
        if (nomeFantasia == null || nomeFantasia.isBlank()) {
            return null;
        }

        return nomeFantasia.trim();
    }

    private boolean cnpjValido(String cnpj) {
        int[] pesosPrimeiroDigito = {
            5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int[] pesosSegundoDigito = {
            6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int soma = 0;

        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i))
                * pesosPrimeiroDigito[i];
        }

        int resto = soma % 11;
        int primeiroDigito = resto < 2 ? 0 : 11 - resto;

        if (Character.getNumericValue(cnpj.charAt(12)) != primeiroDigito) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i))
                * pesosSegundoDigito[i];
        }

        resto = soma % 11;
        int segundoDigito = resto < 2 ? 0 : 11 - resto;

        return Character.getNumericValue(cnpj.charAt(13)) == segundoDigito;
    }
}
