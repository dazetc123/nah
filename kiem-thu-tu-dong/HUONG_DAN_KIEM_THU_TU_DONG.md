# Hướng dẫn kiểm thử tự động – tác nhân Tài xế (bản betong-main_3)

Thư mục này chứa **bộ kiểm thử tự động** cho các chức năng của Tài xế. Mình đã chạy thử với đúng code bản 3 của nhóm: **30 test, 29 đạt, 1 không đạt**. Ca không đạt là lỗi thật của backend, xem mục 7.

| Thành phần | Công cụ | Kiểm tra cái gì | Cần gì để chạy |
|---|---|---|---|
| `backend/src/test/...` | JUnit 5 + Spring Boot Test + MockMvc | Toàn bộ nghiệp vụ phía máy chủ: nhận, từ chối chuyến, hết hạn, đã đến 200 m, hoàn thành, về trạm, sự cố, GPS, trạng thái xe, phân quyền | Chỉ cần JDK. **Không cần** MySQL, Redis hay bật backend |
| `mobile-app/app/src/androidTest/...` | Espresso | Giao diện màn Đăng nhập trên app: bấm nút, kiểm tra dòng báo lỗi | Android Studio + máy ảo |

> **Kiểm thử tự động là gì?** Thay vì tự bấm từng bước rồi nhìn bằng mắt, mình viết code để máy làm thay: tạo dữ liệu → gọi chức năng → so kết quả với kết quả mong đợi. Mỗi lần nhóm sửa code chỉ cần chạy lại một lệnh là biết chức năng cũ còn đúng không (kiểm thử hồi quy).

---

## 1. Chép file vào dự án của nhóm

Bản 3 của nhóm giải nén ở `C:\Users\LENOVO\Downloads\betong-main (3)\betong-main`. Đổi đường dẫn nếu bạn để chỗ khác.

**Bước 1.1 – Lấy file về.** Mở CMD:
```
cd "C:\Users\LENOVO\Downloads\Be tong\betong-test"
git pull
```

**Bước 1.2 – Chép vào thư mục `backend` của bản 3 (một lệnh):**
```
xcopy /E /Y "C:\Users\LENOVO\Downloads\Be tong\betong-test\kiem-thu-tu-dong\backend" "C:\Users\LENOVO\Downloads\betong-main (3)\betong-main\backend\"
```
Lệnh này chép vào:
- thư mục `src\test` (các file test mới, và file `BetongApplicationTests.java` của nhóm có thêm 1 dòng `@ActiveProfiles("test")`);
- file `pom.xml`. Nó **giống hệt** `pom.xml` của nhóm, chỉ khác 2 chỗ:
  1. thêm thư viện H2 (CSDL chạy trong bộ nhớ, chỉ dùng khi test);
  2. Lombok `1.18.34` → `1.18.42` (sửa lỗi `TypeTag :: UNKNOWN` trên JDK 25), nên từ nay không cần gõ `-Dlombok.version=...` nữa.

> Nếu trưởng nhóm sửa `pom.xml` sau này thì đừng chép đè file của mình. Khi đó mở `pom.xml` của nhóm và tự sửa 2 chỗ trên:
> ```xml
> <lombok.version>1.18.42</lombok.version>
> ```
> và thêm ngay dưới khối `spring-security-test`:
> ```xml
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>test</scope>
>         </dependency>
> ```

Sau khi chép, cấu trúc sẽ như sau:
```
backend\src\test\
├── java\com\example\betong\
│   ├── BetongApplicationTests.java      (của nhóm, thêm @ActiveProfiles("test"))
│   └── taixe\
│       ├── KiemThuTaiXeCoSo.java        lớp cha: tạo dữ liệu, gọi API, đọc CSDL
│       ├── RedisGiaConfig.java          Redis giả (lưu vào bộ nhớ)
│       ├── DangNhapTaiXeTest.java       DN-02, 04, 06, 07
│       ├── QuanLyChuyenTest.java        QLC-01 → 11
│       ├── GuiViTriGpsTest.java         GPS-04, 05, 09
│       └── CapNhatTrangThaiTest.java    CNTT-01 → 14
└── resources\
    └── application-test.properties      cấu hình riêng khi test (H2, tắt quét hết hạn...)
```

---

