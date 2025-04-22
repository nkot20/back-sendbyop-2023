package com.sendByOP.expedition.reponse;

import com.sendByOP.expedition.models.dto.CustomerDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public JwtResponse(String accessToken, String refreshToken, String username, Collection<? extends GrantedAuthority> authorities) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.authorities = authorities;
    }
}
