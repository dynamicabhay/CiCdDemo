package com.as.CiCdDemo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class AuthResponse {
    private final String token;
    public AuthResponse(String token){
        this.token = token;
    }


}
