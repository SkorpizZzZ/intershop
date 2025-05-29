package org.example.intershop.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@Service
public class FileService {

    public File getFile(String fileName) {
        try {
            return ResourceUtils.getFile(String.format("classpath:static/%s.jpeg", fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Не удалось найти ни запрашиваемый файл", e);
        }
    }
}
