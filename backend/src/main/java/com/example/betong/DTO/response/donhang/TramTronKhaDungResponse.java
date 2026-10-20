package com.example.betong.DTO.response.donhang;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TramTronKhaDungResponse {
    private Long idTram;
    private String tenTram;
    private String diaChi;
    private Double congSuat;
    private Integer trangThai;
}
