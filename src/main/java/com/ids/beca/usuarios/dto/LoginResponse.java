
package com.ids.beca.usuarios.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    @Override
    public String toString() {
        return "LoginResponse[accessToken=***, tokenType="
                + tokenType + ", expiresIn=" + expiresIn + "]";
    }

}
