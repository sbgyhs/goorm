package com.eouil.bank.bankapi.repositories;

import com.eouil.bank.bankapi.domains.Transaction;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TransactionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void save(Transaction tx) {
        String sql = "INSERT INTO transaction (from_account_number, to_account_number, type, amount, memo, status, balance_after, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null,
                tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : null,
                tx.getType().name(),
                tx.getAmount(),
                tx.getMemo(),
                tx.getStatus().name(),
                tx.getBalanceAfter(),
                LocalDateTime.now()
        );
    }
}
