package com.bsoft.catalogus;

import com.bsoft.catalogus.util.CatalogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class UtilityTests {

    @Test
    public void getDate() {
        CatalogUtil ct = new CatalogUtil();
        String date = ct.getCurrentDate();
        log.info("Current data: {}", date);
    }
}
