package com.bsoft.catalogus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class CatalogusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogusApplication.class, args);
    }

}
