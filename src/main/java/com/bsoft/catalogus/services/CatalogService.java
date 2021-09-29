package com.bsoft.catalogus.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CatalogService {

    private final CatalogEndpoint catalogEndpoint;

    public OperationResult getConceptschemas(final String uri,
                                             final String gepubliceerdDoor,
                                             final String geldigOp,
                                             final String zoekTerm,
                                             final Integer page,
                                             final Integer pageSize,
                                             final List<String> expandScope) {
        return catalogEndpoint.getConceptschemas(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
    }

    public OperationResult getCollecties(final String uri,
                                         final String gepubliceerdDoor,
                                         final String geldigOp,
                                         final String zoekTerm,
                                         final String conceptschema,
                                         final Integer page,
                                         final Integer pageSize,
                                         final List<String> expandScope) {
        return catalogEndpoint.getCollecties(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, page, pageSize, expandScope);
    }

    public OperationResult getConcepten(final String uri,
                                        final String gepubliceerdDoor,
                                        final String geldigOp,
                                        final String zoekTerm,
                                        final String conceptschema,
                                        final String collectie,
                                        final String waardelijst,
                                        final Integer page,
                                        final Integer pageSize) {
        return catalogEndpoint.getConcepten(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, collectie, waardelijst, page, pageSize);
    }

    public OperationResult getBron(final String uri,
                                   final String gepubliceerdDoor,
                                   final String geldigOp,
                                   final String zoekTerm,
                                   final Integer page,
                                   final Integer pageSize) {
        return catalogEndpoint.getBron(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize);
    }

    public OperationResult getWaardelijst(final String uri,
                                          final String gepubliceerdDoor,
                                          final String zoekTerm,
                                          final List<String> expandScope,
                                          final Integer page,
                                          final Integer pageSize) {
        return catalogEndpoint.getWaardenlijst(uri, gepubliceerdDoor, zoekTerm, expandScope, page, pageSize);
    }

}
