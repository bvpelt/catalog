package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.InlineResponse200;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;

import java.util.Arrays;
import java.util.List;

public class ConceptSchemaLoader {

    public  OperationResult<InlineResponse200> loadConceptSchemas(CatalogService catalogService) {

        String uri = "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = "2021-04-14";
        Integer page = 1;
        Integer pageSize = 50;
        List<String> expandScope = Arrays.asList("collecties");

        OperationResult<InlineResponse200> result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);


        return result;
    }
}
