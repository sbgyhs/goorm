package com.eouil.bank.bankapi;

import com.eouil.bank.bankapi.domains.*;
import com.eouil.bank.bankapi.dtos.requests.DepositRequestDTO;
import com.eouil.bank.bankapi.dtos.requests.TransferRequestDTO;
import com.eouil.bank.bankapi.dtos.requests.WithdrawRequestDTO;
import com.eouil.bank.bankapi.dtos.responses.TransactionResponseDTO;
import com.eouil.bank.bankapi.repositories.AccountRepository;
import com.eouil.bank.bankapi.repositories.TransactionJdbcRepository;
import com.eouil.bank.bankapi.repositories.TransactionRepository;
import com.eouil.bank.bankapi.repositories.UserRepository;
import com.eouil.bank.bankapi.services.TransactionService;
import com.eouil.bank.bankapi.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionJdbcRepository transactionRepository;
    @InjectMocks private TransactionService transactionService;
    @Mock private TransactionRepository transactionJPARepository;

    private final String token = "mock.jwt.token";
    private final String userId = "user-123";
    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setUserId(userId);
    }

    @Test
    void testTransfer_userNotFound_shouldThrowException() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO("111-222", "333-444", new BigDecimal("10000"), "메모");
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    transactionService.transfer(request, token)
            );

            System.out.println("결과: " + exception.getMessage());
            assertEquals("User not found", exception.getMessage());
        }
    }

    @Test
    void testTransfer_fromAccountNotFound_shouldThrowException() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO("111-222", "333-444", new BigDecimal("10000"), "메모");
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    transactionService.transfer(request, token)
            );

            System.out.println("결과: " + exception.getMessage());
            assertEquals("From Account not found", exception.getMessage());
        }
    }

    @Test
    void testTransfer_toAccountNotFound_shouldThrowException() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO("111-222", "333-444", new BigDecimal("10000"), "메모");

            Account from = new Account();
            from.setAccountNumber("111-222");
            from.setBalance(new BigDecimal("50000"));
            from.setUser(mockUser);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.of(from));
            when(accountRepository.findByAccountNumber("333-444")).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    transactionService.transfer(request, token)
            );

            System.out.println("결과: " + exception.getMessage());
            assertEquals("To Account not found", exception.getMessage());
        }
    }

    @Test
    void testTransfer_unauthorizedAccess_shouldThrowSecurityException() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO("111-222", "333-444", new BigDecimal("10000"), "메모");

            User otherUser = new User();
            otherUser.setUserId("다른유저");

            Account from = new Account();
            from.setAccountNumber("111-222");
            from.setBalance(new BigDecimal("50000"));
            from.setUser(otherUser);

            Account to = new Account();
            to.setAccountNumber("333-444");
            to.setBalance(new BigDecimal("10000"));
            to.setUser(mockUser);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.of(from));
            when(accountRepository.findByAccountNumber("333-444")).thenReturn(Optional.of(to));

            SecurityException exception = assertThrows(SecurityException.class, () ->
                    transactionService.transfer(request, token)
            );

            System.out.println("결과: " + exception.getMessage());
            assertEquals("Unauthorized access to account", exception.getMessage());
        }
    }

    @Test
    void testTransfer_insufficientFunds_shouldThrowRuntimeException() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO("111-222", "333-444", new BigDecimal("100000"), "메모");

            Account from = new Account();
            from.setAccountNumber("111-222");
            from.setBalance(new BigDecimal("5000"));
            from.setUser(mockUser);

            Account to = new Account();
            to.setAccountNumber("333-444");
            to.setBalance(new BigDecimal("10000"));
            to.setUser(mockUser);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.of(from));
            when(accountRepository.findByAccountNumber("333-444")).thenReturn(Optional.of(to));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    transactionService.transfer(request, token)
            );

            System.out.println("예외 메시지: " + exception.getMessage());
            assertEquals("Insufficient funds", exception.getMessage());
        }
    }

    @Test
    void testTransfer_success() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            TransferRequestDTO request = new TransferRequestDTO();
            request.setFromAccountNumber("111-222");
            request.setToAccountNumber("333-444");
            request.setAmount(new BigDecimal("10000"));
            request.setMemo("친구에게 송금");

            User user = new User();
            user.setUserId(userId);

            Account fromAccount = new Account();
            fromAccount.setAccountNumber("111-222");
            fromAccount.setBalance(new BigDecimal("50000"));
            fromAccount.setUser(user);

            Account toAccount = new Account();
            toAccount.setAccountNumber("333-444");
            toAccount.setBalance(new BigDecimal("20000"));
            toAccount.setUser(new User());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findByAccountNumber("333-444")).thenReturn(Optional.of(toAccount));

            TransactionResponseDTO response = transactionService.transfer(request, token);

            System.out.println("=== Transaction 결과 ===");
            System.out.println("From Account: " + response.getFromAccountNumber());
            System.out.println("To Account: " + response.getToAccountNumber());
            System.out.println("Amount: " + response.getAmount());
            System.out.println("Memo: " + response.getMemo());
            System.out.println("Balance After: " + response.getBalanceAfter());
            System.out.println("Status: " + response.getStatus());
            System.out.println("Created At: " + response.getCreatedAt());

            assertEquals("TRANSFER", response.getType());
            assertEquals("111-222", response.getFromAccountNumber());
            assertEquals("333-444", response.getToAccountNumber());
            assertEquals(new BigDecimal("10000"), response.getAmount());
            assertEquals("친구에게 송금", response.getMemo());
            assertEquals(new BigDecimal("40000"), response.getBalanceAfter());
            assertEquals("COMPLETED", response.getStatus());

            verify(accountRepository).save(fromAccount);
            verify(accountRepository).save(toAccount);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Test
    void testWithdraw_success() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            WithdrawRequestDTO request = new WithdrawRequestDTO();
            request.setFromAccountNumber("111-222");
            request.setAmount(new BigDecimal("3000"));

            Account from = new Account();
            from.setAccountNumber("111-222");
            from.setBalance(new BigDecimal("10000"));
            from.setUser(mockUser);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("111-222")).thenReturn(Optional.of(from));

            TransactionResponseDTO response = transactionService.withdraw(request, token);

            System.out.println("출금 성공 결과: " + response);
            System.out.println("남은 잔액: " + from.getBalance());

            assertEquals("WITHDRAWAL", response.getType());
            assertEquals("111-222", response.getFromAccountNumber());
            assertEquals(new BigDecimal("3000"), response.getAmount());
            assertEquals(new BigDecimal("7000"), response.getBalanceAfter());
            assertEquals("COMPLETED", response.getStatus());

            verify(accountRepository).save(from);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Test
    void testDeposit_success() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            DepositRequestDTO request = new DepositRequestDTO();
            request.setToAccountNumber("333-444");
            request.setAmount(new BigDecimal("5000"));

            Account to = new Account();
            to.setAccountNumber("333-444");
            to.setBalance(new BigDecimal("15000"));
            to.setUser(mockUser);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.findByAccountNumber("333-444")).thenReturn(Optional.of(to));

            TransactionResponseDTO response = transactionService.deposit(request, token);

            System.out.println("입금 성공 결과: " + response);
            System.out.println("남은 잔액: " + to.getBalance());

            assertEquals("DEPOSIT", response.getType());
            assertEquals("333-444", response.getToAccountNumber());
            assertEquals(new BigDecimal("5000"), response.getAmount());
            assertEquals(new BigDecimal("20000"), response.getBalanceAfter());
            assertEquals("COMPLETED", response.getStatus());

            verify(accountRepository).save(to);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Test
    void testGetTransactions_success() {
        try (MockedStatic<JwtUtil> mockedStatic = mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            //사용자
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            // 계좌 하나
            Account account1 = new Account();
            account1.setAccountNumber("111-222");
            account1.setUser(mockUser);

            when(accountRepository.findByUser(mockUser)).thenReturn(List.of(account1));

            // 트랜잭션 3개
            Transaction tx1 = Transaction.builder()
                    .fromAccount(account1)
                    .type(TransactionType.WITHDRAWAL)
                    .amount(new BigDecimal("5000"))
                    .memo("출금 1")
                    .status(TransactionStatus.COMPLETED)
                    .balanceAfter(new BigDecimal("10000"))
                    .createdAt(LocalDateTime.now())
                    .build();

            Transaction tx2 = Transaction.builder()
                    .fromAccount(account1)
                    .type(TransactionType.WITHDRAWAL)
                    .amount(new BigDecimal("3000"))
                    .memo("출금 2")
                    .status(TransactionStatus.COMPLETED)
                    .balanceAfter(new BigDecimal("7000"))
                    .createdAt(LocalDateTime.now())
                    .build();

            Transaction tx3 = Transaction.builder()
                    .toAccount(account1)
                    .type(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("2000"))
                    .memo("입금")
                    .status(TransactionStatus.COMPLETED)
                    .balanceAfter(new BigDecimal("9000"))
                    .createdAt(LocalDateTime.now())
                    .build();

            when(transactionJPARepository.findByAccountNumber("111-222")).thenReturn(List.of(tx1, tx2, tx3));

            //호출
            List<TransactionResponseDTO> result = transactionService.getTransactions(token);

            //검증
            assertEquals(3, result.size());

            TransactionResponseDTO r1 = result.get(0);
            assertEquals("출금 1", r1.getMemo());
            assertEquals("WITHDRAWAL", r1.getType());
            assertEquals(new BigDecimal("5000"), r1.getAmount());

            TransactionResponseDTO r2 = result.get(1);
            assertEquals("출금 2", r2.getMemo());
            assertEquals(new BigDecimal("3000"), r2.getAmount());

            TransactionResponseDTO r3 = result.get(2);
            assertEquals("입금", r3.getMemo());
            assertEquals("DEPOSIT", r3.getType());
            assertEquals(new BigDecimal("2000"), r3.getAmount());

            System.out.println("총 거래 건수: " + result.size());
            result.forEach(tx -> {
                System.out.println("- [" + tx.getType() + "] " + tx.getMemo() + " | 금액: " + tx.getAmount());
            });
        }
    }
}
