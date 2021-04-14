package com.bsoft.catalogus;

import com.bsoft.catalogus.model.InlineResponse200;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class CatalogusApplicationTests {

	@Autowired
	CatalogService catalogService;

	@Test
	void contextLoads() {
	}

	public void testCatalogService_01() {
		String uri = "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
		String gepubliceerdDoor = "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
		String geldigOp = "2021-04-14";
		Integer page = 1;
		Integer pageSize = 50;
		List<String> expandScope = Arrays.asList("collecties");

		OperationResult<InlineResponse200> result =  catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
		Assert.notNull(result, "Result expected");
		log.info("Result: {}", result.toString());

	}
}
