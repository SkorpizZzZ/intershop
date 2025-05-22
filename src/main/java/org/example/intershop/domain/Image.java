package org.example.intershop.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "images")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_seq")
    @SequenceGenerator(name = "image_seq", sequenceName = "image_sequence", allocationSize = 1)
    @EqualsAndHashCode.Exclude
    Long id;

    @Column(name = "path", nullable = false)
    String path;

    @OneToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id", nullable = false)
    Item item;
}