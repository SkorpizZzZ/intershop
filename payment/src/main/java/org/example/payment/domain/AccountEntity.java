package org.example.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.example.domain.Account;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "accounts", schema = "#{@dataBaseConfiguration.DEFAULT_SCHEMA}")
@AllArgsConstructor
@Builder
public class AccountEntity extends Account {

    @Id
    @Override
    public Long getId() {
        return super.getId();
    }
}
