package br.com.gestaodp.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 400,
                "erro", "Requisição inválida",
                "mensagem", Objects.requireNonNullElse(
                        ex.getMessage(),
                        "Operação inválida."
                )
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarIllegalState(
            IllegalStateException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 409,
                "erro", "Operação não permitida",
                "mensagem", Objects.requireNonNullElse(
                        ex.getMessage(),
                        "Operação inválida."
                )
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> tratarResponseStatus(
            ResponseStatusException ex) {

        int codigo = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(codigo);

        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", codigo,
                "erro", status != null
                        ? status.getReasonPhrase()
                        : "Erro",
                "mensagem", Objects.requireNonNullElse(
                        ex.getReason(),
                        "Não foi possível concluir a operação."
                )
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarGeral(
            Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 500,
                "erro", "Erro interno",
                "mensagem", Objects.requireNonNullElse(
                        ex.getMessage(),
                        "Ocorreu um erro inesperado."
                )
        ));
    }
}