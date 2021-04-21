package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import liquibase.pro.packaged.C;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.*;

@NoArgsConstructor
@Slf4j
@RestController
public class ConceptschemaController {

    @Autowired
    CatalogService catalogService;

    @Autowired
    ConceptschemaRepository conceptschemaRepository;

    @Autowired
    ConceptschemaTypeRepository conceptschemaTypeRepository;

    @RequestMapping(value = "/conceptschemas")
    public ResponseEntity<ProcesResult> getConceptschemas() {
        log.info("getConceptschemas");
        ConceptSchemaLoader conceptSchemaLoader = new ConceptSchemaLoader(conceptschemaRepository, conceptschemaTypeRepository);
        OperationResult<ProcesResult> result = conceptSchemaLoader.loadConceptSchemas(catalogService);
        if (result.isSuccess()) {
            ProcesResult procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            String message = "error during proces";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
