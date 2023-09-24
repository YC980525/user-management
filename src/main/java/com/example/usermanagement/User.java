package com.example.usermanagement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @Column(nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonIgnore
    @Column(nullable = false)
    private Boolean enabled;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "username")
    private Set<Authority> authorities;

    private String email;

    public User() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends Authority> authorities) {
        this.authorities = new HashSet<>(authorities);
    }

    public void setAuthorities(String... authorityNames) {
        Set<Authority> authoritiesHolder = new HashSet<>();
        for (String authorityName : authorityNames) {
            authoritiesHolder.add(new Authority(authorityName));
        }
        this.authorities = authoritiesHolder;
    }

    public void setRoles(String... roleNames) {
        Set<Authority> authoritiesHolder = new HashSet<>();
        for (String roleName : roleNames) {
            authoritiesHolder.add(new Authority("ROLE_" + roleName));
        }
        this.authorities = authoritiesHolder;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
