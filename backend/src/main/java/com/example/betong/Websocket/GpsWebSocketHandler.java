package com.example.betong.Websocket;

import com.example.betong.Common.TrangThaiChuyen;
import com.example.betong.Websocket.dto.GpsMessage;
import com.example.betong.entity.Chuyen;
import com.example.betong.entity.TaiKhoan;
import com.example.betong.entity.TaiXe;
import com.example.betong.entity.ViTriGPS;
import com.example.betong.repository.ChuyenRepository;
import com.example.betong.repository.TaiKhoanRepository;
import com.example.betong.repository.TaiXeRepository;
import com.example.betong.repository.ViTriGPSRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Mục 2.2.2 báo cáo - "Gửi tọa độ vị trí theo thời gian thực":
 * App tài xế mở 1 kết nối WebSocket khi Bật định vị và có chuyến đang ở
 * trạng thái Đang giao, rồi gửi mỗi điểm GPS (chu kỳ 5-10 giây) dưới dạng
 * JSON text frame. Server kiểm tra hợp lệ, lưu vào vi_tri_gps rồi trả ack.
 *
 * Dùng WebSocket thuần (không STOMP) để app di động không cần thêm thư viện
 * STOMP client — chỉ cần WebSocket có sẵn trong OkHttp (đã dùng cho Retrofit).
 */
@Component
public class GpsWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GpsWebSocketHandler.class);

    private final ChuyenRepository chuyenRepository;
    private final ViTriGPSRepository viTriGPSRepository;
    private final TaiXeRepository taiXeRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GpsWebSocketHandler(ChuyenRepository chuyenRepository, ViTriGPSRepository viTriGPSRepository,
                                TaiXeRepository taiXeRepository, TaiKhoanRepository taiKhoanRepository) {
        this.chuyenRepository = chuyenRepository;
        this.viTriGPSRepository = viTriGPSRepository;
        this.taiXeRepository = taiXeRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        TaiXe taiXe = taiXeCuaSession(session);
        if (taiXe == null) {
            dong(session, CloseStatus.NOT_ACCEPTABLE.withReason("Không tìm thấy hồ sơ tài xế"));
            return;
        }
        session.getAttributes().put("idTX", taiXe.getIdTX());
        log.info("Tài xế #{} ({}) đã mở kết nối gửi vị trí GPS", taiXe.getIdTX(), taiXe.getHoTen());
    }

    @Override
    @Transactional
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Object idTXAttr = session.getAttributes().get("idTX");
        if (!(idTXAttr instanceof Long idTX)) {
            gui(session, ackLoi("Chưa xác thực được tài xế"));
            return;
        }

        GpsMessage goi;
        try {
            goi = objectMapper.readValue(message.getPayload(), GpsMessage.class);
        } catch (Exception ex) {
            gui(session, ackLoi("Dữ liệu gửi lên không đúng định dạng JSON"));
            return;
        }

        if (goi.getIdChuyen() == null) {
            gui(session, ackLoi("Thiếu idChuyen"));
            return;
        }

        Chuyen chuyen = chuyenRepository.timCuaTaiXe(goi.getIdChuyen(), idTX).orElse(null);
        if (chuyen == null) {
            gui(session, ackLoi("Chuyến không tồn tại hoặc không được phân công cho bạn"));
            return;
        }
        if (!Integer.valueOf(TrangThaiChuyen.DANG_GIAO).equals(chuyen.getTrangThai())) {
            gui(session, ackLoi("Chuyến chưa ở trạng thái Đang giao, bỏ qua vị trí"));
            return;
        }

        if (!toaDoHopLe(goi.getViDo(), goi.getKinhDo())) {
            log.warn("Bỏ qua vị trí GPS không hợp lệ cho chuyến #{}: viDo={}, kinhDo={}",
                    goi.getIdChuyen(), goi.getViDo(), goi.getKinhDo());
            gui(session, ackLoi("Tọa độ không hợp lệ, đã bỏ qua"));
            return;
        }

        ViTriGPS viTri = ViTriGPS.builder()
                .chuyen(chuyen)
                .xe(chuyen.getXe())
                .viDo(goi.getViDo())
                .kinhDo(goi.getKinhDo())
                .tocDo(goi.getTocDo())
                .thoiDiem(parseThoiDiem(goi.getThoiDiem()))
                .build();
        viTriGPSRepository.save(viTri);

        gui(session, ackOk());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Lỗi kết nối WebSocket gửi vị trí GPS (session {}): {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Mỗi session độc lập, không giữ danh sách tĩnh nào cần dọn dẹp thêm.
        log.info("Đóng kết nối gửi vị trí GPS (session {}): {}", session.getId(), status);
    }

    // ===================== Helpers =====================

    private TaiXe taiXeCuaSession(WebSocketSession session) {
        Object tenDangNhapObj = session.getAttributes().get("tenDangNhap");
        if (!(tenDangNhapObj instanceof String tenDangNhap)) return null;
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapOrEmail(tenDangNhap).orElse(null);
        if (taiKhoan == null) return null;
        return taiXeRepository.findByTaiKhoanIdTK(taiKhoan.getIdTK()).orElse(null);
    }

    /** Chặn tọa độ ngoài phạm vi hợp lệ và điểm (0,0) — giá trị mặc định khi thiết bị chưa có tín hiệu GPS thật. */
    private boolean toaDoHopLe(Double viDo, Double kinhDo) {
        if (viDo == null || kinhDo == null) return false;
        if (viDo < -90 || viDo > 90 || kinhDo < -180 || kinhDo > 180) return false;
        return !(viDo == 0.0 && kinhDo == 0.0);
    }

    private LocalDateTime parseThoiDiem(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }

    private void gui(WebSocketSession session, String json) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(json));
        } catch (Exception ex) {
            log.warn("Không gửi được ack GPS: {}", ex.getMessage());
        }
    }

    private void dong(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // Kết nối có thể đã đóng từ phía client, bỏ qua.
        }
    }

    private String ackOk() {
        return "{\"ok\":true}";
    }

    private String ackLoi(String message) {
        return "{\"ok\":false,\"message\":\"" + message.replace("\"", "'") + "\"}";
    }
}
