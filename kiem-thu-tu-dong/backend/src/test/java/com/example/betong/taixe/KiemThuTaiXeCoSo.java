package com.example.betong.taixe;

import com.example.betong.entity.*;
import com.example.betong.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lớp cha của mọi test tác nhân Tài xế.
 *
 * - @SpringBootTest: khởi động TOÀN BỘ backend (controller, service, security...) như khi chạy thật.
 * - @AutoConfigureMockMvc: cho phép gọi API qua MockMvc (giả lập request HTTP, không cần mở cổng 8080).
 * - @ActiveProfiles("test"): dùng src/test/resources/application-test.properties (CSDL H2 trong bộ nhớ).
 * - @Import(RedisGiaConfig): thay Redis thật bằng Redis giả.
 *
 * Mỗi test TỰ TẠO dữ liệu riêng (tài xế, xe, đơn hàng...) nên các test độc lập, chạy theo thứ tự nào cũng được.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisGiaConfig.class)
public abstract class KiemThuTaiXeCoSo {

    protected static final String KEY_APP = "betongmobile";      // header ClientKey của app tài xế
    protected static final String KEY_WEB = "betong-web-local";  // header ClientKey của web điều phối
    protected static final String MAT_KHAU = "123456789";

    // Tọa độ dùng chung: trạm trộn (Cầu Giấy) và công trình cách khoảng 3,6 km
    protected static final double TRAM_VI_DO = 21.0362, TRAM_KINH_DO = 105.7906;
    protected static final double CT_VI_DO = 21.0045, CT_KINH_DO = 105.7985;

    private static final AtomicInteger DEM = new AtomicInteger();

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected JdbcTemplate db;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected RedisGiaConfig.RedisGia redis;

    @Autowired protected VaiTroRepository vaiTroRepo;
    @Autowired protected TaiKhoanRepository taiKhoanRepo;
    @Autowired protected TaiXeRepository taiXeRepo;
    @Autowired protected XeRepository xeRepo;
    @Autowired protected KhachHangRepository khachHangRepo;
    @Autowired protected CongTrinhRepository congTrinhRepo;
    @Autowired protected TramTronRepository tramTronRepo;
    @Autowired protected DonHangRepository donHangRepo;

    /** Thông tin một tài xế vừa tạo để test. */
    protected record TaiXeTest(String tenDangNhap, Long idTK, Long idTX, Long idXe, String token) {}

    // ============================ TẠO DỮ LIỆU ============================

    protected TaiKhoan taoTaiKhoan(String tenVaiTro, String tienTo) {
        VaiTro vaiTro = vaiTroRepo.findByTenVaiTro(tenVaiTro)
                .orElseGet(() -> vaiTroRepo.save(VaiTro.builder().tenVaiTro(tenVaiTro).build()));
        int so = DEM.incrementAndGet();
        return taiKhoanRepo.save(TaiKhoan.builder()
                .tenDangNhap(tienTo + so)
                .matKhau(passwordEncoder.encode(MAT_KHAU))
                .email(tienTo + so + "@test.vn")
                .sdt(String.format("09%08d", so))
                .hoTen("Test " + tienTo + so)
                .trangThai(1).daXacThuc(1).phaiDoiMatKhau(0)
                .vaiTro(vaiTro)
                .build());
    }

    /** Tạo tài xế (tùy chọn có GPLX, có xe) và đăng nhập sẵn bằng app. */
    protected TaiXeTest taoTaiXe(boolean coGPLX, boolean coXe) throws Exception {
        TaiKhoan tk = taoTaiKhoan("Tài xế", "taixe");
        TaiXe tx = taiXeRepo.save(TaiXe.builder()
                .taiKhoan(tk).hoTen(tk.getHoTen()).sdt(tk.getSdt())
                .soGPLX(coGPLX ? "GPLX-" + tk.getIdTK() : null)
                .trangThai(1).build());
        Long idXe = null;
        if (coXe) {
            idXe = xeRepo.save(Xe.builder().bienSo("29C-" + (10000 + tk.getIdTK()))
                    .trongTai(9.0).trangThai(1).taiXe(tx).build()).getIdXe();
        }
        return new TaiXeTest(tk.getTenDangNhap(), tk.getIdTK(), tx.getIdTX(), idXe,
                dangNhap(tk.getTenDangNhap(), KEY_APP));
    }

    protected TaiXeTest taoTaiXe() throws Exception {
        return taoTaiXe(true, true);
    }

    /** Tạo đơn hàng ở trạng thái 6 "Đã phân bổ trạm trộn" (sẵn sàng cho điều phối tạo chuyến). */
    protected DonHang taoDonHangDaPhanBo() {
        TaiKhoan tkKh = taoTaiKhoan("Khách hàng", "khach");
        KhachHang kh = khachHangRepo.save(KhachHang.builder().taiKhoan(tkKh)
                .tenKhachHang(tkKh.getHoTen()).sdt(tkKh.getSdt()).email(tkKh.getEmail()).build());
        CongTrinh ct = congTrinhRepo.save(CongTrinh.builder().khachHang(kh)
                .tenCongTrinh("Công trình test").diaChi("233 Hoàng Quốc Việt, Cầu Giấy, Hà Nội")
                .viDo(CT_VI_DO).kinhDo(CT_KINH_DO).sdt(kh.getSdt()).build());
        TramTron tram = tramTronRepo.save(TramTron.builder().tenTram("Trạm trộn test").diaChi("Cầu Giấy, Hà Nội")
                .congSuat(50.0).sdt("0933333333").trangThai(1).viDo(TRAM_VI_DO).kinhDo(TRAM_KINH_DO).build());
        return donHangRepo.save(DonHang.builder().khachHang(kh).congTrinh(ct).tramTron(tram)
                .ngayDat(LocalDate.now()).thoiGianGiao(LocalDateTime.now().plusHours(2))
                .tongKhoiLuong(8.0).tongTien(12_000_000.0).trangThai(6).ghiChu("Đơn test").build());
    }

