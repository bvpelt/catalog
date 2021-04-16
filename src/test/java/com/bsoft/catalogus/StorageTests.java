package com.bsoft.catalogus;

import com.bsoft.catalogus.model.ConceptschemaDTO;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@DataJpaTest
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = CatalogusApplication.class)
//@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
//@TestPropertySource(value = {
//        "classpath:application.properties",
//        "classpath:application.properties"
//})
//@SpringBootTest(classes=CatalogusApplication.class)
//@SpringBootTest
public class StorageTests {


    @Autowired
    private ConceptschemaRepository conceptschemaRepository;

    @Autowired
    private ConceptschemaTypeRepository conceptschemaTypeRepository;


    @Test
    public void storeConceptScheme() {
        log.info("Start test: StorageTests.storeConceptScheme");
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        conceptschemaDTO.setEigenaar("eigenaar");
        conceptschemaDTO.setBegindatumGeldigheid("2014-04-20");
        conceptschemaDTO.setNaam("naam");
        conceptschemaDTO.setUri("uri");

        conceptschemaRepository.save(conceptschemaDTO);

        List<ConceptschemaDTO> conceptschemaDTOS = conceptschemaRepository.findAll();
        Assert.notNull(conceptschemaDTOS, "conceptschemaDTOS does not exist");
        Assert.isTrue(1 == conceptschemaDTOS.size(), "expected size == 1, found size " + conceptschemaDTOS.size());

        log.info("End   test: StorageTests.storeConceptScheme");
    }
}
