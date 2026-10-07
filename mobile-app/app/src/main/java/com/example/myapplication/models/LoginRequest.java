package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("dinhDanh")
    private String dinhDanh;

    @SerializedName("matKhau")
    private String matKhau;

    public LoginRequest(String dinhDanh, String matKhau) {
        this.dinhDanh = dinhDanh;
        this.matKhau = matKhau;
    }
}
