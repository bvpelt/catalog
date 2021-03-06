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

        String uri = null; //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = new CatalogUtil().getCurrentDate();
        Integer page = 1;
        Integer pageSize = 10;

        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(0);

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
            procesResult.setStatus(1);
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
            persistBronnen(bronnen);

            if (result.getSuccessResult().getLinks().getNext() != null) {
                if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.info("BronLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                }
            }
            procesResult.setPages(page);
            procesResult.setEntries((page - 1) * pageSize + result.getSuccessResult().getEmbedded().getBronnen().size());
        } else {
            log.info("BronLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
        return procesResult;
    }

    private void persistBronnen(final List<Bron> bronnen) {
        log.info("BronLoader persistConceptSchemas number found: {}", bronnen.size());

        for (int i = 0; i < bronnen.size(); i++) {
            log.debug("BronLoader persistConceptSchemas: begin found conceptschema: {}", bronnen.get(i).getUri());
            BronDTO bronDTO = convertToBronDTO(bronnen.get(i));
            log.debug("BronLoader persistConceptSchemas: end   found conceptschema: {}", bronDTO == null ? "(null)" : bronDTO.getUri());
        }
    }

    @Transactional
    public BronDTO convertToBronDTO(final Bron bron) {
        log.info("BronLoader convertToBronDTO:: received uri: {}", bron.getUri());
        BronDTO savedBron = null;
        BronDTO bronDTO = new BronDTO();

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
        } catch (Exception e) {
            log.error("BronLoader convertToBronDTO error at processing: {}", e.getMessage());
        }
        return savedBron;
    }
}
