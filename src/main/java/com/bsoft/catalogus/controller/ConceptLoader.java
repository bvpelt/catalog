package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.CollectieRepository;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.CatalogUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class ConceptLoader {

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptRepository conceptRepository;

    private CollectieRepository collectieRepository;

    public ConceptLoader(ConceptschemaRepository conceptschemaRepository,
                         ConceptRepository conceptRepository,
                         CollectieRepository collectieRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptRepository = conceptRepository;
        this.collectieRepository = collectieRepository;
    }

    public OperationResult loadConcept(final CatalogService catalogService) {

        String uri = null; //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = new CatalogUtil().getCurrentDate();
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = Arrays.asList("concepten", "collecties");
        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(0);

        int conceptschemanr = 0;
        try {
            List<ConceptschemaDTO> conceptschemaDTOS = conceptschemaRepository.findAll();
            log.info("ConceptLoader loadConcept start found: {} conceptschemas", conceptschemaDTOS.size());
            for (ConceptschemaDTO conceptschemaDTO : conceptschemaDTOS) {
                conceptschemanr++;
                uri = conceptschemaDTO.getUri();
                log.info("ConceptLoader loadConcept nr: {} conceptschema uri: {}", conceptschemanr, uri);
                goOn = true;
                procesResult.setMore(goOn);
                while (goOn) {
                    log.info("ConceptLoader loadConcept page: {}", page);
                    procesResult = getPage(catalogService, conceptschemaDTO, procesResult, uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

                    goOn = procesResult.isMore();
                    if (goOn) {
                        page++;
                    }
                    procesResult.setPages(procesResult.getPages() + 1);
                }
            }
            log.info("ConceptLoader loadConcept end  found: {} conceptschemas", conceptschemaDTOS.size());
        } catch (Exception ex) {
            log.error("ConceptLoader loadConcept error loading concept: {}", ex);
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

        OperationResult result = null;
        boolean nextPage = false;

        result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

        if (result.isSuccess()) {
            InlineResponse200 inlineResponse200 = ((OperationResult<InlineResponse200>) result).getSuccessResult();
            InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();


            List<Conceptschema> conceptschemas = embedded.getConceptschemas();

            for (Conceptschema conceptschema : conceptschemas) {
                if (!conceptschema.getUri().equals(conceptschemaDTO.getUri())) {
                    log.error("ConceptLoader getPage Unexpected conceptschema with uri: {}, expected uri: {}", conceptschema.getUri(), conceptschemaDTO.getUri());
                } else {
                    JsonNullable<List<Concept>> concepten = conceptschema.getEmbedded().getConcepten();
                    if (concepten != null && concepten.isPresent() && concepten.get() != null) {
                        log.debug("ConceptLoader getPage number of concepten: {}", concepten.get().size());
                        for (Concept concept : concepten.get()) {
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
                                log.debug("ConceptLoader getPage concept with uri: {} already exists", concept.getUri());
                            }
                        }
                    } else {
                        log.debug("ConceptLoader getPage no concepten found for conceptschema with uri: {}", conceptschemaDTO.getUri());
                    }

                    JsonNullable<List<Collectie>> collecties = conceptschema.getEmbedded().getCollecties();
                    if (collecties != null && collecties.isPresent() && collecties.get() != null) {
                        log.debug("ConceptLoader getPage number of collecties: {}", collecties.get().size());
                        for (Collectie collectie : collecties.get()) {
                            Optional<CollectieDTO> collectieOptional = collectieRepository.findByUri(collectie.getUri());

                            if (!((collectieOptional != null) && collectieOptional.isPresent())) {  // if not exists
                                CollectieDTO collectieDTO = new CollectieDTO();
                                collectieDTO.setUri(collectie.getUri());
                                collectieDTO.setType(collectie.getType());
                                collectieDTO.setTerm(collectie.getTerm());
                                collectieDTO.setEigenaar(collectie.getEigenaar());
                                collectieDTO.setConceptschema(conceptschemaDTO);
                                collectieDTO.setBegindatumGeldigheid(collectie.getBegindatumGeldigheid());
                                collectieDTO.setEinddatumGeldigheid(collectie.getEinddatumGeldigheid().isPresent() ? collectie.getEinddatumGeldigheid().get() : null);
                                collectieDTO.setMetadata(collectie.getMetadata());

                                collectieRepository.save(collectieDTO);
                                procesResult.setEntries(procesResult.getEntries() + 1);
                            } else {
                                log.debug("ConceptLoader getPage concept with uri: {} already exists", collectie.getUri());
                            }
                        }
                    } else {
                        log.debug("ConceptLoader getPage no collecties found for conceptschema with uri: {}", conceptschemaDTO.getUri());
                    }
                }
            }

            if (inlineResponse200.getLinks().getNext() != null) {
                if (inlineResponse200.getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.debug("ConceptLoader getPage  page: {} next: {}", page, inlineResponse200.getLinks().getNext().getHref());
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
