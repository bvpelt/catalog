package com.bsoft.catalogus.controller;

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
    WaardelijstRepository waardelijstRepository;
    @Autowired
    private ConceptRepository conceptRepository;
    @Autowired
    private CollectieRepository collectieRepository;
    @Autowired
    private BronRepository bronRepository;

    @RequestMapping(value = "/crawl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcesResult> doCrawl() {
        log.info("ConceptschemaController getConceptschemas");
        ProcesResult procesResult = null;

        OperationResult<ProcesResult> result = null;
        ProcesResult crawlResult = new ProcesResult();
        // get conceptschema's
        // get all concepts and collections
        // get all bronnen
        // get all waardelijsten
        ConceptSchemaLoader conceptSchemaLoader = new ConceptSchemaLoader(conceptschemaRepository, conceptschemaTypeRepository);
        result = conceptSchemaLoader.loadConceptSchemas(catalogService);

        if (result.isSuccess()) {
            crawlResult.setUpdatedEntries(crawlResult.getUpdatedEntries() + result.getSuccessResult().getUpdatedEntries());
            crawlResult.setUnchangedEntries(crawlResult.getUnchangedEntries() + result.getSuccessResult().getUnchangedEntries());
            crawlResult.setNewEntries(crawlResult.getNewEntries() + result.getSuccessResult().getNewEntries());
            crawlResult.setPages(crawlResult.getPages() + result.getSuccessResult().getPages());

            ConceptLoader conceptLoader = new ConceptLoader(conceptschemaRepository, conceptRepository);
            result = conceptLoader.loadConcept(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setUpdatedEntries(crawlResult.getUpdatedEntries() + result.getSuccessResult().getUpdatedEntries());
            crawlResult.setUnchangedEntries(crawlResult.getUnchangedEntries() + result.getSuccessResult().getUnchangedEntries());
            crawlResult.setNewEntries(crawlResult.getNewEntries() + result.getSuccessResult().getNewEntries());
            crawlResult.setPages(crawlResult.getPages() + result.getSuccessResult().getPages());

            CollectieLoader collectieLoader = new CollectieLoader(conceptschemaRepository, collectieRepository);
            result = collectieLoader.loadCollectie(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setUpdatedEntries(crawlResult.getUpdatedEntries() + result.getSuccessResult().getUpdatedEntries());
            crawlResult.setUnchangedEntries(crawlResult.getUnchangedEntries() + result.getSuccessResult().getUnchangedEntries());
            crawlResult.setNewEntries(crawlResult.getNewEntries() + result.getSuccessResult().getNewEntries());
            crawlResult.setPages(crawlResult.getPages() + result.getSuccessResult().getPages());

            BronLoader bronLoader = new BronLoader(bronRepository);
            result = bronLoader.loadBron(catalogService);
        }

        if (result.isSuccess()) {
            crawlResult.setUpdatedEntries(crawlResult.getUpdatedEntries() + result.getSuccessResult().getUpdatedEntries());
            crawlResult.setUnchangedEntries(crawlResult.getUnchangedEntries() + result.getSuccessResult().getUnchangedEntries());
            crawlResult.setNewEntries(crawlResult.getNewEntries() + result.getSuccessResult().getNewEntries());
            crawlResult.setPages(crawlResult.getPages() + result.getSuccessResult().getPages());

            WaardelijstLoader waardelijstLoader = new WaardelijstLoader(waardelijstRepository, conceptschemaRepository, conceptRepository);
            result = waardelijstLoader.loadWaardelijsten(catalogService);
        }

        crawlResult.setUpdatedEntries(crawlResult.getUpdatedEntries() + result.getSuccessResult().getUpdatedEntries());
        crawlResult.setUnchangedEntries(crawlResult.getUnchangedEntries() + result.getSuccessResult().getUnchangedEntries());
        crawlResult.setNewEntries(crawlResult.getNewEntries() + result.getSuccessResult().getNewEntries());
        crawlResult.setPages(crawlResult.getPages() + result.getSuccessResult().getPages());

        if (!result.isSuccess()) {
            crawlResult.setMessage(result.getFailureResult().getMessage());
        }

        result = OperationResult.success(crawlResult);

        if (result.isSuccess()) {
            procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            procesResult = result.getFailureResult();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(procesResult);
        }
    }
}
