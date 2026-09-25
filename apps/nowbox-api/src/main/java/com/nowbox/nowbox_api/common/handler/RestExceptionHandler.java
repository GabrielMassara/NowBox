package com.nowbox.nowbox_api.common.handler;

import com.nowbox.nowbox_api.common.exception.CredenciaisInvalidasException;
import com.nowbox.nowbox_api.common.exception.NaoEncontradoException;
import com.nowbox.nowbox_api.common.exception.RequisicaoInvalidaException;
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

    @ExceptionHandler(RequisicaoInvalidaException.class)
    private ResponseEntity<String> requisicaoInvalidaHandler(RequisicaoInvalidaException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    private ResponseEntity<String> credenciaisInvalidasHandler(CredenciaisInvalidasException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }
}
