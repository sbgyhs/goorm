package com.eouil.bank.bankapi.repositories;

import com.eouil.bank.bankapi.domains.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String userId);
}
