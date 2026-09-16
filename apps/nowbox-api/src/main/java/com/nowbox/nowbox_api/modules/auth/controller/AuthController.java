package com.nowbox.nowbox_api.modules.auth.controller;

import com.nowbox.nowbox_api.modules.auth.dto.LoginRequestDTO;
import com.nowbox.nowbox_api.modules.auth.dto.LoginResponseDTO;
import com.nowbox.nowbox_api.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@RequestBody LoginRequestDTO login) {
        return authService.login(login);
    }

}