## 2. Chạy bộ kiểm thử

### Cách 1 – Bằng CMD (dễ nhất)
```
cd "C:\Users\LENOVO\Downloads\betong-main (3)\betong-main\backend"
mvnw test
```
Nếu bạn dùng `pom.xml` của nhóm (chưa sửa Lombok) thì thêm `-Dlombok.version=1.18.42` vào cuối lệnh.

Lần đầu Maven tải thư viện H2 nên hơi lâu. Từ lần sau mất khoảng 30–40 giây. Không cần bật MySQL, Redis hay backend.

**Chạy riêng một nhóm hoặc một test:**
```
mvnw test -Dtest=QuanLyChuyenTest
mvnw test -Dtest=CapNhatTrangThaiTest#cntt03_daDenNgoaiBanKinh
mvnw test -Dtest=QuanLyChuyenTest,GuiViTriGpsTest
```

### Cách 2 – Bằng IntelliJ IDEA (bản Community miễn phí) hoặc VS Code (Extension Pack for Java)
1. Mở thư mục `backend` (File → Open), đợi IDE tải xong Maven.
2. Chuột phải vào `src/test/java` → **Run 'All Tests'**. Hoặc bấm nút ▶ xanh cạnh tên lớp hay tên hàm test.
3. Cửa sổ kết quả hiện cây test với tên tiếng Việt, ví dụ `QLC-02 Nhận chuyến: ...`. Test đạt có dấu ✓ xanh, không đạt có dấu ✗ đỏ; bấm vào ca đỏ để xem lý do.

> Android Studio không mở tốt dự án Maven, nên với backend hãy dùng CMD hoặc IntelliJ/VS Code.

---

## 3. Đọc kết quả

