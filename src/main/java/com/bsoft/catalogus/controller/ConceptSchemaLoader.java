package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@NoArgsConstructor
public class ConceptSchemaLoader {


    ConceptschemaRepository conceptschemaRepository;


    ConceptschemaTypeRepository conceptschemaTypeRepository;

    public ConceptSchemaLoader( ConceptschemaRepository conceptschemaRepository,
                    ConceptschemaTypeRepository conceptschemaTypeRepository) {
        this.conceptschemaRepository = conceptschemaRepository;
        this.conceptschemaTypeRepository = conceptschemaTypeRepository;
    }

    public  OperationResult loadConceptSchemas(CatalogService catalogService) {

        String uri = "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = "2021-04-14";
        Integer page = 1;
        Integer pageSize = 10;
        List<String> expandScope = Arrays.asList("concepten");
        boolean goOn = true;

        OperationResult<InlineResponse200> result = null;

        ProcesResult procesResult = new ProcesResult();

        while (goOn) {
            log.info("page: {}", page);
            result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);

            if (result.isSuccess()) {
                ResponseEntity<InlineResponse200> response200ResponseEntity = ResponseEntity.ok(result.getSuccessResult());
                InlineResponse200 inlineResponse200 = result.getSuccessResult();
                InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();
                List<Conceptschema> conceptschemas = embedded.getConceptschemas();
                persistConceptSchemas(conceptschemas);
                procesResult.setPages(page);
                procesResult.setEntries((page - 1) * pageSize + result.getSuccessResult().getEmbedded().getConceptschemas().size());

                if (result.getSuccessResult().getLinks().getNext() != null) {
                    if (result.getSuccessResult().getLinks().getNext().getHref() != null) {
                        page++;
                        log.info("page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    } else {
                        goOn = false;
                    }
                } else {
                    goOn = false;
                }
            } else {
                log.info("loadConceptSchemas: no result stop processing" );
                goOn = false;
            }

            if (page > 4) {
                goOn = false;
            }
        }

        return OperationResult.success(procesResult);
    }

    private int persistConceptSchemas(final List<Conceptschema> conceptschemas) {
        log.info("persistConceptSchemas");
        int number = 0;

        for (int i = 0; i < conceptschemas.size(); i++) {
            log.info("persistConceptSchemas: begin found conceptschema: {}", conceptschemas.get(i).getNaam());
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschemas.get(i));
            //ConceptschemaDTO savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
            number++;
            log.info("persistConceptSchemas: end   found conceptschema: {}", conceptschemas.get(i).getNaam());
        }
        return number;
    }

    //@Transactional
    public ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema) {
        log.info("convertToConcepschemaDTO: found conceptschema: {}", conceptschema.getNaam());
        ConceptschemaDTO savedConceptschema = null;
        try {
            ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

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
                log.info("findTypes: used conceptschematype: {}", x.getId(), x.getType());
                x.getConceptschemas().add(savedConceptschema);
            }

            conceptschemaDTO.setTypes(types);

            savedConceptschema.getTypes().addAll(types);

            log.info("convertToConcepschemaDTO: before 02 save conceptschemaDTO");
            conceptschemaRepository.save(savedConceptschema);
            log.info("convertToConcepschemaDTO: after 02 save conceptschemaDTO");
        } catch (Exception e) {
            log.error("Error at processing: {}", e);
        }
        return savedConceptschema;
    }

    //@Transactional(propagation = Propagation.NESTED)
    public Set<ConceptschemaTypeDTO> findTypes(final List<String> conceptschemaType) {
        log.info("findTypes: found conceptschema: {}", String.join(", ", conceptschemaType));
        List<ConceptschemaTypeDTO> types = new ArrayList<>();

        for ( int i = 0; i < conceptschemaType.size(); i++) {
            String type = conceptschemaType.get(i);
            log.info("findTypes: checking [{}] conceptschematype: {}", i, type);
            Optional<ConceptschemaTypeDTO> conceptschemaTypeDTOOptional = conceptschemaTypeRepository.findByType(type);
            if (conceptschemaTypeDTOOptional.isPresent()) {
                ConceptschemaTypeDTO conceptschemaTypeDTOold = conceptschemaTypeDTOOptional.get();
                log.info("findTypes: found conceptschematype - id: {} type: {}", conceptschemaTypeDTOold.getId(), conceptschemaTypeDTOold.getType() );
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

        Set<ConceptschemaTypeDTO> typesSet = new HashSet<ConceptschemaTypeDTO>();
        for (ConceptschemaTypeDTO x : types) {
            log.info("findTypes: used conceptschematype: {}", x.getId(), x.getType());
            typesSet.add(x);
        }

        return typesSet;
    }

}
