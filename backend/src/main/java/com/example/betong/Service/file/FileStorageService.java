package com.example.betong.Service.file;

import com.example.betong.Exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final Path root;
    private final String publicBaseUrl;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir,
                              @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException("Vui lòng chọn hình ảnh", HttpStatus.BAD_REQUEST);
        }
        if (!IMAGE_TYPES.contains(file.getContentType())) {
            throw new AppException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException("Hình ảnh không được vượt quá 5 MB", HttpStatus.BAD_REQUEST);
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || extension.isBlank()) {
            throw new AppException("Tên file hình ảnh không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        try {
            Path directory = root.resolve(folder).normalize();
            Files.createDirectories(directory);
            String filename = UUID.randomUUID() + "." + extension.toLowerCase();
            Files.copy(file.getInputStream(), directory.resolve(filename));
            return publicBaseUrl + "/uploads/" + folder + "/" + filename;
        } catch (IOException ex) {
            throw new AppException("Không thể lưu hình ảnh, vui lòng thử lại", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
