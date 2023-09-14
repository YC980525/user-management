package com.example.usermanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "AUTHORITIES",
    uniqueConstraints = @UniqueConstraint(columnNames = {"USERNAME", "AUTHORITY"})
)
public class Authority {

    @Id
    @Column(length = 128, nullable = false)
    private String username;

    @Id
    @Column(length = 128, nullable = false)
    private String authority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "USERNAME",
        referencedColumnName = "USERNAME",
        insertable = false,
        updatable = false,
        nullable = false
    )
    private User user;

}

// class AuthorityId {
//     private String username;
//     private String authority;
// }
