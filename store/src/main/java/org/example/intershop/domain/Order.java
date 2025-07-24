package org.example.intershop.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "orders", schema = "#{@dataBaseConfiguration.DEFAULT_SCHEMA}")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @EqualsAndHashCode.Exclude
    private Long id;
    @Column("cart_id")
    private Long cartId;
}