### 3.1 Trên màn hình CMD
Cuối lần chạy sẽ có dòng tổng kết:
```
[ERROR] Tests run: 30, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

| Chữ | Nghĩa |
|---|---|
| `Tests run: 30` | Đã chạy 30 test |
| `Failures: 1` | 1 test **không đạt**: kết quả thực tế khác kết quả mong đợi |
| `Errors` | Test bị lỗi giữa chừng (thường do code test hoặc môi trường) |
| `BUILD FAILURE` | Chỉ cần có 1 test không đạt là Maven báo FAILURE. Ở đây **là bình thường**, vì đó là lỗi DN-06 mà bộ test tìm ra (mục 7) |

Ngay phía trên dòng tổng kết, Maven in chi tiết ca không đạt:
```
[ERROR]   DangNhapTaiXeTest.dn06_chuaDangNhap:46 Status expected:<401> but was:<302>
```
Nghĩa là: ca DN-06 mong đợi mã **401** nhưng thực tế máy chủ trả **302**.

### 3.2 Báo cáo HTML (để chụp ảnh đưa vào báo cáo)
Chạy tiếp ngay sau `mvnw test`:
```
mvnw surefire-report:report-only
```
Sau đó mở file `backend\target\reports\surefire.html` bằng trình duyệt. Báo cáo có bảng tổng hợp (30 test, 1 failure, 96,7%), danh sách từng lớp và từng ca kèm thời gian chạy.

### 3.3 File kết quả thô
`backend\target\surefire-reports\` có một file `.txt` cho mỗi lớp test. File của lớp có ca không đạt chứa đầy đủ thông báo lỗi.

---

## 4. Một test hoạt động như thế nào (đọc hiểu từng dòng)

Mọi test đều theo khuôn **Chuẩn bị → Thực hiện → Kiểm tra** (Arrange – Act – Assert). Ví dụ ca CNTT-03:

```java
@Test
@DisplayName("CNTT-03 Xác nhận đã đến khi còn cách công trình 3,6 km: trả 400, trạng thái không đổi")
void cntt03_daDenNgoaiBanKinh() throws Exception {
    // 1. CHUẨN BỊ: tạo 1 tài xế (có GPLX, có xe) + 1 đơn hàng, điều phối tạo chuyến, tài xế nhận chuyến
    TaiXeTest tx = taoTaiXe();
    Long idChuyen = taoChuyenDaNhan(tx);

    // 2. THỰC HIỆN: app gửi "Đã đến" (trangThai = 2) với tọa độ đang đứng ở TRẠM TRỘN (cách công trình 3,6 km)
    appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/trang-thai", toaDo(2, TRAM_VI_DO, TRAM_KINH_DO))
    // 3. KIỂM TRA kết quả trả về
            .andExpect(status().isBadRequest())                       // mã 400
            .andExpect(jsonPath("$.message").value("Bạn chưa tới gần công trình (còn cách khoảng 3619 m, cần trong 200 m)"));

    // 3. KIỂM TRA trong CSDL: chuyến vẫn ở trạng thái 3 (Đang giao)
    assertThat(trangThaiChuyen(idChuyen)).isEqualTo(3);
}
```

Các thành phần bạn sẽ gặp:

| Thành phần | Ý nghĩa |
|---|---|
| `@Test` | Đánh dấu đây là 1 ca kiểm thử |
| `@DisplayName("...")` | Tên hiển thị, có mã ca (QLC-02, CNTT-03…) khớp với file Word |
| `@SpringBootTest` (ở lớp cha) | Khởi động **toàn bộ backend thật**: controller, service, security, CSDL |
| `MockMvc` (`appGet`, `appPut`…) | Giả lập app gửi request HTTP, kèm header `ClientKey: betongmobile` và token đăng nhập |
| `status().isOk()` / `isBadRequest()`… | Kiểm tra mã HTTP: 200 / 400 / 401 / 403 / 404 / 409 |
| `jsonPath("$.trangThai").value(3)` | Kiểm tra một trường trong JSON trả về |
| `assertThat(x).isEqualTo(y)` | Kiểm tra giá trị (thường đọc từ CSDL bằng `trangThaiChuyen()`, `trangThaiXe()`…) |

**Vì sao không cần MySQL và Redis?**
- `application-test.properties` đổi CSDL sang **H2 trong bộ nhớ**. Mỗi lần chạy test là một CSDL trống, chạy xong tự xoá, nên không đụng tới dữ liệu MySQL của bạn.
- `RedisGiaConfig` thay Redis bằng một `Map` trong bộ nhớ. Test GPS vẫn kiểm tra được "vị trí đã lưu vào Redis".
- Mỗi test **tự tạo dữ liệu riêng** (tài xế, xe, trạm, công trình, đơn hàng) qua các hàm `taoTaiXe()`, `taoDonHangDaPhanBo()`, `taoChuyen()`. Vì vậy các test độc lập, chạy theo thứ tự nào cũng được.
- Ca hết hạn 5 phút (QLC-06) không phải chờ 5 phút: test lùi thời điểm phân công về 6 phút trước rồi gọi thẳng bộ quét `quetQuaHan()`.

---

## 5. Viết thêm một test mới

Ví dụ muốn kiểm tra: **tài xế từ chối chuyến mà không ghi lý do thì hệ thống tự điền lý do mặc định**. Mở `QuanLyChuyenTest.java`, thêm vào trong lớp:

```java
@Test
@DisplayName("QLC-14 Từ chối không ghi lý do: hệ thống tự điền 'Tài xế từ chối chuyến'")
void qlc14_tuChoiKhongLyDo() throws Exception {
    // Chuẩn bị
    TaiXeTest tx = taoTaiXe();
    Long idChuyen = taoChuyen(taoDonHangDaPhanBo(), tx);

    // Thực hiện + Kiểm tra
    appPut(tx, "/api/tai-xe/chuyen/" + idChuyen + "/phan-hoi", "{\"chapNhan\": false}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lyDoTuChoi").value("Tài xế từ chối chuyến"));
}
```

Rồi chạy `mvnw test -Dtest=QuanLyChuyenTest#qlc14_tuChoiKhongLyDo`.

Các hàm có sẵn trong `KiemThuTaiXeCoSo` để dùng lại:

