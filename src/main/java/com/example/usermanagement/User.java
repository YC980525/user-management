package com.example.usermanagement;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @Column(length = 128, nullable = false)
    private String username;

    @Column(length = 128, nullable = false)
    private String password;

    @Column(length = 5, nullable = false)
    private char enabled;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Authority> authorities;

}
