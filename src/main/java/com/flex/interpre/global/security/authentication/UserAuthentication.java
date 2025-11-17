package com.flex.interpre.global.security.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserAuthentication implements Authentication {

    private final AccountPrincipal principal;

    @Getter
    @Setter
    private boolean authenticated;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role = "ROLE_" + principal.getRole().name();
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return "";
    }
}
