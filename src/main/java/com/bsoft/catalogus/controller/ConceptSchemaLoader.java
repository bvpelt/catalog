package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptschemaRepository;
import com.bsoft.catalogus.repository.ConceptschemaTypeRepository;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.SetUtils;
import com.bsoft.catalogus.util.StringChanged;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        OperationResult<InlineResponse200> result;
        boolean nextPage = false;

        Instant start = Instant.now();
        result = catalogService.getConceptschemas(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
        Instant finish = Instant.now();
        long time = Duration.between(start, finish).toMillis();
        log.debug("Timing data getconceptschemas: {} ms ", time);

        if (result.isSuccess()) {
            InlineResponse200 inlineResponse200 = result.getSuccessResult();
            InlineResponse200Embedded embedded = inlineResponse200.getEmbedded();
            List<Conceptschema> conceptschemas;

            if (embedded != null) {
                conceptschemas = embedded.getConceptschemas();


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
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMessage(result.getFailureResult().toString());
            log.info("ConceptSchemaLoader getPage : no result stop processing");
        }
        procesResult.setMore(nextPage);
    }


    public void persistConceptSchemas(final List<Conceptschema> conceptschemas, final ProcesResult procesResult) {
        int maxsize = conceptschemas.size();
        log.info("persistConceptSchemas number found: {}", maxsize);

        for (Conceptschema conceptschema : conceptschemas) {
            log.debug("ConceptSchemaLoader persistConceptSchemas: begin found conceptschema: {}", conceptschema.getNaam());
            ConceptschemaDTO conceptschemaDTO = convertToConcepschemaDTO(conceptschema, procesResult);
            log.debug("ConceptSchemaLoader persistConceptSchemas: end   found conceptschema: {}", conceptschemaDTO == null ? "(null)" : conceptschemaDTO.getNaam());
        }
    }

    @Transactional
    public ConceptschemaDTO convertToConcepschemaDTO(final Conceptschema conceptschema, final ProcesResult procesResult) {
        log.info("ConceptSchemaLoader convertToConcepschemaDTO:: received uri: {} conceptschema: {}", conceptschema.getUri(), conceptschema.getNaam());
        ConceptschemaDTO savedConceptschema = null;
        ConceptschemaDTO conceptschemaDTO;
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
            boolean attributesChanged = true; // always true for new entry
            boolean relationTypesChanged = true;  // always true for new entry

            if (!newEntry) {
                attributesChanged = changedAttributes(conceptschema, conceptschemaDTO);
                relationTypesChanged = changedTypeRelations(conceptschema, conceptschemaDTO);
            }

            log.debug("ConceptSchemaLoader convertToConcepschemaDTO newEntry             : {}", newEntry);
            log.debug("ConceptSchemaLoader convertToConcepschemaDTO attributesChanged    : {}", attributesChanged);
            log.debug("ConceptSchemaLoader convertToConcepschemaDTO relationTypesChanged : {}", relationTypesChanged);

            if (attributesChanged || newEntry) {
                log.info("ConceptSchemaLoader convertToConcepschemaDTO new or changed attributes");
                //
                // for new all attributes and for existing some attributes
                //
                conceptschemaDTO.setUri(conceptschema.getUri());
                conceptschemaDTO.setNaam(conceptschema.getNaam());
                conceptschemaDTO.setUitleg(conceptschema.getUitleg().get());
                conceptschemaDTO.setEigenaar(conceptschema.getEigenaar());
                conceptschemaDTO.setBegindatumGeldigheid(conceptschema.getBegindatumGeldigheid());
                conceptschemaDTO.setEinddatumGeldigheid(conceptschema.getEinddatumGeldigheid().get());
                conceptschemaDTO.setMetadata(conceptschema.getMetadata());
                savedConceptschema = conceptschemaRepository.save(conceptschemaDTO);
            } else {
                log.debug("ConceptSchemaLoader convertToConcepschemaDTO existing, not new or changed attributes");
                savedConceptschema = conceptschemaDTO;
            }

            if (relationTypesChanged) {
                log.debug("ConceptSchemaLoader convertToConcepschemaDTO relations changed, remove all relations");
                savedConceptschema.getTypes().removeAll(savedConceptschema.getTypes());
            }

            if (newEntry || relationTypesChanged) {
                log.debug("ConceptSchemaLoader convertToConcepschemaDTO new or relations changed, add all relations");
                Set<ConceptschemaTypeDTO> types = new HashSet<>();
                Set<ConceptschemaDTO> conceptschemas = new HashSet<>();
                conceptschemas.add(savedConceptschema);

                for (int i = 0; i < conceptschema.getType().size(); i++) {
                    String type = conceptschema.getType().get(i);
                    ConceptschemaTypeDTO savedType;

                    log.trace("ConceptSchemaLoader findTypes: checking [{}] conceptschematype: {}", i, type);
                    Optional<ConceptschemaTypeDTO> conceptschemaTypeDTOOptional = conceptschemaTypeRepository.findByType(type);

                    if (conceptschemaTypeDTOOptional.isPresent()) {  // existing conceptschema type
                        ConceptschemaTypeDTO conceptschemaTypeDTOold = conceptschemaTypeDTOOptional.get();
                        log.trace("ConceptSchemaLoader findTypes: found conceptschematype - id: {} type: {}", conceptschemaTypeDTOold.getId(), conceptschemaTypeDTOold.getType());
                        conceptschemaTypeDTOold.setConceptschema(conceptschemas);
                        savedType = conceptschemaTypeRepository.save(conceptschemaTypeDTOold);
                        types.add(savedType);
                    } else {                                          // new conceptschema
                        ConceptschemaTypeDTO newType = new ConceptschemaTypeDTO();
                        newType.setType(type);
                        newType.setConceptschema(conceptschemas);
                        log.trace("ConceptSchemaLoader findTypes: before save conceptschemaTypeDTO");
                        savedType = conceptschemaTypeRepository.save(newType);
                        log.trace("ConceptSchemaLoader findTypes: after save conceptschemaTypeDTO - id: {}, type: {}", newType.getId(), newType.getType());
                        types.add(savedType);
                    }
                }
                savedConceptschema.getTypes().addAll(types);
                conceptschemaRepository.save(savedConceptschema);
            }

            if (newEntry) { // new entry
                procesResult.setNewEntries(procesResult.getNewEntries() + 1);
            } else if (attributesChanged || relationTypesChanged) { // changed
                procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }
        } catch (Exception e) {
            log.error("ConceptSchemaLoader convertToConcepschemaDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedConceptschema;
    }

    private boolean changedAttributes(final Conceptschema conceptschema, final ConceptschemaDTO conceptschemaDTO) {
        boolean changed = false;


        changed = StringChanged.stringChanged(conceptschema.getUri(), conceptschemaDTO.getUri());


        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getNaam(), conceptschemaDTO.getNaam());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getUitleg(), conceptschemaDTO.getUitleg());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getEigenaar(), conceptschemaDTO.getEigenaar());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getBegindatumGeldigheid(), conceptschemaDTO.getBegindatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getEinddatumGeldigheid(), conceptschemaDTO.getEinddatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(conceptschema.getMetadata(), conceptschemaDTO.getMetadata());
        }

        return changed;
    }

    /**
     * Determine if any releation from conceptschema to type
     *
     * @param conceptschema    The input conceptschema with related types
     * @param conceptschemaDTO The saved conceptschema with related types
     * @return false if type sets are not equal
     */
    private boolean changedTypeRelations(final Conceptschema conceptschema, final ConceptschemaDTO conceptschemaDTO) {
        boolean changed = false;

        Set<String> typeSet = new HashSet<>(conceptschema.getType());

        changed = SetUtils.equals(typeSet, conceptschemaDTO.getTypes());

        return changed;
    }

}
