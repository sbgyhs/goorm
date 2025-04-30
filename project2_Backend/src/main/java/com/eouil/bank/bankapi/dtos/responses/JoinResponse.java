package com.eouil.bank.bankapi.dtos.responses;

public class JoinResponse {
    public String name;
    public String email;

    public JoinResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
