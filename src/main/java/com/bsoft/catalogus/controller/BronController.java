package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.ProcesResult;
import com.bsoft.catalogus.repository.BronRepository;
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
public class BronController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private BronRepository bronRepository;

    @RequestMapping(value = "/bron")
    public ResponseEntity<ProcesResult> getBronnen() {
        log.info("BronController getBronnen");
        BronLoader bronLoader = new BronLoader(bronRepository);

        OperationResult<ProcesResult> result = bronLoader.loadBron(catalogService);
        if (result.isSuccess()) {
            ProcesResult procesResult = result.getSuccessResult();
            return ResponseEntity.ok(procesResult);
        } else {
            log.error("BronController getBronnen error: {}", result.getFailureResult().getMessage());
            String message = "error during proces";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