| Hàm | Làm gì |
|---|---|
| `taoTaiXe()` / `taoTaiXe(coGPLX, coXe)` | Tạo tài khoản tài xế + hồ sơ + xe, đăng nhập sẵn |
| `taoDonHangDaPhanBo()` | Tạo khách hàng, công trình, trạm trộn, đơn hàng ở trạng thái 6 |
| `taoChuyen(don, tx)` | Điều phối tạo chuyến qua API web → chuyến ở trạng thái 5 |
| `taoChuyenDaNhan(tx)` | Tạo đơn + chuyến + tài xế nhận luôn → chuyến ở trạng thái 3 |
| `appGet / appPut / appPost(tx, url, body)` | Gọi API như app tài xế |
| `trangThaiChuyen / trangThaiDon / trangThaiXe(id)` | Đọc trạng thái trong CSDL |
| `soThongBaoChoVaiTro(vaiTro, loai, idThamChieu)` | Đếm thông báo gửi cho điều phối / quản lý |
| `soThongBaoChoTaiKhoan(idTK, loai)` | Đếm thông báo gửi cho 1 tài khoản (tài xế, khách hàng) |
| `soViTriGps(idChuyen)` | Đếm số điểm GPS đã lưu của chuyến |
| `redis.duLieu` | Xem dữ liệu đã ghi vào Redis giả |

---

## 6. Bảng đối chiếu mã ca ↔ test tự động

| Mã ca (file Word) | Lớp test | Hàm test |
|---|---|---|
| DN-02 | DangNhapTaiXeTest | `dn02_dangNhapHopLe` |
| DN-04 | DangNhapTaiXeTest | `dn04_saiMatKhau` |
| DN-06 | DangNhapTaiXeTest | `dn06_chuaDangNhap` ← **không đạt** |
| DN-07 | DangNhapTaiXeTest | `dn07_thieuClientKey` |
| QLC-01 | QuanLyChuyenTest | `qlc01_deNghiChuyenMoi` (phần dữ liệu) |
| QLC-02 | QuanLyChuyenTest | `qlc02_nhanChuyen` |
| QLC-03 | QuanLyChuyenTest | `qlc03_nhanChuyenThieuGPLX` |
| QLC-04 | QuanLyChuyenTest | `qlc04_tuChoiChuyen` |
| QLC-06 | QuanLyChuyenTest | `qlc06_hetHanXacNhan` |
| QLC-07 | QuanLyChuyenTest | `qlc07_nhanChuyenDaHetHan` |
| QLC-08 | QuanLyChuyenTest | `qlc08_nhanChuyenCuaNguoiKhac` |
| QLC-09 | QuanLyChuyenTest | `qlc09_xemChuyenDangThucHien` (phần dữ liệu) |
| QLC-10 | QuanLyChuyenTest | `qlc10_khongCoChuyen` |
| QLC-11 | QuanLyChuyenTest | `qlc11_xemLichSu` |
| GPS-04 | GuiViTriGpsTest | `gps04_guiViTriKhiDangGiao` |
| GPS-05 | GuiViTriGpsTest | `gps05_gioiHanGhiLichSu` |
| GPS-09 | GuiViTriGpsTest | `gps09_boQuaViTriSauKhiKetThuc` |
| CNTT-01 | CapNhatTrangThaiTest | `cntt01_capNhatKhiChuaNhan` |
| CNTT-02 | CapNhatTrangThaiTest | `cntt02_daDenTrongBanKinh` |
| CNTT-03 | CapNhatTrangThaiTest | `cntt03_daDenNgoaiBanKinh` |
| CNTT-05 | CapNhatTrangThaiTest | `cntt05_hoanThanhGiaoHang` |
| CNTT-06 | CapNhatTrangThaiTest | `cntt06_daVeToiTram` |
| CNTT-07 | CapNhatTrangThaiTest | `cntt07_baoCaoSuCo` |
| CNTT-09 | CapNhatTrangThaiTest | `cntt09_suCoChuyenDaHoanThanh` |
| CNTT-10 | CapNhatTrangThaiTest | `cntt10_xeSangBaoTri` |
| CNTT-11 | CapNhatTrangThaiTest | `cntt11_baoTriKhiDangCoChuyen` |
| CNTT-12 | CapNhatTrangThaiTest | `cntt12_xeSanSangTroLai` |
| CNTT-13 | CapNhatTrangThaiTest | `cntt13_chuaPhanCongXe` |
| CNTT-14 | CapNhatTrangThaiTest | `cntt14_baoHongXe` |
| DN-03 | (Espresso) DangNhapGiaoDienTest | `dn03_boTrongTenDangNhapVaMatKhau`, `dn03_matKhauDuoi8KyTu` |

