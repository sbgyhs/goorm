package com.eouil.bank.bankapi.dtos.responses;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class TransactionResponseDTO {
    private Long transactionID;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String type;
    private BigDecimal amount;
    private String memo;
    private String status;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
