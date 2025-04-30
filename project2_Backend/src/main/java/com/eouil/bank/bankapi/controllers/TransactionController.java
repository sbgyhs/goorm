package com.eouil.bank.bankapi.controllers;

import com.eouil.bank.bankapi.dtos.requests.DepositRequestDTO;
import com.eouil.bank.bankapi.dtos.requests.TransactionHistoryRequest;
import com.eouil.bank.bankapi.dtos.requests.TransferRequestDTO;
import com.eouil.bank.bankapi.dtos.requests.WithdrawRequestDTO;
import com.eouil.bank.bankapi.dtos.responses.TransactionHistoryResponse;
import com.eouil.bank.bankapi.dtos.responses.TransactionResponseDTO;
import com.eouil.bank.bankapi.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(
            @RequestBody TransferRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        log.info("[POST /transfer] 요청 도착: {}", request);
        TransactionResponseDTO response = transactionService.transfer(request, token);
        log.info("[POST /transfer] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(
            @RequestBody WithdrawRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        log.info("[POST /withdraw] 요청 도착: {}", request);
        TransactionResponseDTO response = transactionService.withdraw(request, token);
        log.info("[POST /withdraw] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(
            @RequestBody DepositRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        log.info("[POST /deposit] 요청 도착: {}", request);
        TransactionResponseDTO response = transactionService.deposit(request, token);
        log.info("[POST /deposit] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        log.info("[GET /transactions] 요청 도착 (토큰: {})", token.substring(0, Math.min(token.length(), 10)) + "...");
        List<TransactionResponseDTO> transactions = transactionService.getTransactions(token);
        log.info("[GET /transactions] 조회 완료: {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }
}
