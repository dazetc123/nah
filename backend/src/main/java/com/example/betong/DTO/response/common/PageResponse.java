package com.example.betong.DTO.response.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Bọc kết quả phân trang dùng chung cho MỌI API danh sách trong hệ thống
 * (tài khoản, khách hàng, xe, đơn hàng...). trangHienTai đánh số từ 1 để
 * khớp trực tiếp với tham số "trang" người dùng gõ trên giao diện.
 */
@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> danhSach;
    private int trangHienTai;
    private int tongSoTrang;
    private long tongSoPhanTu;

    public static <E, T> PageResponse<T> tu(Page<E> page, Function<E, T> chuyenDoi) {
        List<T> danhSach = page.getContent().stream().map(chuyenDoi).toList();
        return new PageResponse<>(danhSach, page.getNumber() + 1, page.getTotalPages(), page.getTotalElements());
    }
}
