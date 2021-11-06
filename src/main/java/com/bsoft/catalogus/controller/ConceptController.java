package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.ProcesResult;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ToelichtingRepository;
import com.bsoft.catalogus.repository.TrefwoordRepository;
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
public class ConceptController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private TrefwoordRepository trefwoordRepository;

    @Autowired
    private ToelichtingRepository toelichtingRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @RequestMapping(value = "/concepten", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcesResult> getConcepten() {
        log.info("ConceptController getConcepten");
        ProcesResult procesResult = null;

        ConceptLoader conceptLoader = new ConceptLoader(trefwoordRepository, toelichtingRepository, conceptRepository);
        OperationResult<ProcesResult> result = conceptLoader.loadConcept(catalogService);
        if (result.isSuccess()) {
            procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            log.error("ConceptController getBronnen error: {}", result.getFailureResult().getMessage());
            procesResult = result.getFailureResult();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(procesResult);
        }
    }
}
