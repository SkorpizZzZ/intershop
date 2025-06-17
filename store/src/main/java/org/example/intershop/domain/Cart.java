package org.example.intershop.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("cart")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class Cart {
    @Id
    private Long id;
}
