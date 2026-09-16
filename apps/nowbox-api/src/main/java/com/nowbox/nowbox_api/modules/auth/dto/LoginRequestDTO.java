package com.nowbox.nowbox_api.modules.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequestDTO {

    private String email;

    private String senha;

}
