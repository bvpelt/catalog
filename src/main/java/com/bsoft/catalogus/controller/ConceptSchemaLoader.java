package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@NoArgsConstructor
public class ConceptSchemaLoader {

    private ConceptschemaRepository conceptschemaRepository;

    private ConceptschemaTypeRepository conceptschemaTypeRepository;

    public ConceptSchemaLoader(ConceptschemaRepository conceptschemaRepository,
                               ConceptschemaTypeRepository conceptschemaTypeRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptschemaTypeRepository = conceptschemaTypeRepository;
    }

    public OperationResult loadConceptSchemas(final CatalogService catalogService) {

        String uri = null; //"http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null; //"https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = "2021-04-14";
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = null; // Arrays.asList("concepten");
        boolean goOn = true;

        ProcesResult procesResult = new ProcesResult();
        procesResult.setMore(goOn);
        procesResult.setEntries(0);
        procesResult.setPages(0);

        while (goOn) {
            log.info("page: {}", page);
            procesResult = getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

            goOn = procesResult.isMore();
            if (goOn) {
                page++;
            }
        }

        return OperationResult.success(procesResult);
    }

    private ProcesResult getPage(final CatalogService catalogService,
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
            persistConceptSchemas(conceptschemas);

            if (result.getSuccessResult().getLinks().getNext() != null) {
                if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                    nextPage = true;
                    log.info("page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                }
            }
            procesResult.setPages(page);
            procesResult.setEntries((page - 1) * pageSize + result.getSuccessResult().getEmbedded().getConceptschemas().size());
        } else {
            log.info("loadConceptSchemas: no result stop processing");
        }
        procesResult.setMore(nextPage);
        return procesResult;
    }

    private void persistConceptSchemas(final List<Conceptschema> conceptschemas) {
        log.info("persistConceptSchemas number found: {}", conceptschemas.size());

        for (int i = 0; i < conceptschemas.size(); i++) {
            log.info("persistConceptSchemas: begin found conceptschema: {}", conceptschemas.get(i).getNaam());
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschemas.get(i));
            log.info("persistConceptSchemas: end   found conceptschema: {}", conceptschemaDTO == null ? "(null)" : conceptschemaDTO.getNaam());
        }
    }

    //@Transactional
    private ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema) {
        log.info("convertToConcepschemaDTO:: received uri: {} conceptschema: {}", conceptschema.getUri(), conceptschema.getNaam());
        ConceptschemaDTO savedConceptschema = null;
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        Optional<ConceptschemaDTO> optionalConceptschemaDTO = conceptschemaRepository.findByUri(conceptschema.getUri());
        if (optionalConceptschemaDTO.isPresent()) {
            log.info("convertToConcepschemaDTO:: found uri: {} - updating", conceptschema.getUri());
            conceptschemaDTO = optionalConceptschemaDTO.get();
        }

        try {
            conceptschemaDTO.setUri(conceptschema.getUri());
            conceptschemaDTO.setNaam(conceptschema.getNaam());
            conceptschemaDTO.setUitleg(conceptschema.getUitleg().get());
            conceptschemaDTO.setEigenaar(conceptschema.getEigenaar());
            conceptschemaDTO.setBegindatumGeldigheid(conceptschema.getBegindatumGeldigheid());
            conceptschemaDTO.setEinddatumGeldigheid(conceptschema.getEinddatumGeldigheid().get());
            conceptschemaDTO.setMetadata(conceptschema.getMetadata());

            log.info("convertToConcepschemaDTO: before 01 save conceptschemaDTO");
            savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
            log.info("convertToConcepschemaDTO: after 01 save conceptschemaDTO");

            List<String> conceptschemaType = conceptschema.getType();
            Set<ConceptschemaTypeDTO> types = findTypes(conceptschemaType);

            for (ConceptschemaTypeDTO x : types) {
                log.info("findTypes: used id: {} conceptschematype: {}", x.getId(), x.getType());
                x.getConceptschemas().add(savedConceptschema);
            }

            conceptschemaDTO.setTypes(types);

            savedConceptschema.getTypes().addAll(types);

            log.info("convertToConcepschemaDTO: before 02 save conceptschemaDTO");
            conceptschemaRepository.save(savedConceptschema);
            log.info("convertToConcepschemaDTO: after 02 save conceptschemaDTO");
        } catch (Exception e) {
            log.error("Error at processing: {}", e.getMessage());
        }
        return savedConceptschema;
    }

    //@Transactional(propagation = Propagation.NESTED)
    private Set<ConceptschemaTypeDTO> findTypes(final List<String> conceptschemaType) {
        log.info("findTypes: found conceptschema: {}", String.join(", ", conceptschemaType));
        List<ConceptschemaTypeDTO> types = new ArrayList<>();

        for (int i = 0; i < conceptschemaType.size(); i++) {
            String type = conceptschemaType.get(i);
            log.info("findTypes: checking [{}] conceptschematype: {}", i, type);
            Optional<ConceptschemaTypeDTO> conceptschemaTypeDTOOptional = conceptschemaTypeRepository.findByType(type);
            if (conceptschemaTypeDTOOptional.isPresent()) {
                ConceptschemaTypeDTO conceptschemaTypeDTOold = conceptschemaTypeDTOOptional.get();
                log.info("findTypes: found conceptschematype - id: {} type: {}", conceptschemaTypeDTOold.getId(), conceptschemaTypeDTOold.getType());
                types.add(conceptschemaTypeDTOold);
            } else {
                ConceptschemaTypeDTO newType = new ConceptschemaTypeDTO();
                newType.setType(type);
                log.info("findTypes: before save conceptschemaTypeDTO");
                //ConceptschemaTypeDTO savedConceptschemaTypeDTO = conceptschemaTypeRepository.save(newType);
                conceptschemaTypeRepository.save(newType);
                log.info("findTypes: after save conceptschemaTypeDTO - id: {}, type: {}", newType.getId(), newType.getType());
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
