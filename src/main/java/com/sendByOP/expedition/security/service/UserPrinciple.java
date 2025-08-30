package com.sendByOP.expedition.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class UserPrinciple implements UserDetails {
    private String id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection<GrantedAuthority> authorities;

    public UserPrinciple(String id, String name, String password, Collection<GrantedAuthority> authorities) {
        this.id = id;
        this.username = name;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrinciple build(User user) {
        String roleUser = user.getRole();
        Set<String> roles = new HashSet<>();
        Collections.addAll(roles, roleUser);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UserPrinciple(
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

