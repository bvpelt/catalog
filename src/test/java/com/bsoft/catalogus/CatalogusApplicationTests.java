package com.bsoft.catalogus;

import com.bsoft.catalogus.model.InlineResponse200;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class CatalogusApplicationTests {

	@Rule
	public TestName name = new TestName();

	@Autowired
	CatalogService catalogService;

	@Test
	void contextLoads() {
		log.info("Start test: {}", name.getMethodName());
		log.info("End   test: {}", name.getMethodName());
	}

	@Test
	public void testCatalogService_01() {
		log.info("Start test: {}", name.getMethodName());
		String uri = "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
		String gepubliceerdDoor = "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
		String geldigOp = "2021-04-14";
		Integer page = 1;
		Integer pageSize = 50;
		List<String> expandScope = Arrays.asList("collecties");

		OperationResult<InlineResponse200> result =  catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
		Assert.notNull(result, "Result expected");

		try {
			log.info("Result as string: {}", result.toString());
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(result);
			log.info("Result as json: {}", jsonString);
		} catch (JsonProcessingException ex) {
			log.error("Error processing json: {}", ex);
		}
		log.info("End   test: {}", name.getMethodName());
	}
}
