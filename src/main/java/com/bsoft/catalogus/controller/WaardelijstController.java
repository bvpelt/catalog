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
public class WaardelijstController {

    @Autowired
    CatalogService catalogService;

    @Autowired
    WaardelijstRepository waardelijstRepository;

    @Autowired
    ToelichtingRepository toelichtingRepository;

    @Autowired
    TrefwoordRepository trefwoordRepository;

    @Autowired
    ConceptRepository conceptRepository;

    @RequestMapping(value = "/waardelijsten", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcesResult> getWaardelijsten() {
        log.info("WaardelijstController getWaardelijsten");
        ProcesResult procesResult = null;

        WaardelijstLoader waardelijstLoader = new WaardelijstLoader(waardelijstRepository, conceptRepository, trefwoordRepository, toelichtingRepository);
        OperationResult<ProcesResult> result = waardelijstLoader.loadWaardelijsten(catalogService);
        if (result.isSuccess()) {
            procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            log.error("WaardelijstController getWaardelijsten error: {}", result.getFailureResult().getMessage());
            procesResult = result.getFailureResult();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(procesResult);
        }
    }
}
