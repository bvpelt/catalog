package com.bsoft.catalogus;

import com.bsoft.catalogus.model.InlineResponse200;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.CatalogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
public class UtilityTests {

    @Test
    public void getDate() {
        log.info("Current data: {}", new CatalogUtil().getCurrentDate());
    }
}
