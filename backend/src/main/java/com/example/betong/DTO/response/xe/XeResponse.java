package com.example.betong.DTO.response.xe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@AllArgsConstructor
public class XeResponse {
    private Long idXe;
    private String bienSo;
    private Double trongTai;
    private Integer trangThai;
    private Long idTX;
    private String thongBao;
}
