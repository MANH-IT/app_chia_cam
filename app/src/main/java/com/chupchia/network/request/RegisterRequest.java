package com.chupchia.network.request;

public class RegisterRequest {
    private String fullName;
    private String phone;
    private String email;
    private String password;

    public RegisterRequest(String fullName, String phone, String email, String password) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }
}
