package com.example.myapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/** Dựng các phần (part) của request multipart: trường văn bản và ảnh chọn từ thư viện. */
public final class FormParts {

    private static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    private static final long ANH_TOI_DA = 5L * 1024 * 1024; // khớp giới hạn 5 MB của FileStorageService

    private FormParts() {}

    /** Trả về null nếu value null để Retrofit bỏ qua trường này. */
    public static RequestBody text(Object value) {
        if (value == null) return null;
        // OkHttp 3.x (đi kèm Retrofit 2.9): tham số MediaType đứng trước nội dung
        return RequestBody.create(TEXT, String.valueOf(value));
    }

    /**
     * Đọc ảnh từ Uri thành part "name". Backend chỉ nhận JPG/PNG/WEBP tối đa 5 MB
     * và cần tên file có phần mở rộng.
     */
    public static MultipartBody.Part image(Context context, Uri uri, String name) throws Exception {
        if (uri == null) return null;
        String mime = context.getContentResolver().getType(uri);
        if (mime == null) mime = "image/jpeg";
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (ext == null) ext = "jpg";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IllegalStateException("Không đọc được ảnh");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                if (out.size() > ANH_TOI_DA) throw new IllegalStateException("Ảnh vượt quá 5 MB, vui lòng chọn ảnh khác");
            }
        }
        RequestBody body = RequestBody.create(MediaType.parse(mime), out.toByteArray());
        return MultipartBody.Part.createFormData(name, name + "." + ext, body);
    }
}
