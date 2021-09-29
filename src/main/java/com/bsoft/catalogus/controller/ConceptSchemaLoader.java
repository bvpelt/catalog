package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class ConceptSchemaLoader {

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptschemaTypeRepository conceptschemaTypeRepository;

    public ConceptSchemaLoader(final ConceptschemaRepository conceptschemaRepository,
                               final ConceptschemaTypeRepository conceptschemaTypeRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptschemaTypeRepository = conceptschemaTypeRepository;
    }

    public OperationResult<ProcesResult> loadConceptSchemas(final CatalogService catalogService) {

        String uri = null;              //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = null;         //new CatalogUtil().getCurrentDate();
        String zoekTerm = null;
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = null; // Arrays.asList("concepten");
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
                log.info("ConceptSchemaLoader loadConceptSchemas page: {}", page);

                getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
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
                         final String geldigOp,
                         final String zoekTerm,
                         final Integer page,
                         final Integer pageSize,
                         final List<String> expandScope) {

        OperationResult<InlineResponse200> result = null;
        boolean nextPage = false;

        result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);

        if (result.isSuccess()) {
            InlineResponse200 inlineResponse200 = result.getSuccessResult();
            InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();
            List<Conceptschema> conceptschemas = embedded.getConceptschemas();

            persistConceptSchemas(conceptschemas, procesResult);

            if (procesResult.getStatus() == ProcesResult.SUCCESS) {
                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (!((result.getSuccessResult().getLinks().getNext().getHref().isPresent() && (result.getSuccessResult().getLinks().getNext().getHref().get() == null)) || (!result.getSuccessResult().getLinks().getNext().getHref().isPresent()))) {
                        nextPage = true;
                        log.info("ConceptSchemaLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(procesResult.getPages() + 1);
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(result.getFailureResult().toString());
            log.info("ConceptSchemaLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    private void persistConceptSchemas(final List<Conceptschema> conceptschemas, final ProcesResult procesResult) {
        int maxsize = conceptschemas.size();
        log.info("persistConceptSchemas number found: {}", maxsize);

        for (int i = 0; i < maxsize; i++) {
            log.debug("ConceptSchemaLoader persistConceptSchemas: begin found conceptschema: {}", conceptschemas.get(i).getNaam());
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschemas.get(i), procesResult);
            log.debug("ConceptSchemaLoader persistConceptSchemas: end   found conceptschema: {}", conceptschemaDTO == null ? "(null)" : conceptschemaDTO.getNaam());
        }
    }

    @Transactional
    public ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema, final ProcesResult procesResult) {
        log.info("ConceptSchemaLoader convertToConcepschemaDTO:: received uri: {} conceptschema: {}", conceptschema.getUri(), conceptschema.getNaam());
        ConceptschemaDTO savedConceptschema = null;
        ConceptschemaDTO conceptschemaDTO = null;
        boolean newEntry = false;

        Optional<ConceptschemaDTO> optionalConceptschemaDTO = conceptschemaRepository.findByUri(conceptschema.getUri());
        if (optionalConceptschemaDTO.isPresent()) {
            log.debug("ConceptSchemaLoader convertToConcepschemaDTO:: found uri: {} - updating", conceptschema.getUri());
            conceptschemaDTO = optionalConceptschemaDTO.get();
        } else {
            conceptschemaDTO = new ConceptschemaDTO();
            newEntry = true;
        }

        try {
            conceptschemaDTO.setUri(conceptschema.getUri());
            conceptschemaDTO.setNaam(conceptschema.getNaam());
            conceptschemaDTO.setUitleg(conceptschema.getUitleg().get());
            conceptschemaDTO.setEigenaar(conceptschema.getEigenaar());
            conceptschemaDTO.setBegindatumGeldigheid(conceptschema.getBegindatumGeldigheid());
            conceptschemaDTO.setEinddatumGeldigheid(conceptschema.getEinddatumGeldigheid().get());
            conceptschemaDTO.setMetadata(conceptschema.getMetadata());

            boolean conceptschemaTypeChanged = false;
            conceptschemaTypeChanged = changedConceptSchemaType(optionalConceptschemaDTO.get(), conceptschema);

            if (newEntry || conceptschemaTypeChanged || (optionalConceptschemaDTO.isPresent() && !optionalConceptschemaDTO.get().equals(conceptschemaDTO))) {
                log.debug("ConceptSchemaLoader convertToConcepschemaDTO: before 01 save conceptschemaDTO");
                savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
                log.debug("ConceptSchemaLoader convertToConcepschemaDTO: after 01 save conceptschemaDTO");

                List<String> conceptschemaType = conceptschema.getType();
                Set<ConceptschemaTypeDTO> types = findTypes(conceptschemaType);

                for (ConceptschemaTypeDTO x : types) {
                    log.trace("ConceptSchemaLoader convertToConcepschemaDTO findTypes: used id: {} conceptschematype: {}", x.getId(), x.getType());
                    x.getConceptschemas().add(savedConceptschema);
                }

                savedConceptschema.getTypes().addAll(types);

                log.trace("ConceptSchemaLoader convertToConcepschemaDTO: before 02 save conceptschemaDTO");
                conceptschemaRepository.save(savedConceptschema);
                log.trace("ConceptSchemaLoader convertToConcepschemaDTO: after 02 save conceptschemaDTO");

                if (newEntry) { // new entry
                    procesResult.setNewEntries(procesResult.getNewEntries() + 1);
                } else { // changed
                    procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
                }
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }

        } catch (Exception e) {
            log.error("ConceptSchemaLoader convertToConcepschemaDTO error at processing: {}", e.getMessage());
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedConceptschema;
    }

    private boolean inConceptSchema(final String cttype, final Conceptschema conceptschema) {
        boolean inList = false;
        List<String> cs = conceptschema.getType();

        for (String test : cs) {
            inList = inList || test.equals(cttype);
        }
        return inList;
    }

    private boolean changedConceptSchemaType(final ConceptschemaDTO conceptschemaDTO, final Conceptschema conceptschema) {
        boolean changed = true;

        if (conceptschemaDTO != null) {
            Set<ConceptschemaTypeDTO> conceptschemaTypeDTOS = conceptschemaDTO.getTypes();
            List<String> conceptschemaType = conceptschema.getType();

            if (conceptschemaTypeDTOS.size() == conceptschemaType.size()) {
                Iterator<ConceptschemaTypeDTO> conceptschemaTypeDTOIterator = conceptschemaTypeDTOS.iterator();

                changed = true; // assume lists contain all values
                // check if each element in Set<ConceptschemaTypeDTO> also occurs in List<String>
                while (conceptschemaTypeDTOIterator.hasNext()) {
                    ConceptschemaTypeDTO ct = conceptschemaTypeDTOIterator.next();
                    String cttype = ct.getType();
                    boolean ctin = inConceptSchema(cttype, conceptschema);
                    changed = changed || ctin;
                }
            } else {
                changed = true;
            }
        } else {
            if (conceptschema.getType().size() == 0) {
                changed = false;
            }
        }

        return changed;
    }

    private Set<ConceptschemaTypeDTO> findTypes(final List<String> conceptschemaType) {
        log.trace("ConceptSchemaLoader findTypes: found conceptschema: {}", String.join(", ", conceptschemaType));
        List<ConceptschemaTypeDTO> types = new ArrayList<>();

        for (int i = 0; i < conceptschemaType.size(); i++) {
            String type = conceptschemaType.get(i);
            log.trace("ConceptSchemaLoader findTypes: checking [{}] conceptschematype: {}", i, type);
            Optional<ConceptschemaTypeDTO> conceptschemaTypeDTOOptional = conceptschemaTypeRepository.findByType(type);
            if (conceptschemaTypeDTOOptional.isPresent()) {
                ConceptschemaTypeDTO conceptschemaTypeDTOold = conceptschemaTypeDTOOptional.get();
                log.trace("ConceptSchemaLoader findTypes: found conceptschematype - id: {} type: {}", conceptschemaTypeDTOold.getId(), conceptschemaTypeDTOold.getType());
                types.add(conceptschemaTypeDTOold);
            } else {
                ConceptschemaTypeDTO newType = new ConceptschemaTypeDTO();
                newType.setType(type);
                log.trace("ConceptSchemaLoader findTypes: before save conceptschemaTypeDTO");
                conceptschemaTypeRepository.save(newType);
                log.trace("ConceptSchemaLoader findTypes: after save conceptschemaTypeDTO - id: {}, type: {}", newType.getId(), newType.getType());
                types.add(newType);
            }
        }

        Set<ConceptschemaTypeDTO> typesSet = new HashSet<>();
        for (ConceptschemaTypeDTO x : types) {
            log.info("findTypes: used id: {} conceptschematype: {}", x.getId(), x.getType());
            typesSet.add(x);
        }

        return typesSet;
    }

}
