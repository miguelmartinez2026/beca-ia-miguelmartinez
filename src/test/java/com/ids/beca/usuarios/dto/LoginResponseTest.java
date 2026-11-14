
package com.ids.beca.usuarios.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginResponseTest {

    @Test
    void toStringNoDebeExponerElToken() {
        String token = "token-secreto-de-prueba";
        LoginResponse respuesta = new LoginResponse(token, "Bearer", 3600);

        assertThat(respuesta.toString())
                .contains("accessToken=***")
                .contains("tokenType=Bearer")
                .contains("expiresIn=3600")
                .doesNotContain(token);
    }

}
