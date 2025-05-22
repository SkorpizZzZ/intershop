package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileService fileService;

    public File findImageByItemId(Long itemId) {
        if (itemId != null) {
            String relativePath = imageRepository.findPathByItemId(itemId)
                    .orElseThrow(() -> new RuntimeException(
                            MessageFormat.format("Image с идентификатором item {0} не найден", itemId)
                    ));
            return fileService.getFile(relativePath);
        } else {
            throw new RuntimeException("Не передан обязательный атрибут - идентификатор Item");
        }
    }
}
