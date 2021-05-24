package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class ConceptLoader {

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptRepository conceptRepository;


    public ConceptLoader(final ConceptschemaRepository conceptschemaRepository,
                         final ConceptRepository conceptRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptRepository = conceptRepository;
    }

    public OperationResult loadConcept(final CatalogService catalogService) {

        String uri = null;               //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null;  //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = null;          //new CatalogUtil().getCurrentDate();
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = null; //Arrays.asList("concepten", "collecties");
        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setNewEntries(0);
        procesResult.setUnchangedEntries(0);
        procesResult.setUpdatedEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(ProcesResult.SUCCESS);

        try {
            while (goOn) {
                log.info("ConceptLoader loadConcept page: {}", page);
                getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("ConceptLoader loadConcept error loading concept: {}", ex);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(ex.getMessage());
            procesResult.setMore(false);
        }

        return OperationResult.success(procesResult);
    }

    @Transactional
    public void getPage(final CatalogService catalogService,
                        final ProcesResult procesResult,
                        final String uri,
                        final String gepubliceerdDoor,
                        final String geldigOp,
                        final Integer page,
                        final Integer pageSize,
                        final List<String> expandScope) {

        OperationResult<InlineResponse2002> result = null;
        boolean nextPage = false;

        result = catalogService.getConcepten(uri, gepubliceerdDoor, geldigOp, null, null, null, page, pageSize);

        if (result.isSuccess()) {
            InlineResponse2002 inlineResponse2002 = result.getSuccessResult();
            InlineResponse2002Embedded embedded = inlineResponse2002.getEmbedded();
            List<Concept> concepten = embedded.getConcepten();

            persistConcepten(concepten, procesResult);

            if (result.getSuccessResult().getLinks().getNext() != null) {
                if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.info("CollectieLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                }
            }
            procesResult.setPages(procesResult.getPages() + 1);
        } else {
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(result.getFailureResult().toString());
            log.info("ConceptLoader getPage: no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    private void persistConcepten(final List<Concept> concepten, final ProcesResult procesResult) {
        int maxsize = concepten.size();
        log.info("ConceptLoader persistConcepten number found: {}", maxsize);

        for (int i = 0; i < maxsize; i++) {
            log.debug("ConceptLoader persistConcepten: begin found collectie: {}", concepten.get(i).getNaam());
            ConceptDTO conceptDTO = convertToConceptDTO(concepten.get(i), procesResult);
            log.debug("ConceptLoader persistConcepten: end   found collectie: {}", conceptDTO == null ? "(null)" : conceptDTO.getUri());
        }
    }

    @Transactional
    public ConceptDTO convertToConceptDTO(final Concept concept, final ProcesResult procesResult) {
        log.info("ConceptLoader convertToConceptDTO:: received uri: {} collectie: {}", concept.getUri(), concept.getNaam());
        ConceptDTO savedConcept = null;
        ConceptDTO conceptDTO = null;
        boolean newEntry = false;

        Optional<ConceptDTO> optionalConceptDTO = conceptRepository.findByUri(concept.getUri());
        if (optionalConceptDTO.isPresent()) {
            log.debug("ConceptLoader convertToConceptDTO: found uri: {} - updating", concept.getUri());
            conceptDTO = optionalConceptDTO.get();
        } else {
            log.debug("ConceptLoader convertToConceptDTO: found uri: {} - new", concept.getUri());
            conceptDTO = new ConceptDTO();
            newEntry = true;
        }

        try {
            Optional<ConceptschemaDTO> optionalConceptschemaDTO = conceptschemaRepository.findByUri(concept.getConceptschema());
            ConceptschemaDTO conceptschemaDTO = null;
            if (optionalConceptschemaDTO.isPresent()) {
                conceptschemaDTO = optionalConceptschemaDTO.get();
            }

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
            log.debug("ConceptLoader convertToConceptDTO: newEntry: {}, present: {}, changed: {}", newEntry, optionalConceptDTO.isPresent(), optionalConceptDTO.isPresent() && !optionalConceptDTO.get().equals(conceptDTO));

            if (newEntry || (optionalConceptDTO.isPresent() && !optionalConceptDTO.get().equals(conceptDTO))) { // new entry or updated entry
                log.trace("ConceptLoader convertToConceptDTO: before 02 save conceptDTO");
                savedConcept = conceptRepository.save(conceptDTO);
                log.trace("ConceptLoader convertToConceptDTO: after 02 save conceptDTO");
                if (newEntry) { // new entry
                    procesResult.setNewEntries(procesResult.getNewEntries() + 1);
                } else { // changed
                    procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
                }
            } else { // unchanged
                log.debug("ConceptLoader convertToConceptDTO: found uri: {} - unchanged", concept.getUri());
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }
        } catch (Exception e) {
            log.error("ConceptLoader convertToConceptDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedConcept;
    }

}
