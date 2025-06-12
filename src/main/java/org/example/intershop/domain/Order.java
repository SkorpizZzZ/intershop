package org.example.intershop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "orders", schema = "#{@dataBaseConfiguration.DEFAULT_SCHEMA}")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Order {

    @Id
    private Long id;

}
