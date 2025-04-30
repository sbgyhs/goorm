package com.eouil.bank.bankapi.exceptions;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now());
    }
}

