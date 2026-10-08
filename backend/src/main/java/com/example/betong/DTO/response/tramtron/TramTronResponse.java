package com.example.betong.DTO.response.tramtron;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TramTronResponse {
    private Long idTram;
    private String tenTram;
    private String diaChi;
    private Double congSuat;
    private String sdt;
    private Integer trangThai;

    /** Chỉ set khi cần gửi kèm 1 thông báo đặc biệt (ví dụ: xóa/sửa/thêm thành công, hoặc không tìm thấy kết quả). */
    private String thongBao;
}
