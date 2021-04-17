package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import liquibase.pro.packaged.C;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@NoArgsConstructor
@Slf4j
@RestController
public class ConceptschemaController {

    @Autowired
    CatalogService catalogService;

    @Autowired
    ConceptschemaRepository conceptschemaRepository;

    @Autowired
    ConceptschemaTypeRepository conceptschemaTypeRepository;

    @RequestMapping(value = "/conceptschemas")
    public ResponseEntity<InlineResponse200> getConceptschemas() {
        log.info("getConceptschemas");
        ConceptSchemaLoader conceptSchemaLoader = new ConceptSchemaLoader();
        OperationResult<InlineResponse200> result = conceptSchemaLoader.loadConceptSchemas(catalogService);
        if (result.isSuccess()) {
            ResponseEntity<InlineResponse200> response200ResponseEntity = ResponseEntity.ok(result.getSuccessResult());
            InlineResponse200 inlineResponse200 = result.getSuccessResult();
            InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();
            List<Conceptschema> conceptschemas = embedded.getConceptschemas();
            persistConceptSchemas(conceptschemas);
            return response200ResponseEntity;
        } else {
            String message = "error during proces";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private int persistConceptSchemas(final List<Conceptschema> conceptschemas) {
        log.info("persistConceptSchemas");
        int number = 0;

        for (int i = 0; i < conceptschemas.size(); i++) {
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschemas.get(i));
            log.info("found conceptschema: {}", conceptschemaDTO.getNaam());
            ConceptschemaDTO savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
            number++;
        }
        return number;
    }

    private ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema) {
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        conceptschemaDTO.setUri(conceptschema.getUri());

        List<String> conceptschemaType = conceptschema.getType();
        Set<ConceptschemaTypeDTO> types = findTypes(conceptschemaType);
        conceptschemaDTO.setTypes(types);

        conceptschemaDTO.setNaam(conceptschema.getNaam());
        conceptschemaDTO.setUitleg(conceptschema.getUitleg().get());
        conceptschemaDTO.setEigenaar(conceptschema.getEigenaar());
        conceptschemaDTO.setBegindatumGeldigheid(conceptschema.getBegindatumGeldigheid());
        conceptschemaDTO.setEinddatumGeldigheid(conceptschema.getEinddatumGeldigheid().get());
        conceptschemaDTO.setMetadata(conceptschema.getMetadata());

        return conceptschemaDTO;
    }

    private Set<ConceptschemaTypeDTO> findTypes(final List<String> conceptschemaType) {
        List<ConceptschemaTypeDTO> types = new ArrayList<>();

        for ( int i = 0; i < conceptschemaType.size(); i++) {
            Optional<ConceptschemaTypeDTO> conceptschemaTypeDTOOptional = conceptschemaTypeRepository.findByType(conceptschemaType.get(i));
            if (conceptschemaTypeDTOOptional.isPresent()) {
                types.add(conceptschemaTypeDTOOptional.get());
            } else {
                ConceptschemaTypeDTO newType = new ConceptschemaTypeDTO();
                newType.setType(conceptschemaType.get(i));
                ConceptschemaTypeDTO savedConceptschemaTypeDTO = conceptschemaTypeRepository.save(newType);
                types.add(savedConceptschemaTypeDTO);
            }
        }

        Set<ConceptschemaTypeDTO> typesSet = new HashSet<ConceptschemaTypeDTO>();
        for (ConceptschemaTypeDTO x : types) {
            typesSet.add(x);
        }

        return typesSet;
    }
}
