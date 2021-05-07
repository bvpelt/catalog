package com.bsoft.catalogus.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CatalogService {

    private final CatalogEndpoint catalogEndpoint;

    public OperationResult getConceptschemas(String uri,
                                             String gepubliceerdDoor,
                                             String geldigOp,
                                             Integer page,
                                             Integer pageSize,
                                             List<String> expandScope) {
        return catalogEndpoint.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
    }

    public OperationResult getBron(String uri,
                                   String gepubliceerdDoor,
                                   String geldigOp,
                                   Integer page,
                                   Integer pageSize) {
        return catalogEndpoint.getBron(uri, gepubliceerdDoor, geldigOp, page, pageSize);
    }

}
