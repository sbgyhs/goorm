package com.eouil.bank.bankapi.dtos.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawRequestDTO {
    private String fromAccountNumber;
    private BigDecimal amount;
    private String memo;
}
