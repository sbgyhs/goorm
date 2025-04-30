package com.eouil.bank.bankapi;

import com.eouil.bank.bankapi.domains.Account;
import com.eouil.bank.bankapi.domains.User;
import com.eouil.bank.bankapi.dtos.requests.CreateAccountRequest;
import com.eouil.bank.bankapi.dtos.responses.CreateAccountResponse;
import com.eouil.bank.bankapi.repositories.AccountRepository;
import com.eouil.bank.bankapi.repositories.UserRepository;
import com.eouil.bank.bankapi.services.AccountService;
import com.eouil.bank.bankapi.utils.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private final String token = "mock.jwt.token";
    private final String userId = "user-123";
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(userId);
    }

    @Test
    void createAccount_success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(1L); // 테스트에서는 사용 안 함
        request.setBalance(new BigDecimal("100000"));

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

            CreateAccountResponse response = accountService.createAccount(request, token);

            assertNotNull(response.getAccountNumber());
            assertEquals(userId, response.getUserId());
            assertEquals(request.getBalance(), response.getBalance());
            assertNotNull(response.getCreatedAt());

            verify(accountRepository).save(any(Account.class));
        }
    }

    @Test
    void createAccount_userNotFound_shouldThrowException() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setBalance(new BigDecimal("100000"));

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateTokenAndGetUserId(token)).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    accountService.createAccount(request, token));

            assertEquals("User not found", ex.getMessage());
        }
    }
}
