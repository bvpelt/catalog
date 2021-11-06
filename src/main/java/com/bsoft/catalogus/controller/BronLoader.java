package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.BronRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class BronLoader {

    private BronRepository bronRepository;

    public BronLoader(BronRepository bronRepository) {
        this.bronRepository = bronRepository;
    }

    public OperationResult loadBron(final CatalogService catalogService) {

        String uri = null;              // "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; // "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = null;         // new CatalogUtil().getCurrentDate();
        String zoekTerm = null;
        Integer page = 1;
        Integer pageSize = 10;
        boolean goOn = true;

        // Initialize proces result
        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setNewEntries(0);
        procesResult.setUnchangedEntries(0);
        procesResult.setUpdatedEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(ProcesResult.SUCCESS);

        try {
            while (goOn) {
                log.info("BronLoader loadBron page: {}", page);
                getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize);

                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("BronLoader loadBron error loading concept: {}", ex);
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
                         final Integer pageSize) {

        OperationResult<InlineResponse2003> result;
        boolean nextPage = false;

        Instant start = Instant.now();
        result = catalogService.getBron(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize);
        Instant finish = Instant.now();
        long time = Duration.between(start, finish).toMillis();
        log.info("Timing data getbron: {} ms ", time);

        if ((result != null) && result.isSuccess()) {
            InlineResponse2003 inlineResponse2003 = result.getSuccessResult();
            InlineResponse2003Embedded embedded = inlineResponse2003.getEmbedded();
            List<Bron> bronnen = embedded.getBronnen();

            if ((bronnen != null) && (!bronnen.isEmpty())) {  // only proces if list contains entries
                persistBronnen(bronnen, procesResult);
            }

            if (procesResult.getStatus() == ProcesResult.SUCCESS) {
                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (!((result.getSuccessResult().getLinks().getNext().getHref().isPresent() && (result.getSuccessResult().getLinks().getNext().getHref().get() == null)) || (!result.getSuccessResult().getLinks().getNext().getHref().isPresent()))) {
                        nextPage = true;
                        log.debug("BronLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(procesResult.getPages() + 1);
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);

            if ((result != null) && result.getFailureResult() != null) {
                log.debug("BronLoader getPage, result: " + result.getFailureResult().toString());
                procesResult.setMessage(result.getFailureResult().toString());
            } else {
                procesResult.setMessage("Error during processing request /bronnen");
            }
            log.info("BronLoader getPage: no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    private void persistBronnen(final List<Bron> bronnen, final ProcesResult procesResult) {
        int maxsize =  bronnen.size();
            log.info("BronLoader persistConceptSchemas number found: {}", maxsize);

            for (int i = 0; i < maxsize; i++) {
                log.debug("CollectieLoader persistCollecties: begin found collectie: {}", bronnen.get(i).getUri());
                BronDTO bronDTO = convertToBronDTO(bronnen.get(i), procesResult);
                log.debug("CollectieLoader persistCollecties: end   found collectie: {}", bronDTO == null ? "(null)" : bronDTO.getUri());
            }
    }

    @Transactional
    public BronDTO convertToBronDTO(final Bron bron, ProcesResult procesResult) {
        log.debug("BronLoader convertToBronDTO:: received uri: {}", bron.getUri());
        BronDTO savedBron = null;
        BronDTO bronDTO = new BronDTO();
        boolean newEntry = false;

        // If bron with uri already exists
        //   update bron
        // else
        //   create new bron
        // fi
        Optional<BronDTO> optionalBronDTO = bronRepository.findByUri(bron.getUri());
        if (optionalBronDTO.isPresent()) {
            log.debug("BronLoader convertToBronDTO:: found uri: {} - updating", bron.getUri());
            bronDTO = optionalBronDTO.get();
        } else {
            log.debug("BronLoader convertToBronDTO: found uri: {} - new", bron.getUri());
            bronDTO = new BronDTO();
            newEntry = true;
        }

        try {
            boolean attributesChanged = true; // always true for new entry

            if (!newEntry) {
                attributesChanged = changedAttributes(bron, bronDTO);
            }

            log.debug("BronLoader convertToBronDTO newEntry                     : {}", newEntry);
            log.debug("BronLoader convertToBronDTO attributesChanged            : {}", attributesChanged);

            if (attributesChanged || newEntry) {
                log.info("BronLoader convertToBronDTO new or changed attributes");
                //
                // for new all attributes and for existing some attributes
                //
                bronDTO.setUri(bron.getUri());
                bronDTO.setTitel(bron.getTitel());
                bronDTO.setWebpagina(bron.getWebpagina().isPresent() ? bron.getWebpagina().get() : null);
                bronDTO.setResource(bron.getResource().isPresent() ? bron.getResource().get() : null);
                bronDTO.setType(bron.getType());
                bronDTO.setBegindatumGeldigheid(bron.getBegindatumGeldigheid());
                bronDTO.setEinddatumGeldigheid(bron.getEinddatumGeldigheid().isPresent() ? bron.getEinddatumGeldigheid().get() : null);
                bronDTO.setEigenaar(bron.getEigenaar().isPresent() ? bron.getEigenaar().get() : null);
                bronDTO.setMetadata(bron.getMetadata());

                savedBron = bronRepository.save(bronDTO);
            } else {
                log.debug("BronLoader convertToBronDTO existing, not new or changed attributes");
                savedBron = bronDTO;
            }

            if (newEntry) { // new entry
                procesResult.setNewEntries(procesResult.getNewEntries() + 1);
            } else if (attributesChanged) { // changed
                procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }
        } catch (Exception e) {
            log.error("BronLoader convertToBronDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedBron;
    }

    private boolean changedAttributes(final Bron bron, final BronDTO bronDTO) {
        boolean changed = false;

        changed = !bron.getUri().equals(bronDTO.getUri());

        if (!changed) {
            changed = !bron.getTitel().equals(bronDTO.getTitel());
        }

        if (!changed) {
            if (bron.getWebpagina().isPresent() && (bron.getWebpagina().get() != null)) {
                changed = !bron.getWebpagina().get().equals(bronDTO.getWebpagina());
            } else { // webpagina not present == null
                if (bronDTO.getWebpagina() != null) {
                    changed = bronDTO.getWebpagina().length() > 0;
                }
            }
        }

        if (!changed) {
            if (bron.getResource().isPresent() && (bron.getResource().get() != null)) {
                changed = !bron.getResource().get().equals(bronDTO.getResource());
            } else { // resource not present == null
                if (bronDTO.getResource() != null) {
                    changed = bronDTO.getResource().length() > 0;
                }
            }
        }

        if (!changed) {
            changed = !bron.getType().equals(bronDTO.getType());
        }

        if (!changed) {
            changed = !bron.getBegindatumGeldigheid().equals(bronDTO.getBegindatumGeldigheid());
        }

        if (!changed) {
            if (bron.getEinddatumGeldigheid().isPresent() && (bron.getEinddatumGeldigheid().get() != null)) {
                changed = !bron.getEinddatumGeldigheid().get().equals(bronDTO.getEinddatumGeldigheid());
            } else { // einddatum not present == null
                if (bronDTO.getEinddatumGeldigheid() != null) {
                    changed = bronDTO.getEinddatumGeldigheid().length() > 0;
                }
            }
        }

        if (!changed) {
            if (bron.getEigenaar().isPresent() && (bron.getEigenaar().get() != null)) {
                changed = !bron.getEigenaar().get().equals(bronDTO.getEigenaar());
            } else { // einddatum not present == null
                if (bronDTO.getEigenaar() != null) {
                    changed = bronDTO.getEigenaar().length() > 0;
                }
            }
        }

        if (!changed) {
            changed = !bron.getMetadata().equals(bronDTO.getMetadata());
        }

        return changed;
    }
}
