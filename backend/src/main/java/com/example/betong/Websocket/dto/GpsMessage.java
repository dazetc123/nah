package com.example.betong.Websocket.dto;

import lombok.Data;

/**
 * Payload JSON mà app tài xế gửi lên qua WebSocket mỗi 5-10 giây
 * (mục 2.2.2 báo cáo - usecase "Gửi tọa độ vị trí theo thời gian thực").
 *
 * Ví dụ: {"idChuyen":12,"viDo":21.0045,"kinhDo":105.7985,"tocDo":32.5,
 *          "thoiDiem":"2026-10-07T10:15:30"}
 */
@Data
public class GpsMessage {
    private Long idChuyen;
    private Double viDo;
    private Double kinhDo;
    private Double tocDo;

    /** ISO-8601 (ví dụ 2026-10-07T10:15:30). Có thể để trống, server sẽ tự lấy giờ hiện tại. */
    private String thoiDiem;
}
