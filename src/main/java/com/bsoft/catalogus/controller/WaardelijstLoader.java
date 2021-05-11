package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.repository.WaardelijstRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.CatalogUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class WaardelijstLoader {

    private WaardelijstRepository waardelijstRepository;

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptRepository conceptRepository;

    public WaardelijstLoader(WaardelijstRepository waardelijstRepository,
                             ConceptschemaRepository conceptschemaRepository,
                             ConceptRepository conceptRepository) {
        this.waardelijstRepository = waardelijstRepository;
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptRepository = conceptRepository;
    }

    public OperationResult loadWaardelijsten(final CatalogService catalogService) {

        String uri = null; //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";/;
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = Arrays.asList("waarden");
        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);
        procesResult.setStatus(0);

        try {
            while (goOn) {
                log.info("WaardelijstLoader loadWaardelijsten page: {}", page);
                procesResult = getPage(catalogService, procesResult, uri, gepubliceerdDoor, page, pageSize, expandScope);

                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("ConceptSchemaLoader loadConceptSchemas error: {}", ex);
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

                                 final Integer page,
                                 final Integer pageSize,
                                 final List<String> expandScope) {

        OperationResult<InlineResponse2004> result = null;
        boolean nextPage = false;

        result = catalogService.getWaardelijst(uri, gepubliceerdDoor, expandScope, page, pageSize);

        if (result.isSuccess()) {
            InlineResponse2004 inlineResponse2004 = result.getSuccessResult();
            InlineResponse2004Embedded embedded = inlineResponse2004.getEmbedded();
            List<Waardelijst> waardelijsts = embedded.getWaardelijsten();

            persistWaardelijsten(waardelijsts);

            if (result.getSuccessResult().getLinks().getNext() != null) {
                if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.info("WaardelijstLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                }
            }
            procesResult.setPages(page);
            procesResult.setEntries((page - 1) * pageSize + result.getSuccessResult().getEmbedded().getWaardelijsten().size());
        } else {
            log.info("WaardelijstLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
        return procesResult;
    }

    private void persistWaardelijsten(final List<Waardelijst> waardelijsts) {
        log.info("WaardelijstLoader persistWaardelijsten number found: {}", waardelijsts.size());

        for (int i = 0; i < waardelijsts.size(); i++) {
            log.debug("WaardelijstLoader persistWaardelijsten: begin found conceptschema: {}", waardelijsts.get(i).getNaam());
            WaardelijstDTO waardelijstDTO = convertToWaardelijstDTO(waardelijsts.get(i));
            log.debug("WaardelijstLoader persistWaardelijsten: end   found conceptschema: {}", waardelijstDTO == null ? "(null)" : waardelijstDTO.getNaam());
        }
    }

    @Transactional
    public WaardelijstDTO convertToWaardelijstDTO(final Waardelijst waardelijst) {
        log.info("WaardelijstLoader convertToWaardelijstDTO:: received uri: {} waardelijst: {}", waardelijst.getUri(), waardelijst.getNaam());
        WaardelijstDTO savedWaardelijst = null;
        WaardelijstDTO waardelijstDTO = new WaardelijstDTO();

        Optional<WaardelijstDTO> optionalWaardelijstDTO = waardelijstRepository.findByUri(waardelijst.getUri());
        if (optionalWaardelijstDTO.isPresent()) {
            log.debug("ConceptSchemaLoader convertToConcepschemaDTO:: found uri: {} - updating", waardelijst.getUri());
            waardelijstDTO = optionalWaardelijstDTO.get();
        }

        try {
            waardelijstDTO.setEigenaar(waardelijst.getEigenaar());
            waardelijstDTO.setMetadata(waardelijst.getMetadata());
            waardelijstDTO.setNaam(waardelijst.getNaam().isPresent() ? waardelijst.getNaam().get() : null);
            waardelijstDTO.setUri(waardelijst.getUri());
            waardelijstDTO.setBeschrijving(waardelijst.getBeschrijving().isPresent() ? waardelijst.getBeschrijving().get() : null);
            waardelijstDTO.setTitel(waardelijst.getTitel());
            waardelijstDTO.setVersie(waardelijst.getVersie());
            waardelijstDTO.setVersienotitie(waardelijst.getVersienotities().isPresent() ? waardelijst.getVersienotities().get() : null);

            log.debug("WaardelijstLoader convertToWaardelijstDTO: before 01 save conceptschemaDTO");
            savedWaardelijst = waardelijstRepository.save(waardelijstDTO);
            log.debug("WaardelijstLoader convertToWaardelijstDTO: after 01 save conceptschemaDTO");

            List<Concept> concepten = null;

            if ((waardelijst.getEmbedded().getWaarden() != null) && waardelijst.getEmbedded().getWaarden().isPresent()) {
                concepten = waardelijst.getEmbedded().getWaarden().get();
                Set<ConceptDTO> types = findConcepten(concepten);

                for (ConceptDTO x : types) {
                    log.trace("WaardelijstLoader convertToWaardelijstDTO findTypes: used id: {} conceptschematype: {}", x.getId(), x.getType());
                    x.getWaardelijsten().add(savedWaardelijst);
                }

                //conceptschemaDTO.setTypes(types);

                savedWaardelijst.getWaarden().addAll(types);

                log.trace("WaardelijstLoader convertToWaardelijstDTO: before 02 save conceptschemaDTO");
                waardelijstRepository.save(savedWaardelijst);
                log.trace("WaardelijstLoader convertToWaardelijstDTO: after 02 save conceptschemaDTO");
            }
        } catch (Exception e) {
            log.error("WaardelijstLoader convertToWaardelijstDTO error at processing: {}", e.getMessage());
        }
        return savedWaardelijst;
    }

    private Set<ConceptDTO> findConcepten(final List<Concept> concepten) {
        log.trace("WaardelijstLoader findConcepten: found concepten");
        List<ConceptDTO> types = new ArrayList<>();

        for (int i = 0; i < concepten.size(); i++) {
            Concept type = concepten.get(i);
            log.trace("WaardelijstLoader findConcepten: checking [{}] concept: {}", i, type.getUri());
            Optional<ConceptDTO> conceptDTOOptional = conceptRepository.findByUri(type.getUri());
            if (conceptDTOOptional.isPresent()) {
                ConceptDTO conceptDTOold = conceptDTOOptional.get();
                log.trace("WaardelijstLoader findConcepten: found conceptschematype - id: {} concept: {}", conceptDTOold.getId(), conceptDTOold.getUri());
                types.add(conceptDTOold);
            } else {
                ConceptDTO newType = new ConceptDTO();
                newType.setType(type.getType());
                newType.setUitleg(type.getUitleg().isPresent() ? type.getUitleg().get() : null);
                newType.setTerm(type.getTerm());
                newType.setUri(type.getUri());
                newType.setEinddatumGeldigheid(type.getEinddatumGeldigheid().isPresent() ? type.getEinddatumGeldigheid().get() : null);
                newType.setDefinitie(type.getDefinitie().isPresent() ? type.getDefinitie().get() : null);
                newType.setNaam(type.getNaam());
                newType.setMetadata(type.getMetadata());
                newType.setEigenaar(type.getEigenaar().isPresent() ? type.getEigenaar().get() : null);
                newType.setBegindatumGeldigheid(type.getBegindatumGeldigheid());

                Optional<ConceptschemaDTO> conceptschemaDTO = conceptschemaRepository.findByUri(type.getConceptschema());
                if (conceptschemaDTO.isPresent()) {
                    newType.setConceptschema(conceptschemaDTO.get());
                }
                log.trace("WaardelijstLoader findConcepten: before save conceptschemaTypeDTO");
                //ConceptschemaTypeDTO savedConceptschemaTypeDTO = conceptschemaTypeRepository.save(newType);
                conceptRepository.save(newType);
                log.trace("WaardelijstLoader findConcepten: after save conceptschemaTypeDTO - id: {}, concept: {}", newType.getId(), newType.getUri());
                types.add(newType);
            }
        }

        Set<ConceptDTO> typesSet = new HashSet<>();
        for (ConceptDTO x : types) {
            log.info("WaardelijstLoader findConcepten: used id: {} concept: {}", x.getId(), x.getUri());
            typesSet.add(x);
        }

        return typesSet;
    }

}
