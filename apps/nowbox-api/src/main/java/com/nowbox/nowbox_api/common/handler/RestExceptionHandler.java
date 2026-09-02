package com.nowbox.nowbox_api.common.handler;

import com.nowbox.nowbox_api.common.exception.EstadoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EstadoNaoEncontradoException.class)
    private ResponseEntity<String> estadoNotFoundHandler(EstadoNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estado não encontrado.");
    }
}
