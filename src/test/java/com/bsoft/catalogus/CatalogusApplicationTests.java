package com.bsoft.catalogus;

import com.bsoft.catalogus.model.InlineResponse200;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
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
public class CatalogusApplicationTests {

    @Autowired
    CatalogService catalogService;

    @Test
    void contextLoads() {
    }

    @Test
    public void testCatalogService_01() {
        log.info("Start test: CatalogusApplicationTests.testCatalogService_01");
        String uri = "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = "2021-04-14";
        Integer page = 1;
        Integer pageSize = 50;
        List<String> expandScope = Arrays.asList("collecties", "concepten");

        OperationResult<InlineResponse200> result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
        Assert.notNull(result, "Result expected, null received");
        if (result.isSuccess()) {
            InlineResponse200 inlineResponse200 = result.getSuccessResult();
            try {
                log.info("Result as string: {}", result);
                ObjectMapper mapper = new ObjectMapper();
                //String jsonString = mapper.writeValueAsString(inlineResponse200);
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inlineResponse200);
                log.info("Result as json: \n{}", jsonString);
            } catch (JsonProcessingException ex) {
                log.error("Error processing json: {}", ex);
            }
        } else {
            log.error("Error during request, result: {}", result.getFailureResult());
        }
        log.info("End   test: CatalogusApplicationTests.testCatalogService_01");
    }
}
