package org.example.intershop.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("carts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {
    @Id
    @EqualsAndHashCode.Exclude
    private Long id;
    private String username;
}
