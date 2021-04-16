package com.bsoft.catalogus.services;

import com.bsoft.catalogus.model.InlineResponse200;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CatalogService {

    private final CatalogEndpoint catalogEndpoint;

    public OperationResult<InlineResponse200> getConceptschemas(String uri,
                                                                String gepubliceerdDoor,
                                                                String geldigOp,
                                                                Integer page,
                                                                Integer pageSize,
                                                                List<String> expandScope) {
        return catalogEndpoint.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
    }
}
