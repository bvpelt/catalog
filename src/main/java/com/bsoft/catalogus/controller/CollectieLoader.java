package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.CollectieRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.StringChanged;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class CollectieLoader {

    private CollectieRepository collectieRepository;

    public CollectieLoader(CollectieRepository collectieRepository) {
        this.collectieRepository = collectieRepository;
    }

    public OperationResult<ProcesResult> loadCollectie(final CatalogService catalogService) {

        String uri = null;              //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = null;         //new CatalogUtil().getCurrentDate();
        String zoekTerm = null;
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = null; // Arrays.asList("concepten", "collecties");
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
                log.info("CollectieLoader loadCollectie page: {}", page);

                getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("CollectieLoader loadCollectie error loading collectie: {}", ex);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(ex.getMessage());
            procesResult.setMore(false);
        }

        return OperationResult.success(procesResult);
    }

    private void getPage(final CatalogService catalogService,
                         final ProcesResult procesResult,
                         final String uri,
                         final String gepubliceerdDoor,
                         final String geldigOp,
                         final String zoekTerm,
                         final Integer page,
                         final Integer pageSize,
                         final List<String> expandScope) {

        OperationResult<InlineResponse2001> result = null;
        boolean nextPage = false;

        Instant start = Instant.now();
        result = catalogService.getCollecties(uri, gepubliceerdDoor, geldigOp, zoekTerm, null, page, pageSize, expandScope);
        Instant finish = Instant.now();
        long time = Duration.between(start, finish).toMillis();
        log.debug("Timing data getcollecties: {} ms ", time);

        if ((result != null) && result.isSuccess()) {
            InlineResponse2001 inlineResponse2001 = result.getSuccessResult();
            InlineResponse2001Embedded embedded = inlineResponse2001.getEmbedded();
            List<Collectie> collecties = embedded.getCollecties();

            persistCollecties(collecties, procesResult);

            if (procesResult.getStatus() == ProcesResult.SUCCESS) {
                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (!((result.getSuccessResult().getLinks().getNext().getHref().isPresent() && (result.getSuccessResult().getLinks().getNext().getHref().get() == null)) || (!result.getSuccessResult().getLinks().getNext().getHref().isPresent()))) {
                        nextPage = true;
                        log.info("CollectieLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(procesResult.getPages() + 1);
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);

            if ((result != null) && result.getFailureResult() != null) {
                log.debug("CollectieLoader getPage, result: " + result.getFailureResult().toString());
                procesResult.setMessage(result.getFailureResult().toString());
            } else {
                procesResult.setMessage("Error during processing request /collecties");
            }
            log.info("CollectieLoader getPage: no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    private void persistCollecties(final List<Collectie> collecties, final ProcesResult procesResult) {
        int maxsize = collecties.size();
        log.info("CollectieLoader persistCollecties number found: {}", maxsize);

        for (int i = 0; i < maxsize; i++) {
            log.debug("CollectieLoader persistCollecties: begin found collectie: {}", collecties.get(i).getNaam());
            CollectieDTO collectieDTO = convertToCollectieDTO(collecties.get(i), procesResult);
            log.debug("CollectieLoader persistCollecties: end   found collectie: {}", collectieDTO == null ? "(null)" : collectieDTO.getUri());
        }
    }

    @Transactional
    public CollectieDTO convertToCollectieDTO(final Collectie collectie, final ProcesResult procesResult) {
        log.info("CollectieLoader convertToCollectieDTO:: received uri: {} collectie: {}", collectie.getUri(), collectie.getNaam());
        CollectieDTO savedCollectie = null;
        CollectieDTO collectieDTO = null;
        boolean newEntry = false;

        Optional<CollectieDTO> optionalCollectieDTO = collectieRepository.findByUri(collectie.getUri());
        if (optionalCollectieDTO.isPresent()) {
            log.debug("CollectieLoader convertToCollectieDTO:: found uri: {} - updating", collectie.getUri());
            collectieDTO = optionalCollectieDTO.get();
        } else {
            log.debug("CollectieLoader convertToCollectieDTO: found uri: {} - new", collectie.getUri());
            collectieDTO = new CollectieDTO();
            newEntry = true;
        }

        try {
            boolean attributesChanged = true; // always true for new entry

            if (!newEntry) {
                attributesChanged = changedAttributes(collectie, collectieDTO);
            }

            log.debug("CollectieLoader convertToCollectieDTO newEntry                     : {}", newEntry);
            log.debug("CollectieLoader convertToCollectieDTO attributesChanged            : {}", attributesChanged);

            if (attributesChanged || newEntry) {
                log.info("CollectieLoader convertToCollectieDTO new or changed attributes");
                //
                // for new all attributes and for existing some attributes
                //
                collectieDTO.setUri(collectie.getUri());
                collectieDTO.setType(collectie.getType());
                collectieDTO.setNaam(collectie.getNaam());
                collectieDTO.setTerm(collectie.getTerm());
                collectieDTO.setEigenaar(collectie.getEigenaar());
                collectieDTO.setConceptschema(collectie.getConceptschema());
                collectieDTO.setBegindatumGeldigheid(collectie.getBegindatumGeldigheid());
                collectieDTO.setEinddatumGeldigheid(collectie.getEinddatumGeldigheid().isPresent() ? collectie.getEinddatumGeldigheid().get() : null);
                collectieDTO.setMetadata(collectie.getMetadata());

                savedCollectie = collectieRepository.save(collectieDTO);
            } else {
                log.debug("CollectieLoader convertToCollectieDTO existing, not new or changed attributes");
                savedCollectie = collectieDTO;
            }

            if (newEntry) { // new entry
                procesResult.setNewEntries(procesResult.getNewEntries() + 1);
            } else if (attributesChanged) { // changed
                procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }

        } catch (Exception e) {
            log.error("CollectieLoader convertToCollectieDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }

        return savedCollectie;
    }


    private boolean changedAttributes(final Collectie collectie, final CollectieDTO collectieDTO) {
        boolean changed = false;

        changed = StringChanged.stringChanged(collectie.getUri(), collectieDTO.getUri());

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getType(), collectieDTO.getType());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getNaam(), collectieDTO.getNaam());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getTerm(), collectieDTO.getTerm());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getEigenaar(), collectieDTO.getEigenaar());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getConceptschema(), collectieDTO.getConceptschema());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getBegindatumGeldigheid(), collectieDTO.getBegindatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getEinddatumGeldigheid(), collectieDTO.getEinddatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(collectie.getMetadata(), collectieDTO.getMetadata());
        }

        return changed;
    }
}
