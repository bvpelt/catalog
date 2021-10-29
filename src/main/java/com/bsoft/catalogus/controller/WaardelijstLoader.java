package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.WaardelijstRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
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

        String uri = null;              //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";/;
        String zoekTerm = null;
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = Arrays.asList("waarden");
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
                log.info("WaardelijstLoader loadWaardelijsten page: {}", page);
                getPage(catalogService, procesResult, uri, gepubliceerdDoor, zoekTerm, page, pageSize, expandScope);

                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("ConceptSchemaLoader loadConceptSchemas error: {}", ex);
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
                         final String zoekTerm,
                         final Integer page,
                         final Integer pageSize,
                         final List<String> expandScope) {

        OperationResult<InlineResponse2004> result = null;
        boolean nextPage = false;

        result = catalogService.getWaardelijst(uri, gepubliceerdDoor, zoekTerm, expandScope, page, pageSize);

        if (result.isSuccess()) {
            InlineResponse2004 inlineResponse2004 = result.getSuccessResult();
            InlineResponse2004Embedded embedded = inlineResponse2004.getEmbedded();
            List<Waardelijst> waardelijsts = embedded.getWaardelijsten();

            persistWaardelijsten(waardelijsts, procesResult);

            if (procesResult.getStatus() == ProcesResult.SUCCESS) {
                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (!((result.getSuccessResult().getLinks().getNext().getHref().isPresent() && (result.getSuccessResult().getLinks().getNext().getHref().get() == null)) || (!result.getSuccessResult().getLinks().getNext().getHref().isPresent()))) {
                        nextPage = true;
                        log.info("WaardelijstLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(page);
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(result.getFailureResult().toString());
            log.info("WaardelijstLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    @Transactional
    public void persistWaardelijsten(final List<Waardelijst> waardelijsts, ProcesResult procesResult) {
        int maxsize = waardelijsts.size();
        log.info("WaardelijstLoader persistWaardelijsten number found: {}", maxsize);

        for (int i = 0; i < maxsize; i++) {
            log.debug("WaardelijstLoader persistWaardelijsten: begin found conceptschema: {}", waardelijsts.get(i).getNaam().isPresent() ? waardelijsts.get(i).getNaam().get() : "");
            WaardelijstDTO waardelijstDTO = convertToWaardelijstDTO(waardelijsts.get(i), procesResult);
            log.debug("WaardelijstLoader persistWaardelijsten: end   found conceptschema: {}", waardelijstDTO == null ? "(null)" : waardelijstDTO.getNaam());
        }
    }


    public WaardelijstDTO convertToWaardelijstDTO(final Waardelijst waardelijst, ProcesResult procesResult) {
        log.info("WaardelijstLoader convertToWaardelijstDTO:: received uri: {} ", waardelijst.getUri());
        WaardelijstDTO savedWaardelijst = null;
        WaardelijstDTO waardelijstDTO = null;
        boolean newEntry = false;


        Optional<WaardelijstDTO> optionalWaardelijstDTO = waardelijstRepository.findByUri(waardelijst.getUri());
        if (optionalWaardelijstDTO.isPresent()) {
            log.debug("WaardelijstLoader convertToWaardelijstDTO:: found uri: {} - updating", waardelijst.getUri());
            waardelijstDTO = optionalWaardelijstDTO.get();
        } else {
            log.debug("WaardelijstLoader convertToWaardelijstDTO: found uri: {} - new", waardelijst.getUri());
            waardelijstDTO = new WaardelijstDTO();
            newEntry = true;
        }
        /*


        List<WaardelijstDTO> waardelijstDTOS = waardelijstRepository.findWaardelijstUrl(waardelijst.getUri());
        if (waardelijstDTOS.size() == 1) {
            log.debug("WaardelijstLoader convertToWaardelijstDTO found uri (updating): {}", waardelijst.getUri());
            waardelijstDTO = waardelijstDTOS.get(0);
        } else if (waardelijstDTOS.size() == 0) {
            log.debug("WaardelijstLoader convertToWaardelijstDTO found uri (new): {} - new", waardelijst.getUri());
            waardelijstDTO = new WaardelijstDTO();
            newEntry = true;
        } else {
            log.error("Unexpected number of entries: {} 0 or 1 expected", waardelijstDTOS.size());
        }
*/
        try {
            waardelijstDTO.setEigenaar(waardelijst.getEigenaar());
            waardelijstDTO.setMetadata(waardelijst.getMetadata());
            waardelijstDTO.setNaam(waardelijst.getNaam().isPresent() ? waardelijst.getNaam().get() : null);
            waardelijstDTO.setUri(waardelijst.getUri());
            waardelijstDTO.setBeschrijving(waardelijst.getBeschrijving().isPresent() ? waardelijst.getBeschrijving().get() : null);
            waardelijstDTO.setTitel(waardelijst.getTitel());
            waardelijstDTO.setVersie(waardelijst.getVersie());
            waardelijstDTO.setVersienotitie(waardelijst.getVersienotities().isPresent() ? waardelijst.getVersienotities().get() : null);

            boolean waardenChanged = false;
            if (!newEntry) {
                waardenChanged = changedWaarden(waardelijstDTO, waardelijst);
            }

            // if (newEntry || waardenChanged || (optionalWaardelijstDTO.isPresent() && !optionalWaardelijstDTO.get().equals(waardelijstDTO))) {
            if (newEntry || waardenChanged || (waardelijstDTO.getId() > 0)) {
                log.debug("WaardelijstLoader convertToWaardelijstDTO: before 01 save conceptschemaDTO");
                savedWaardelijst = waardelijstRepository.save(waardelijstDTO);
                log.debug("WaardelijstLoader convertToWaardelijstDTO: after 01 save conceptschemaDTO");

                List<Concept> concepten = null;

                if (waardelijst.getEmbedded() != null) {
                    if ((waardelijst.getEmbedded().getWaarden() != null) && waardelijst.getEmbedded().getWaarden().isPresent()) {
                        concepten = waardelijst.getEmbedded().getWaarden().get();
                        Set<ConceptDTO> types = findConcepten(concepten);

                        /*
                        for (ConceptDTO x : types) {
                            log.trace("WaardelijstLoader convertToWaardelijstDTO findTypes: used id: {} conceptschematype before change: {}", x.getId(), x.getType());
                            x.getWaardelijsten().add(savedWaardelijst);
                            conceptRepository.save(x);
                            log.trace("WaardelijstLoader convertToWaardelijstDTO findTypes: used id: {} conceptschematype after change: {}", x.getId(), x.getType());
                        }
*/
                        savedWaardelijst.getWaarden().addAll(types);

                        log.trace("WaardelijstLoader convertToWaardelijstDTO: before 02 save conceptschemaDTO");
                        waardelijstRepository.save(savedWaardelijst);
                        log.trace("WaardelijstLoader convertToWaardelijstDTO: after 02 save conceptschemaDTO");
                    }
                }
                if (newEntry) { // new entry
                    procesResult.setNewEntries(procesResult.getNewEntries() + 1);
                } else { // changed
                    procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
                }
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }
        } catch (Exception e) {
            log.error("WaardelijstLoader convertToWaardelijstDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedWaardelijst;
    }

    private boolean inWaarden(final String uri, final List<Concept> concepten) {
        boolean inList = false;

        for (Concept concept : concepten) {
            inList = inList || uri.equals(concept.getUri());
        }
        return inList;
    }

    private boolean changedWaarden(WaardelijstDTO waardelijstDTO, Waardelijst waardelijst) {
        boolean changed = true;

        if (waardelijstDTO != null) {
            Set<ConceptDTO> waardelijstDTOS = waardelijstDTO.getWaarden();
            List<Concept> concepten = new ArrayList<Concept>();

            if (waardelijst.getEmbedded() != null) {
                if (waardelijst.getEmbedded().getWaarden().isPresent()) {
                    concepten = waardelijst.getEmbedded().getWaarden().get();
                }
            }

            if (waardelijstDTOS.size() == concepten.size()) {
                Iterator<ConceptDTO> conceptDTOIterator = waardelijstDTOS.iterator();

                changed = true;

                while (conceptDTOIterator.hasNext()) {
                    ConceptDTO conceptDTO = conceptDTOIterator.next();
                    String uri = conceptDTO.getUri();
                    boolean uriin = inWaarden(uri, concepten);
                    changed = changed || uriin;
                }
            } else {
                changed = true;
            }
        } else {
            if (waardelijst.getEmbedded() == null) {
                changed = false;
            } else {
                if (waardelijst.getEmbedded().getWaarden().isPresent() && waardelijst.getEmbedded().getWaarden().get().size() == 0) {
                    changed = false;
                }
            }
        }
        return changed;
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

                /*
                Optional<ConceptschemaDTO> conceptschemaDTO = conceptschemaRepository.findByUri(type.getConceptschema());
                if (conceptschemaDTO.isPresent()) {
                    newType.setConceptschema(conceptschemaDTO.get());
                }

                 */
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
