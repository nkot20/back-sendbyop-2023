package com.sendByOP.expedition.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sendByOP.expedition.model.Role;
import com.sendByOP.expedition.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class UserPrinciple implements UserDetails {
    private String id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection authorities;

    private static Set<String> roles = new HashSet<>();

    public UserPrinciple(String name, String password, Collection authorities) {
        this.username = name;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrinciple build(User user) {
        Role roleUser = user.getRole();
        Collections.addAll(roles, roleUser.getIntitule());
        //SimpleGrantedAuthority authorities = new SimpleGrantedAuthority(role.getIntitule());
        List<SimpleGrantedAuthority> authorities = roles.stream().map(role ->
                new SimpleGrantedAuthority(roleUser.getIntitule())).collect(Collectors.toList());

        return new UserPrinciple(
                user.getUsername(),
                user.getPw(),
                authorities
        );
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Collection getAuthorities() {
        return authorities;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

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
}

