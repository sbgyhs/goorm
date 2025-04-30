package com.eouil.bank.bankapi.repositories;

import com.eouil.bank.bankapi.domains.Account;
import com.eouil.bank.bankapi.domains.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    // 계좌 번호 존재 여부 확인
    boolean existsByAccountNumber(String accountNumber);

    // 계좌 + 유저 즉시 로딩 (일반 조회용)
    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    // 특정 유저가 보유한 전체 계좌 조회
    List<Account> findByUser(User user);

    // 계좌 락을 걸고 조회 (동시성 제어용)
    @Query(value = "SELECT * FROM account WHERE account_number = :accountNumber FOR UPDATE", nativeQuery = true)
    Account findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
