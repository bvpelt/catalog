package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.BronRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.CatalogUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
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
        String geldigOp = new CatalogUtil().getCurrentDate();
        Integer page = 1;
        Integer pageSize = 10;

        boolean goOn = true;

        // Initialize proces result
        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(ProcesResult.SUCCESS);

        try {
            while (goOn) {
                log.info("BronLoader loadBron page: {}", page);
                procesResult = getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, page, pageSize);

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

    private ProcesResult getPage(final CatalogService catalogService,
                                 ProcesResult procesResult,
                                 final String uri,
                                 final String gepubliceerdDoor,
                                 final String geldigOp,
                                 final Integer page,
                                 final Integer pageSize) {

        OperationResult<InlineResponse2003> result = null;
        boolean nextPage = false;

        result = catalogService.getBron(uri, gepubliceerdDoor, geldigOp, page, pageSize);

        if (result.isSuccess()) {
            InlineResponse2003 inlineResponse2003 = result.getSuccessResult();

            InlineResponse2003Embedded embedded = inlineResponse2003.getEmbedded();
            List<Bron> bronnen = embedded.getBronnen();
            persistBronnen(bronnen, procesResult);

            if (procesResult.getStatus() == 0) {
                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                        nextPage = true;
                        log.debug("BronLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(procesResult.getPages() + 1);
            }
        } else {
            log.debug("BronLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
        return procesResult;
    }

    private void persistBronnen(final List<Bron> bronnen, ProcesResult procesResult) {
        int maxsize = bronnen.size();
        log.info("BronLoader persistConceptSchemas number found: {}", maxsize);

        int i = 0;
        boolean goOn = true;

        while ((i < maxsize) && goOn) {
            log.debug("BronLoader persistConceptSchemas: begin found conceptschema: {}", bronnen.get(i).getUri());
            BronDTO bronDTO = convertToBronDTO(bronnen.get(i), procesResult);
            if (bronDTO == null) {
                goOn = false;
            }
            log.debug("BronLoader persistConceptSchemas: end   found conceptschema: {}", bronDTO == null ? "(null)" : bronDTO.getUri());
            i++;
        }
    }

    @Transactional
    public BronDTO convertToBronDTO(final Bron bron, ProcesResult procesResult) {
        log.debug("BronLoader convertToBronDTO:: received uri: {}", bron.getUri());
        BronDTO savedBron = null;
        BronDTO bronDTO = new BronDTO();

        // If bron with uri already exists
        //   update bron
        // else
        //   create new bron
        // fi
        Optional<BronDTO> optionalBronDTO = bronRepository.findByUri(bron.getUri());
        if (optionalBronDTO.isPresent()) {
            log.debug("BronLoader convertToBronDTO:: found uri: {} - updating", bron.getUri());
            bronDTO = optionalBronDTO.get();
        }

        try {
            bronDTO.setTitel(bron.getTitel());
            bronDTO.setMetadata(bron.getMetadata());
            bronDTO.setWebpagina(bron.getWebpagina().isPresent() ? bron.getWebpagina().get() : null);
            bronDTO.setResource(bron.getResource().isPresent() ? bron.getResource().get() : null);
            bronDTO.setBegindatumGeldigheid(bron.getBegindatumGeldigheid());
            bronDTO.setEinddatumGeldigheid(bron.getEinddatumGeldigheid().isPresent() ? bron.getEinddatumGeldigheid().get() : null);
            bronDTO.setType(bron.getType());
            bronDTO.setEigenaar(bron.getEigenaar().isPresent() ? bron.getEigenaar().get() : null);
            bronDTO.setUri(bron.getUri());

            log.debug("BronLoader convertToBronDTO: before 01 save conceptschemaDTO");
            savedBron = bronRepository.save(bronDTO);
            log.debug("BronLoader convertToBronDTO: after 01 save conceptschemaDTO");
            procesResult.setEntries(procesResult.getEntries() + 1);
        } catch (Exception e) {
            log.error("BronLoader convertToBronDTO error at processing: {}", e.getMessage());
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedBron;
    }
}
