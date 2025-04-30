package com.eouil.bank.bankapi.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("존재하지 않는 이메일입니다: " + email);
    }
}