**Các ca còn lại phải test bằng tay trên điện thoại**, vì chỉ kiểm tra được trên giao diện app: DN-01, DN-05, QLC-05, QLC-12, QLC-13, GPS-01, 02, 03, 06, 07, 08, CNTT-04, 08, 15, 16, cùng phần chuông/rung/bản đồ của QLC-01 và QLC-09.

---

## 7. Lỗi bộ test tìm ra: DN-06

**Hiện tượng:** gọi API tài xế mà chưa đăng nhập, hoặc token đã hết hạn sau 24 giờ, thì backend trả **302** chuyển hướng sang `/oauth2/authorization/google` thay vì **401**.

**Hậu quả trên app:** Retrofit tự đi theo chuyển hướng và nhận về trang HTML của Google, nên không đọc được JSON. Khả năng cao app hiện lỗi kiểu "không kết nối được máy chủ" thay vì đưa tài xế về màn đăng nhập. Đây là suy luận từ code, chưa thử trên điện thoại.

**Cách sửa gợi ý** (báo trưởng nhóm). Trong `backend/src/main/java/com/example/betong/config/SecurityConfig.java`, thêm 3 dòng `.exceptionHandling(...)` vào **giữa** khối `.oauth2Login(...)` và dòng `.addFilterBefore(jwtAuthenticationFilter, ...)`:
```java
                .oauth2Login(oauth2 -> oauth2
                        ...
                        .successHandler(googleOAuth2SuccessHandler))
                // API (app, web) chưa đăng nhập / token hết hạn -> trả 401 thay vì chuyển hướng sang Google
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED),
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```
Mình đã thử sửa đúng như trên trên một bản sao: chạy lại `mvnw test` thì **30/30 đạt**. Đăng nhập Google trên web không bị ảnh hưởng vì chỉ áp dụng cho đường dẫn `/api/**`.

---

## 8. Kiểm thử giao diện app bằng Espresso (tuỳ chọn)

File `mobile-app/app/src/androidTest/java/com/example/myapplication/DangNhapGiaoDienTest.java` tự động:
1. mở màn Đăng nhập, bấm "Đăng nhập" khi bỏ trống → kiểm tra 2 dòng lỗi đỏ "Vui lòng nhập tên đăng nhập" và "Vui lòng nhập mật khẩu";
2. gõ mật khẩu 7 ký tự → kiểm tra lỗi "Mật khẩu phải từ 8 ký tự trở lên".

**Cách chạy:**
1. Chép file vào đúng thư mục `mobile-app\app\src\androidTest\java\com\example\myapplication\` của dự án nhóm. Thư viện Espresso đã có sẵn trong `build.gradle.kts`, không cần thêm.
2. Mở `mobile-app` bằng Android Studio, bật máy ảo.
3. Chuột phải file `DangNhapGiaoDienTest` → **Run 'DangNhapGiaoDienTest'**. Bạn sẽ thấy app tự mở, tự gõ, tự bấm, rồi Android Studio báo kết quả xanh/đỏ.

> Lưu ý: phần Espresso mình **chưa chạy được** vì môi trường của mình không có Android SDK/máy ảo. Code dùng đúng id của layout (`btnLogin`, `etUsername`, `tilPassword`…) và các thư viện có sẵn trong dự án. Nếu Android Studio báo lỗi, gửi mình ảnh chụp.

---

## 9. Câu hỏi thường gặp

- **Chạy test có làm mất dữ liệu MySQL của tôi không?** Không. Test dùng H2 trong bộ nhớ, hoàn toàn tách khỏi MySQL.
- **Có cần bật backend (`mvnw spring-boot:run`) trước khi test không?** Không. Test tự khởi động backend bên trong.
- **Báo `TypeTag :: UNKNOWN`?** Đó là lỗi Lombok cũ với JDK 25. Chép `pom.xml` ở bước 1.2, hoặc thêm `-Dlombok.version=1.18.42`.
- **Báo lỗi không tìm thấy `org.h2.Driver`?** `pom.xml` chưa có thư viện H2. Xem bước 1.2.
- **Muốn tạm bỏ qua ca DN-06 để build xanh?** Thêm `@Disabled("Lỗi DN-06 chưa sửa")` trên hàm `dn06_chuaDangNhap`. Nhưng khi báo cáo nên giữ nguyên để nhóm thấy lỗi.
