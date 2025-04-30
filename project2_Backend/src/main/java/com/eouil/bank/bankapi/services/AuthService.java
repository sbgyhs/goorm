package com.eouil.bank.bankapi.services;

import com.eouil.bank.bankapi.domains.User;
import com.eouil.bank.bankapi.dtos.requests.CreateAccountRequest;
import com.eouil.bank.bankapi.dtos.requests.JoinRequest;
import com.eouil.bank.bankapi.dtos.requests.LoginRequest;
import com.eouil.bank.bankapi.dtos.responses.JoinResponse;
import com.eouil.bank.bankapi.dtos.responses.LoginResponse;
import com.eouil.bank.bankapi.exceptions.*;
import com.eouil.bank.bankapi.repositories.UserRepository;
import com.eouil.bank.bankapi.utils.JwtUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final Environment env;
    private final RedisTemplate<String, String> redisTemplate;

    private final AccountService accountService;

    final Map<String, String> refreshStore = new HashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       Environment env,
                       RedisTemplate<String, String> redisTemplate, AccountService accountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.redisTemplate = redisTemplate;
        this.accountService = accountService;
    }

    public boolean isLocal() {
        return Arrays.asList(env.getActiveProfiles()).contains("local");
    }

    public JoinResponse join(JoinRequest joinRequest) {
        log.info("➡️ [JOIN] 요청 - email: {}", joinRequest.email);

        if (userRepository.findByEmail(joinRequest.email).isPresent()) {
            log.warn("[JOIN] 중복 이메일 시도 - {}", joinRequest.email);
            throw new DuplicateEmailException();
        }

        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);
        user.setName(joinRequest.name);
        user.setEmail(joinRequest.email);
        user.setPassword(passwordEncoder.encode(joinRequest.password));
        userRepository.save(user);

        CreateAccountRequest acctReq = new CreateAccountRequest();
        acctReq.setBalance(BigDecimal.valueOf(0));  // 초기 잔액을 0원으로 설정
        accountService.createAccount(acctReq, userId);
        log.info("[JOIN] 자동 계좌 생성 완료 - userId: {}, initialBalance: {}", userId, 0);

        log.info("[JOIN] 완료 - userId: {}, email: {}", userId, user.getEmail());
        return new JoinResponse(user.getName(), user.getEmail());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        log.info("[LOGIN] 요청 - email: {}", loginRequest.email);

        User user = userRepository.findByEmail(loginRequest.email)
                .orElseThrow(() -> new UserNotFoundException(loginRequest.email));

        if (!passwordEncoder.matches(loginRequest.password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = JwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getUserId());

        refreshStore.put(user.getUserId(), refreshToken);

        boolean mfaRegistered = user.getMfaSecret() != null;

        log.info("[LOGIN] 성공 - userId: {}, MFA 등록 여부: {}", user.getUserId(), mfaRegistered);
        return new LoginResponse(accessToken, refreshToken, mfaRegistered);
    }


    public LoginResponse refreshAccessToken(String refreshToken) {
        log.info("[REFRESH] 요청");

        String userId = JwtUtil.validateTokenAndGetUserId(refreshToken);
        String storedRefreshToken = refreshStore.get(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String newAccessToken = JwtUtil.generateAccessToken(userId);
        boolean mfaRegistered = user.getMfaSecret() != null;

        return new LoginResponse(newAccessToken, refreshToken, mfaRegistered); // 이게 핵심!
    }


    public void logout(String token) {
        log.info("[LOGOUT] 요청");

        if (token == null || token.isEmpty()) {
            throw new TokenMissingException();
        }

        String userId = JwtUtil.validateTokenAndGetUserId(token);
        refreshStore.remove(userId);

        log.info("[LOGOUT] 완료 - userId: {}", userId);
    }

    public void putRefreshTokenForTest(String userId, String refreshToken) {
        refreshStore.put(userId, refreshToken);
    }

    public String generateOtpUrlByToken(String token) {
        String userId = JwtUtil.validateTokenAndGetUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String secret = gAuth.createCredentials().getKey();
        try {
            saveSecret(user, secret);  // Redis or H2 저장 로직 분기
        } catch (Exception e) {
            log.warn("❗ Redis 저장 실패 → fallback to H2 저장: {}", e.getMessage());
            saveSecretToH2(user.getEmail(), secret);
        }

        return String.format("otpauth://totp/%s?secret=%s&issuer=EouilBank", user.getEmail(), secret);
    }

    public boolean verifyCode(String email, int code) {
        String secret = isLocal() ? getSecretFromH2(email) : getSecretFromRedis(email);
        return gAuth.authorize(secret, code);
    }

    private void saveSecretToH2(String email, String secret) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        user.setMfaSecret(secret);
        userRepository.save(user);
    }

    private String getSecretFromH2(String email) {
        return userRepository.findByEmail(email)
                .map(User::getMfaSecret)
                .orElseThrow(() -> new MfaSecretNotFoundException("H2에서 " + email));
    }

    private void saveSecretToRedis(String email, String secret) {
        redisTemplate.opsForHash().put("MFA:SECRETS", email, secret);
    }

    private String getSecretFromRedis(String email) {
        Object secret = redisTemplate.opsForHash().get("MFA:SECRETS", email);
        if (secret == null) throw new MfaSecretNotFoundException("Redis에서 " + email);
        return (String) secret;
    }

    private void saveSecret(User user, String secret) {
        if (isLocal()) {
            saveSecretToH2(user.getEmail(), secret);
        } else {
            saveSecretToRedis(user.getEmail(), secret);
        }
    }
}
