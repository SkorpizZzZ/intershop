package org.example.intershop.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.stream.Stream;

@Service
public class FileService {

    @Value("${image-dir}")
    private String imageDir;
    @Value("${app-base-dir}")
    private String appBaseDir;
    @Value("${clear-upload-dir:true}")
    private boolean isClear;
    @Value("${default-image-path}")
    private String defaultImage;

    public Path uploadPath;
    public String baseDir;

    @PostConstruct
    public void createUploadDir() throws IOException {
        baseDir = System.getProperty(appBaseDir);
        uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    @PreDestroy
    public void clearUploadDir() throws IOException {
        if (isClear) {
            if (Files.exists(uploadPath)) {
                try (Stream<Path> paths = Files.list(uploadPath)) {
                    paths.forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Не удалось удалить: " + path, e);
                        }
                    });
                }
            }
        }
    }

    public String saveFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        try {
            Files.write(filePath, file.getBytes());
        } catch (
                IOException e) {
            throw new RuntimeException(
                    MessageFormat.format("Не удалось сохранить file {0}", fileName)
            );
        }
        return fileName;
    }

    public File getFile(String path) {
        try {
            return ResourceUtils.getFile(baseDir.concat(path));
        } catch (FileNotFoundException e) {
            try {
                return ResourceUtils.getFile("classpath:".concat(defaultImage));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("Не удалось найти ни запрашиваемый файл, ни файл по умолчанию", ex);
            }
        }
    }

    public void delete(String path) {
        try {
            Files.delete(this.getFile(path).toPath());
        } catch (IOException e) {
            throw new RuntimeException("Произошла не предвиденная ошибка. File не удален");
        }
    }

    private Path getUploadPath() {
        return Paths.get(baseDir, "uploads", imageDir);
    }
}
