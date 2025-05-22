package org.example.intershop.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_seq")
    @SequenceGenerator(name = "item_seq", sequenceName = "item_sequence", allocationSize = 1)
    @EqualsAndHashCode.Exclude
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "description")
    String description;

    @Column(name = "count", nullable = false)
    Long count;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    Image image;
}
