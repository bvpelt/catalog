package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.ProcesResult;
import com.bsoft.catalogus.repository.CollectieRepository;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor
@Slf4j
@RestController
public class ConceptController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ConceptschemaRepository conceptschemaRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private CollectieRepository collectieRepository;

    @RequestMapping(value = "/conceptschemas/concept")
    public ResponseEntity<ProcesResult> getConcepten() {
        log.info("getConcepten");
        ConceptLoader conceptLoader = new ConceptLoader(conceptschemaRepository, conceptRepository, collectieRepository);
        OperationResult<ProcesResult> result = conceptLoader.loadConcept(catalogService);
        if (result.isSuccess()) {
            ProcesResult procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            String message = "error during proces";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
