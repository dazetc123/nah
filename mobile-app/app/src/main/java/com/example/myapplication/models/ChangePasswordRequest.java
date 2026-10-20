package com.example.myapplication.models;

public class ChangePasswordRequest {
    private String matKhauCu;
    private String matKhauMoi;
    
    public ChangePasswordRequest(String matKhauCu, String matKhauMoi) {
        this.matKhauCu = matKhauCu;
        this.matKhauMoi = matKhauMoi;
    }
}