    /** Điều phối tạo chuyến cho tài xế qua API web (POST /api/dieu-phoi/xe/chuyen). Trả về id chuyến (trạng thái 5). */
    protected Long taoChuyen(DonHang don, TaiXeTest tx) throws Exception {
        String token = dangNhap(taoTaiKhoan("Nhân viên điều phối", "dieuphoi").getTenDangNhap(), KEY_WEB);
        String body = """
                {"idDH": %d, "idXe": %d, "idTX": %d, "idTram": %d}
                """.formatted(don.getIdDH(), tx.idXe(), tx.idTX(), don.getTramTron().getIdTram());
        String res = mvc.perform(post("/api/dieu-phoi/xe/chuyen")
                        .header("ClientKey", KEY_WEB).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(res).get("idChuyen").asLong();
    }

    /** Tạo chuyến và cho tài xế NHẬN luôn -> chuyến ở trạng thái 3 (Đang giao). */
    protected Long taoChuyenDaNhan(TaiXeTest tx) throws Exception {
        Long id = taoChuyen(taoDonHangDaPhanBo(), tx);
        appPut(tx, "/api/tai-xe/chuyen/" + id + "/phan-hoi", "{\"chapNhan\": true}").andExpect(status().isOk());
        return id;
    }

    // ============================ GỌI API ============================

    protected String dangNhap(String tenDangNhap, String clientKey) throws Exception {
        String res = mvc.perform(post("/api/auth/dang-nhap").header("ClientKey", clientKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dinhDanh\":\"" + tenDangNhap + "\",\"matKhau\":\"" + MAT_KHAU + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(res).get("accessToken").asText();
    }

    /** Request từ app tài xế: kèm ClientKey của app + token đăng nhập. */
    private MockHttpServletRequestBuilder tuApp(MockHttpServletRequestBuilder req, TaiXeTest tx) {
        return req.header("ClientKey", KEY_APP).header("Authorization", "Bearer " + tx.token());
    }

    protected ResultActions appGet(TaiXeTest tx, String url) throws Exception {
        return mvc.perform(tuApp(get(url), tx));
    }

    protected ResultActions appPut(TaiXeTest tx, String url, String body) throws Exception {
        return mvc.perform(tuApp(put(url), tx).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected ResultActions appPost(TaiXeTest tx, String url, String body) throws Exception {
        return mvc.perform(tuApp(post(url), tx).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected JsonNode docJson(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    // ============================ ĐỌC CSDL ĐỂ KIỂM TRA ============================

    protected int trangThaiChuyen(Long idChuyen) {
        return db.queryForObject("SELECT trang_thai FROM chuyen WHERE id_chuyen = ?", Integer.class, idChuyen);
    }

    protected int trangThaiDon(Long idDH) {
        return db.queryForObject("SELECT trang_thai FROM don_hang WHERE iddh = ?", Integer.class, idDH);
    }

    protected int trangThaiXe(Long idXe) {
        return db.queryForObject("SELECT trang_thai FROM xe WHERE id_xe = ?", Integer.class, idXe);
    }

    protected Long idDonCuaChuyen(Long idChuyen) {
        return db.queryForObject("SELECT iddh FROM chuyen WHERE id_chuyen = ?", Long.class, idChuyen);
    }

    /** Số thông báo loại `loai` gửi cho một VAI TRÒ (vd "Nhân viên điều phối") về đối tượng idThamChieu. */
    protected int soThongBaoChoVaiTro(String vaiTro, String loai, Long idThamChieu) {
        return db.queryForObject("SELECT COUNT(*) FROM thong_bao WHERE vai_tro_nhan = ? AND loai = ? AND id_tham_chieu = ?",
                Integer.class, vaiTro, loai, idThamChieu);
    }

    /** Số thông báo loại `loai` gửi riêng cho một TÀI KHOẢN (tài xế hoặc khách hàng). */
    protected int soThongBaoChoTaiKhoan(Long idTK, String loai) {
        return db.queryForObject("SELECT COUNT(*) FROM thong_bao WHERE idtk = ? AND loai = ?",
                Integer.class, idTK, loai);
    }

    /** id tài khoản của khách hàng đặt đơn chứa chuyến này (để kiểm tra khách hàng có nhận thông báo không). */
    protected Long idTaiKhoanKhachCuaChuyen(Long idChuyen) {
        return db.queryForObject("""
                SELECT kh.idtk FROM chuyen c JOIN don_hang d ON d.iddh = c.iddh
                JOIN khach_hang kh ON kh.idkh = d.idkh WHERE c.id_chuyen = ?""", Long.class, idChuyen);
    }

    protected int soViTriGps(Long idChuyen) {
        return db.queryForObject("SELECT COUNT(*) FROM vi_tri_gps WHERE id_chuyen = ?", Integer.class, idChuyen);
    }
}
