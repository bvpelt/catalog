package com.bsoft.catalogus;

import com.bsoft.catalogus.model.ConceptschemaDTO;
import com.bsoft.catalogus.model.ConceptschemaTypeDTO;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        conceptschemaDTO.setUri("uri1");

        conceptschemaRepository.save(conceptschemaDTO);

        List<ConceptschemaDTO> conceptschemaDTOS = conceptschemaRepository.findAll();
        Assert.notNull(conceptschemaDTOS, "conceptschemaDTOS does not exist");
        Assert.isTrue(1 == conceptschemaDTOS.size(), "expected size == 1, found size " + conceptschemaDTOS.size());

        log.info("End   test: StorageTests.storeConceptScheme");
    }

    @Test
    public void storeConceptSchemaAndType() {
        log.info("Start test: StorageTests.storeConceptSchemaAndType");
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        conceptschemaDTO.setEigenaar("eigenaar");
        conceptschemaDTO.setBegindatumGeldigheid("2014-04-20");
        conceptschemaDTO.setNaam("naam");
        conceptschemaDTO.setUri("uri2");

        conceptschemaRepository.save(conceptschemaDTO);

        ConceptschemaTypeDTO conceptschemaTypeDTO = new ConceptschemaTypeDTO();
        conceptschemaTypeDTO.setType("een");
        conceptschemaTypeDTO.getConceptschemas().add(conceptschemaDTO);

        conceptschemaTypeRepository.save(conceptschemaTypeDTO);

        conceptschemaDTO.getTypes().add(conceptschemaTypeDTO);
        conceptschemaRepository.save(conceptschemaDTO);

        Optional<ConceptschemaDTO> optionalConceptschemaDTO = conceptschemaRepository.findByUri("uri2");
        Assert.isTrue(optionalConceptschemaDTO.isPresent(), "Concept schema uri: uri2 expected, not found");
        ConceptschemaDTO savedConceptschemaDTO = optionalConceptschemaDTO.get();
        Assert.isTrue(savedConceptschemaDTO.equals(conceptschemaDTO), "Original and saved instance are different");
        log.info("End   test: StorageTests.storeConceptSchemaAndType");
    }
}
