package org.example.intershop.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "items")
@EqualsAndHashCode
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @OneToMany(mappedBy = "cart")
    private List<Item> items;
}
