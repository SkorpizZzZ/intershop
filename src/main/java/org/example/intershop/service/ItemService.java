package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Page<ItemDto> findAll(PageRequest page, String title) {
        if (StringUtils.isBlank(title)) {
            return itemRepository.findAll(page)
                    .map(itemMapper::itemEntityToItemDto);
        }
        return itemRepository.findAllByTitle(title, page)
                .map(itemMapper::itemEntityToItemDto);
    }
}
