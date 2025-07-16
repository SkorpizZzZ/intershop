package org.example.intershop.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "cart_item", schema = "#{@dataBaseConfiguration.DEFAULT_SCHEMA}")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;

    private Long quantity;
}
