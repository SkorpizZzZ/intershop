package org.example.intershop.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

@Service
public class FileService {

    public Mono<Resource> getFile(String fileName) {
       return Mono.fromSupplier(() -> new ClassPathResource(String.format("static/%s.jpeg", fileName)))
                .flatMap(resource -> {
                    if (resource.exists()) {
                        return Mono.just((Resource) resource);
                    } else {
                        return Mono.error(new FileNotFoundException(
                                String.format("Не удалось найти запрашиваемый файл %s", fileName)
                        ));
                    }
                })
                .onErrorMap(ex -> new RuntimeException(String.format("Ошибка при загрузке файла %s", fileName)));
    }
}
