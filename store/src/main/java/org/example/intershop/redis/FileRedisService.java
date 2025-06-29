package org.example.intershop.redis;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class FileRedisService {

    @Cacheable(value = "file", key = "#fileName")
    public Mono<byte[]> getFile(String fileName, Function<String, Mono<byte[]>> file) {
        return file.apply(fileName);
    }
}
