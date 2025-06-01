package org.example.intershop.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

@Service
public class FileService {

    public Resource getFile(String fileName) {
        try {
            Resource image = new ClassPathResource(String.format("static/%s.jpeg", fileName));
            if (image.exists()) {
                return image;
            } else {
                throw new FileNotFoundException(String.format("Не удалось найти запрашиваемый файл %s", fileName));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Ошибка при загрузке файла %s", e));
        }
    }
}
