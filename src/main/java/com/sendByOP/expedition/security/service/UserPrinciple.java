package com.sendByOP.expedition.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class UserPrinciple implements UserDetails {
    private String id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection<SimpleGrantedAuthority> authorities;

    public UserPrinciple(String id, String name, String password, Collection<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.username = name;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrinciple build(User user) {
        String roleUser = user.getRole();
        Set<String> roles = new HashSet<>();
        Collections.addAll(roles, roleUser);
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        return new UserPrinciple(
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPrinciple user = (UserPrinciple) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

