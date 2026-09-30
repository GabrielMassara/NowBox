package com.nowbox.nowbox_api.common.handler;

import com.nowbox.nowbox_api.common.exception.ConflitoException;
import com.nowbox.nowbox_api.common.exception.CredenciaisInvalidasException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NaoEncontradoException.class)
    private ResponseEntity<String> estadoNotFoundHandler(NaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    private ResponseEntity<String> conflitoHandler(ConflitoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    private ResponseEntity<String> requisicaoInvalidaHandler(RequisicaoInvalidaException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<String> integridadeHandler(DataIntegrityViolationException exception) {
        String detalhe = exception.getMostSpecificCause().getMessage();
        String texto = detalhe == null ? "" : detalhe.toLowerCase();

        String mensagem;
        if (texto.contains("cpf")) {
            mensagem = "Já existe um registro cadastrado com este CPF.";
        } else if (texto.contains("cnpj")) {
            mensagem = "Já existe um registro cadastrado com este CNPJ.";
        } else if (texto.contains("email")) {
            mensagem = "Já existe um registro cadastrado com este e-mail.";
        } else {
            mensagem = "Não foi possível salvar: os dados conflitam com um registro existente.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(mensagem);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    private ResponseEntity<String> credenciaisInvalidasHandler(CredenciaisInvalidasException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }
}
