package org.example.intershop.repository;

import org.example.intershop.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllByTitle(String title, Pageable pageable);

    List<Item> findAllByCartId(Long cartId);
}
