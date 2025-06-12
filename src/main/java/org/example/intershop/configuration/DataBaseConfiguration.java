package org.example.intershop.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseConfiguration {

    @Value("${spring.data.r2dbc.schema}")
    public String DEFAULT_SCHEMA;

}
