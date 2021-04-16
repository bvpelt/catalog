package com.bsoft.catalogus;

import com.bsoft.catalogus.model.ConceptschemaDTO;
import com.bsoft.catalogus.model.ConceptschemaTypeDTO;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
//@DataJpaTest
@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = CatalogusApplication.class)
//@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
//@TestPropertySource(value = {
//        "classpath:application.properties",
//        "classpath:application-test.properties"
//})
@SpringBootTest(classes=CatalogusApplication.class)
public class StorageTests {

    @Rule
    public TestName name = new TestName();

    @Autowired
    private ConceptschemaRepository conceptschemaRepository;

    @Autowired
    private ConceptschemaTypeRepository conceptschemaTypeRepository;


    @Test
    public void storeConceptScheme() {
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        conceptschemaDTO.setEigenaar("eigenaar");
        conceptschemaDTO.setBegindatumGeldigheid("2014-04-20");
        conceptschemaDTO.setNaam("naam");
        conceptschemaDTO.setUri("uri");

        conceptschemaRepository.save(conceptschemaDTO);
    }
}
