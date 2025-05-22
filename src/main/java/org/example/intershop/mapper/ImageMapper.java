package org.example.intershop.mapper;

import org.example.intershop.domain.Image;
import org.example.intershop.dto.ImageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageDto imageEntityToImageDto(Image image);
    Image imageDtoToImage(ImageDto image);
}
