package com.example.myapplication.models;

public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long idTK;
    private String hoTen;
    private String tenVaiTro;
    private boolean phaiDoiMatKhau;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getIdTK() { return idTK; }
    public String getHoTen() { return hoTen; }
    public String getTenVaiTro() { return tenVaiTro; }
    public boolean isPhaiDoiMatKhau() { return phaiDoiMatKhau; }
}
