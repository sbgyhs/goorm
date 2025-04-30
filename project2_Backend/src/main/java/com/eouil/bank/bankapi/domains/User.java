package com.eouil.bank.bankapi.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Getter@Setter
public class User {
    @Id
    @Column(length = 36)
    private String userId;
    @Column(length = 16, nullable = false)
    private String name;
    @Column(length = 50, nullable = false, unique = true)
    private String email;
    @Column(length = 100, nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    //Google MFA
    private String mfaSecret;
}
