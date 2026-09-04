package com.weeklyreport.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.weeklyreport.backend.entity.User;

// Wraps our User entity so Spring Security can work with it without the entity implementing UserDetails itself.
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final String roleName;

    public CustomUserDetails(User user) {
        this.user = user;
        // Resolved once here (while a Hibernate session is still guaranteed open) so
        // later lazy
        // access to user.getRole() from filters running outside a session never blows
        // up.
        this.roleName = user.getRole().getName();
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.isActive();
    }
}
