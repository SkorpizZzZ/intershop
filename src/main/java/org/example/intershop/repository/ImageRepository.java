package org.example.intershop.repository;

import org.example.intershop.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i.path FROM Image i WHERE i.item.id = :itemId")
    Optional<String> findPathByItemId(Long itemId);
}
