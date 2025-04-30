package com.eouil.bank.bankapi.dtos.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DepositRequestDTO {
    private String toAccountNumber;
    private BigDecimal amount;
    private String memo;

}
