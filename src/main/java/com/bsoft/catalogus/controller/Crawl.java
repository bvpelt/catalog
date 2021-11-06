package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.CrawlResult;
import com.bsoft.catalogus.model.ProcesResult;
import com.bsoft.catalogus.repository.*;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor
@Slf4j
@RestController
public class Crawl {

    @Autowired
    CatalogService catalogService;

    @Autowired
    ConceptschemaRepository conceptschemaRepository;

    @Autowired
    ConceptschemaTypeRepository conceptschemaTypeRepository;

    @Autowired
    TrefwoordRepository trefwoordRepository;

    @Autowired
    ToelichtingRepository toelichtingRepository;

    @Autowired
    WaardelijstRepository waardelijstRepository;
    @Autowired
    private ConceptRepository conceptRepository;
    @Autowired
    private CollectieRepository collectieRepository;
    @Autowired
    private BronRepository bronRepository;

    @RequestMapping(value = "/crawl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CrawlResult> doCrawl() {
        log.info("ConceptschemaController getConceptschemas");
        CrawlResult crawlResult = new CrawlResult();
        ProcesResult procesResult = null;

        OperationResult<ProcesResult> result = null;
        // get conceptschema's
        // get all concepts
        // get all collections
        // get all bronnen
        // get all waardelijsten
        ConceptSchemaLoader conceptSchemaLoader = new ConceptSchemaLoader(conceptschemaRepository, conceptschemaTypeRepository);
        result = conceptSchemaLoader.loadConceptSchemas(catalogService);

        if (result.isSuccess()) {
            crawlResult.setConceptSchema(result.getSuccessResult());

            ConceptLoader conceptLoader = new ConceptLoader(trefwoordRepository, toelichtingRepository, conceptRepository);
            result = conceptLoader.loadConcept(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setConcepten(result.getSuccessResult());

            CollectieLoader collectieLoader = new CollectieLoader(collectieRepository);
            result = collectieLoader.loadCollectie(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setCollecties(result.getSuccessResult());

            BronLoader bronLoader = new BronLoader(bronRepository);
            result = bronLoader.loadBron(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setBronnen(result.getSuccessResult());

            WaardelijstLoader waardelijstLoader = new WaardelijstLoader(waardelijstRepository, conceptRepository, trefwoordRepository, toelichtingRepository);
            result = waardelijstLoader.loadWaardelijsten(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setWaarden(result.getSuccessResult());
        } else {
            crawlResult.setMessage(result.getFailureResult().getMessage());
            crawlResult.setStatus(ProcesResult.ERROR);
        }


        if (crawlResult.getStatus() == 0) {
            return ResponseEntity.ok(crawlResult);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(crawlResult);
        }
    }
}
