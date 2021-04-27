package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class ConceptLoader {

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptRepository conceptRepository;

    public ConceptLoader(ConceptschemaRepository conceptschemaRepository,
                         ConceptRepository conceptRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptRepository = conceptRepository;
    }

    public OperationResult loadConcept(final CatalogService catalogService) {

        String uri = null; //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = "2021-04-14";
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = Arrays.asList("concepten");
        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(0);

        int conceptschemanr = 0;
        try {
            List<ConceptschemaDTO> conceptschemaDTOS = conceptschemaRepository.findAll();
            log.info("ConceptLoader found: {} conceptschemas", conceptschemaDTOS.size());
            for (ConceptschemaDTO conceptschemaDTO: conceptschemaDTOS) {
                conceptschemanr++;
                uri = conceptschemaDTO.getUri();
                log.info("ConceptLoader nr: {} conceptschema uri: {}", conceptschemanr, uri);
                goOn = true;
                procesResult.setMore(goOn);
                while (goOn) {
                    log.info("ConceptLoader page: {}", page);
                    procesResult = getPage(catalogService, conceptschemaDTO, procesResult, uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

                    goOn = procesResult.isMore();
                    if (goOn) {
                        page++;
                    }
                }
            }
        } catch (Exception ex) {
            procesResult.setStatus(1);
            procesResult.setMessage(ex.getMessage());
            procesResult.setMore(false);
        }

        return OperationResult.success(procesResult);
    }

    @Transactional
    public ProcesResult getPage(final CatalogService catalogService,
                                 final ConceptschemaDTO conceptschemaDTO,
                                 ProcesResult procesResult,
                                 final String uri,
                                 final String gepubliceerdDoor,
                                 final String geldigOp,
                                 final Integer page,
                                 final Integer pageSize,
                                 final List<String> expandScope) {

        OperationResult<InlineResponse200> result = null;
        boolean nextPage = false;

        result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

        if (result.isSuccess()) {
            InlineResponse200 inlineResponse200 = result.getSuccessResult();
            InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();


            List<Conceptschema> conceptschemas = embedded.getConceptschemas();

            for (Conceptschema conceptschema: conceptschemas) {
                if (!conceptschema.getUri().equals(conceptschemaDTO.getUri())) {
                    log.error("ConceptLoader Unexpected conceptschema with uri: {}, expected uri: {}", conceptschema.getUri(), conceptschemaDTO.getUri());
                } else {
                    JsonNullable<List<Concept>> concepten = conceptschema.getEmbedded().getConcepten();
                    if (concepten.isPresent()) {
                        for (Concept concept: concepten.get()) {
                            Optional<ConceptDTO> conceptOptional = conceptRepository.findByUri(concept.getUri());
                            if (!((conceptOptional != null) && conceptOptional.isPresent())) {  // if not exists
                                ConceptDTO conceptDTO = new ConceptDTO();
                                conceptDTO.setUri(concept.getUri());
                                conceptDTO.setType(concept.getType());
                                conceptDTO.setNaam(concept.getNaam());
                                conceptDTO.setTerm(concept.getTerm());
                                conceptDTO.setUitleg(concept.getUitleg().isPresent() ? concept.getUitleg().get() : null);
                                conceptDTO.setDefinitie(concept.getDefinitie().isPresent() ? concept.getDefinitie().get() : null);
                                conceptDTO.setEigenaar(concept.getEigenaar().isPresent() ? concept.getEigenaar().get() : null);
                                conceptDTO.setConceptschema(conceptschemaDTO);
                                conceptDTO.setBegindatumGeldigheid(concept.getBegindatumGeldigheid());
                                conceptDTO.setEinddatumGeldigheid(concept.getEinddatumGeldigheid().isPresent() ? concept.getEinddatumGeldigheid().get() : null);
                                conceptDTO.setMetadata(concept.getMetadata());

                                conceptRepository.save(conceptDTO);
                                procesResult.setEntries(procesResult.getEntries() + 1);
                            } else {
                                log.info("ConceptLoader concept with uri: {} already exists", concept.getUri());
                            }
                        }
                    } else {
                        log.info("ConceptLoader No concepten found for conceptschema with uri: {}", conceptschemaDTO.getUri());
                    }
                }
            }

            if (result.getSuccessResult().getLinks().getNext() != null) {
                if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.info("ConceptLoader page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                }
            }
            procesResult.setPages(page);

        } else {
            log.info("ConceptLoader getPage: no result stop processing");
        }
        procesResult.setMore(nextPage);
        return procesResult;
    }

}
