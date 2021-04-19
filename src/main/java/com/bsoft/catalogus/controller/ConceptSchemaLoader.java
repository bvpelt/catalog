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

import javax.transaction.Transactional;
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
        List<String> expandScope = Arrays.asList("collecties");
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
                goOn = false;
            }

            if (page > 4) {
                goOn = false;
            }
        }

        return OperationResult.success(procesResult);
    }


    @Transactional
    public int persistConceptSchemas(final List<Conceptschema> conceptschemas) {
        log.info("persistConceptSchemas");
        int number = 0;

        for (int i = 0; i < conceptschemas.size(); i++) {
            log.info("persistConceptSchemas: found conceptschema: {}", conceptschemas.get(i).getNaam());
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschemas.get(i));
            //ConceptschemaDTO savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
            number++;
        }
        return number;
    }

    private ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema) {
        log.info("convertToConcepschemaDTO: found conceptschema: {}", conceptschema.getNaam());
        ConceptschemaDTO conceptschemaDTO = new ConceptschemaDTO();

        conceptschemaDTO.setUri(conceptschema.getUri());
        conceptschemaDTO.setNaam(conceptschema.getNaam());
        conceptschemaDTO.setUitleg(conceptschema.getUitleg().get());
        conceptschemaDTO.setEigenaar(conceptschema.getEigenaar());
        conceptschemaDTO.setBegindatumGeldigheid(conceptschema.getBegindatumGeldigheid());
        conceptschemaDTO.setEinddatumGeldigheid(conceptschema.getEinddatumGeldigheid().get());
        conceptschemaDTO.setMetadata(conceptschema.getMetadata());

        ConceptschemaDTO savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);

        List<String> conceptschemaType = conceptschema.getType();
        Set<ConceptschemaTypeDTO> types = findTypes(conceptschemaType);
        conceptschemaDTO.setTypes(types);

        savedConceptschema.getTypes().addAll(types);

        conceptschemaRepository.save(savedConceptschema);

        return savedConceptschema;
    }

    private Set<ConceptschemaTypeDTO> findTypes(final List<String> conceptschemaType) {
        log.info("findTypes: found conceptschema: {}", String.join(", ", conceptschemaType));
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
