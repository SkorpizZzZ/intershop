package org.example.intershop.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table(name = "items", schema = "#{@dataBaseConfiguration.DEFAULT_SCHEMA}")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    @Id
    @EqualsAndHashCode.Exclude
    @Column("id")
    private Long id;

    private String title;

    private BigDecimal price;

    private String description;

    private Long count;

    @Column("image_name")
    private String imageName;

    @Column("cart_id")
    private Long cartId;

}
