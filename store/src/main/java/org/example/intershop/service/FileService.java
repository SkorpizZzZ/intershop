package org.example.intershop.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FileService {

    @Cacheable(value = "file", key = "#fileName")
    public Mono<byte[]> getFile(String fileName) {
       return Mono.fromSupplier(() -> new ClassPathResource(String.format("static/%s.jpeg", fileName)))
                .flatMap(resource -> {
                    if (resource.exists()) {
                        try {
                            return Mono.just(FileCopyUtils.copyToByteArray(resource.getInputStream()));
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    } else {
                        return Mono.error(new FileNotFoundException(
                                String.format("Не удалось найти запрашиваемый файл %s", fileName)
                        ));
                    }
                })
                .onErrorMap(ex -> new RuntimeException(String.format("Ошибка при загрузке файла %s", fileName)));
    }
}
