package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.ConceptRepository;
import com.bsoft.catalogus.repository.ToelichtingRepository;
import com.bsoft.catalogus.repository.TrefwoordRepository;
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
public class ConceptLoader {

    private TrefwoordRepository trefwoordRepository;

    private ToelichtingRepository toelichtingRepository;

    private ConceptRepository conceptRepository;

    public ConceptLoader(final TrefwoordRepository trefwoordRepository,
                         final ToelichtingRepository toelichtingRepository,
                         final ConceptRepository conceptRepository) {
        this.trefwoordRepository = trefwoordRepository;
        this.toelichtingRepository = toelichtingRepository;
        this.conceptRepository = conceptRepository;
    }

    public OperationResult loadConcept(final CatalogService catalogService) {

        String uri = null;               // "http://regelgeving.omgevingswet.overheid.nl/id/conceptscheme/Regelgeving";
        String gepubliceerdDoor = null;  // "https://standaarden.overheid.nl/owms/terms/Ministerie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties";
        String geldigOp = null;          // new CatalogUtil().getCurrentDate();
        String zoekTerm = null;
        Integer page = 1;
        Integer pageSize = 10;           // 10,20,40
        List<String> expandScope = null; // not supported!!! Arrays.asList("concepten", "collecties");
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
                log.info("ConceptLoader loadConcept page: {}", page);
                getPage(catalogService, procesResult, uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
                goOn = procesResult.isMore();
                if (goOn) {
                    page++;
                }
            }
        } catch (Exception ex) {
            log.error("ConceptLoader loadConcept error loading concept: {}", ex);
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

        OperationResult<InlineResponse2002> result = null;
        boolean nextPage = false;

        Instant start = Instant.now();
        result = catalogService.getConcepten(uri, gepubliceerdDoor, geldigOp, zoekTerm, null, null, null, page, pageSize);
        Instant finish = Instant.now();
        long time = Duration.between(start, finish).toMillis();
        log.debug("Timing data getconcepten: {} ms ", time);

        if ((result != null) && result.isSuccess()) {
            InlineResponse2002 inlineResponse2002 = result.getSuccessResult();
            InlineResponse2002Embedded embedded = inlineResponse2002.getEmbedded();
            List<Concept> concepten = embedded.getConcepten();

            if ((concepten != null) && (!concepten.isEmpty())) {
                persistConcepten(concepten, procesResult);
            }

            if (procesResult.getStatus() == ProcesResult.SUCCESS) {
                if (result.getSuccessResult().getLinks().getNext() != null) {

                    if (!((result.getSuccessResult().getLinks().getNext().getHref().isPresent() && (result.getSuccessResult().getLinks().getNext().getHref().get() == null)) || (!result.getSuccessResult().getLinks().getNext().getHref().isPresent()))) {
                        nextPage = true;
                        log.info("ConceptLoader getPage page: {} next: {}", page, result.getSuccessResult().getLinks().getNext().getHref());
                    }
                }
                procesResult.setPages(procesResult.getPages() + 1);
            }
        } else {
            procesResult.setStatus(ProcesResult.ERROR);

            if ((result != null) && result.getFailureResult() != null) {
                log.debug("ConceptLoader getPage, result: " + result.getFailureResult().toString());
                procesResult.setMessage(result.getFailureResult().toString());
            } else {
                procesResult.setMessage("Error during processing request /concepten");
            }
            log.info("ConceptLoader getPage: no result stop processing");
        }
        procesResult.setMore(nextPage);
    }

    private void persistConcepten(final List<Concept> concepten, final ProcesResult procesResult) {
        int maxsize = concepten.size();
        log.info("ConceptLoader persistConcepten number found: {}", maxsize);

        for (int i = 0; i < maxsize; i++) {
            log.debug("ConceptLoader persistConcepten: begin found collectie: {}", concepten.get(i).getNaam());
            ConceptDTO conceptDTO = convertToConceptDTO(concepten.get(i), procesResult);
            log.debug("ConceptLoader persistConcepten: end   found collectie: {}", conceptDTO == null ? "(null)" : conceptDTO.getUri());
        }
    }

    @Transactional
    public ConceptDTO convertToConceptDTO(final Concept concept, final ProcesResult procesResult) {
        log.info("ConceptLoader convertToConceptDTO:: received uri: {} collectie: {}", concept.getUri(), concept.getNaam());
        ConceptDTO savedConcept = null;
        ConceptDTO conceptDTO = null;
        boolean newEntry = false;

        Optional<ConceptDTO> optionalConceptDTO = conceptRepository.findByUri(concept.getUri());
        if (optionalConceptDTO.isPresent()) {
            log.debug("ConceptLoader convertToConceptDTO: found uri: {} - updating", concept.getUri());
            conceptDTO = optionalConceptDTO.get();
        } else {
            log.debug("ConceptLoader convertToConceptDTO: found uri: {} - new", concept.getUri());
            conceptDTO = new ConceptDTO();
            newEntry = true;
        }

        try {
            boolean attributesChanged = true; // always true for new entry
            boolean relationToelichtingenChanged = true; // always true for new entry
            boolean relationTermenChanged = true; // always true for new entry

            if (!newEntry) {
                attributesChanged = changedAttributes(concept, conceptDTO);
                relationToelichtingenChanged = changedToelichtingenRelations(concept, conceptDTO);
                relationTermenChanged = changedTermenRelations(concept, conceptDTO);
            }

            log.debug("ConceptLoader convertToConceptDTO newEntry                     : {}", newEntry);
            log.debug("ConceptLoader convertToConceptDTO attributesChanged            : {}", attributesChanged);
            log.debug("ConceptLoader convertToConceptDTO relationToelichtingenChanged : {}", relationToelichtingenChanged);
            log.debug("ConceptLoader convertToConceptDTO relationTermenChanged        : {}", relationTermenChanged);

            if (attributesChanged || newEntry) {
                log.info("ConceptLoader convertToConceptDTO new or changed attributes");
                //
                // for new all attributes and for existing some attributes
                //
                conceptDTO.setUri(concept.getUri());
                conceptDTO.setType(concept.getType());
                conceptDTO.setNaam(concept.getNaam());
                conceptDTO.setTerm(concept.getTerm());
                conceptDTO.setUitleg(concept.getUitleg().isPresent() ? concept.getUitleg().get() : null);
                conceptDTO.setDefinitie(concept.getDefinitie().isPresent() ? concept.getDefinitie().get() : null);
                conceptDTO.setEigenaar(concept.getEigenaar().isPresent() ? concept.getEigenaar().get() : null);
                conceptDTO.setConceptschema(concept.getConceptschema());
                conceptDTO.setBegindatumGeldigheid(concept.getBegindatumGeldigheid());
                conceptDTO.setEinddatumGeldigheid(concept.getEinddatumGeldigheid().isPresent() ? concept.getEinddatumGeldigheid().get() : null);
                conceptDTO.setMetadata(concept.getMetadata());
                savedConcept = conceptRepository.save(conceptDTO);
            } else {
                log.debug("ConceptLoader convertToConceptDTO existing, not new or changed attributes");
                savedConcept = conceptDTO;
            }

            if (relationTermenChanged) {
                log.debug("ConceptLoader convertToConceptDTO relation termen changed, remove all relations");
                savedConcept.getTrefwoorden().removeAll(savedConcept.getTrefwoorden());

            }

            if (relationToelichtingenChanged) {
                log.debug("ConceptLoader convertToConceptDTO relation toelichtingen changed, remove all relations");
                savedConcept.getToelichtingen().removeAll(savedConcept.getToelichtingen());
            }

            if (newEntry || relationTermenChanged || relationToelichtingenChanged) {
                log.debug("ConceptLoader convertToConcepschemaDTO new or relations changed, add all relations");

                Set<ConceptDTO> concepten = new HashSet<>();
                concepten.add(savedConcept);

                // Trefwoorden
                if (concept.getTrefwoorden().isPresent() && concept.getTrefwoorden().get() != null) {
                    addTrefwoorden(concept.getTrefwoorden().get(), savedConcept,  concepten);
                }

                // Toelichtingen
                if (concept.getToelichtingen().isPresent() && concept.getToelichtingen().get() != null) {
                    addToelichtingen(concept.getToelichtingen().get(), savedConcept, concepten);
                }
            }

            if (newEntry) { // new entry
                procesResult.setNewEntries(procesResult.getNewEntries() + 1);
            } else if (attributesChanged || relationTermenChanged) { // changed
                procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
            } else { // unchanged
                procesResult.setUnchangedEntries(procesResult.getUnchangedEntries() + 1);
            }
        } catch (Exception e) {
            log.error("ConceptLoader convertToConceptDTO error at processing: {}", e);
            procesResult.setStatus(ProcesResult.ERROR);
            procesResult.setMore(false);
            procesResult.setMessage(e.getMessage());
        }
        return savedConcept;
    }

    private void addToelichtingen(final List<String> toelichtingenList, ConceptDTO savedConcept,  final Set<ConceptDTO> concepten) {
        Set<ToelichtingDTO> toelichtingen = new HashSet<>();

        for (int i = 0; i < toelichtingenList.size(); i++) {
            String toelichting = toelichtingenList.get(i);
            ToelichtingDTO savedToelichting;

            log.trace("ConceptLoader addToelichtingen: checking [{}] toelichting: {}", i, toelichting);
            Optional<ToelichtingDTO> toelichtingDTOOptional = toelichtingRepository.findByToelichting(toelichting);

            if (toelichtingDTOOptional.isPresent()) {  // existing conceptschema trefwoord
                ToelichtingDTO toelichtingDTOold = toelichtingDTOOptional.get();
                log.trace("ConceptLoader addToelichtingen: found conceptschematype - id: {} toelichting: {}", toelichtingDTOold.getId(), toelichtingDTOold.getToelichting());
                toelichtingDTOold.setConcept(concepten);
                savedToelichting = toelichtingRepository.save(toelichtingDTOold);
            } else {                                          // new conceptschema
                ToelichtingDTO newToelichting = new ToelichtingDTO();
                newToelichting.setToelichting(toelichting);
                newToelichting.setConcept(concepten);
                log.trace("ConceptLoader addToelichtingen: before save conceptschemaTypeDTO");
                savedToelichting = toelichtingRepository.save(newToelichting);
                log.trace("ConceptLoader addToelichtingen: after save conceptschemaTypeDTO - id: {}, toelichting: {}", newToelichting.getId(), newToelichting.getToelichting());
            }
            toelichtingen.add(savedToelichting);
        }
        savedConcept.getToelichtingen().addAll(toelichtingen);
        conceptRepository.save(savedConcept);
    }

    private void addTrefwoorden(final List<String> trefwoordenLijst, ConceptDTO savedConcept,  final Set<ConceptDTO> concepten) {
        Set<TrefwoordDTO> trefwoorden = new HashSet<>();

        for (int i = 0; i < trefwoordenLijst.size(); i++) {
            String trefwoord = trefwoordenLijst.get(i);
            TrefwoordDTO savedTrefwoord;

            log.trace("ConceptLoader addTrefwoorden: checking [{}] trefwoord: {}", i, trefwoord);
            Optional<TrefwoordDTO> trefwoordDTOOptional = trefwoordRepository.findByTrefwoord(trefwoord);

            if (trefwoordDTOOptional.isPresent()) {  // existing conceptschema trefwoord
                TrefwoordDTO trefwoordDTOold = trefwoordDTOOptional.get();
                log.trace("ConceptLoader addTrefwoorden: found conceptschematype - id: {} trefwoord: {}", trefwoordDTOold.getId(), trefwoordDTOold.getTrefwoord());
                trefwoordDTOold.setConcept(concepten);
                savedTrefwoord = trefwoordRepository.save(trefwoordDTOold);
            } else {                                          // new conceptschema
                TrefwoordDTO newTrefwoord = new TrefwoordDTO();
                newTrefwoord.setTrefwoord(trefwoord);
                newTrefwoord.setConcept(concepten);
                log.trace("ConceptLoader addTrefwoorden: before save conceptschemaTypeDTO");
                savedTrefwoord = trefwoordRepository.save(newTrefwoord);
                log.trace("ConceptLoader addTrefwoorden: after save conceptschemaTypeDTO - id: {}, type: {}", newTrefwoord.getId(), newTrefwoord.getTrefwoord());
            }
            trefwoorden.add(savedTrefwoord);
        }
        savedConcept.getTrefwoorden().addAll(trefwoorden);
        conceptRepository.save(savedConcept);
    }

    private boolean changedAttributes(final Concept concept, final ConceptDTO conceptDTO) {
        boolean changed = false;
        changed = StringChanged.stringChanged(concept.getUri(), conceptDTO.getUri());

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getType(), conceptDTO.getType());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getNaam(), conceptDTO.getNaam());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getTerm(), conceptDTO.getTerm());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getUitleg(), conceptDTO.getUitleg());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getDefinitie(), conceptDTO.getDefinitie());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getEigenaar(), conceptDTO.getEigenaar());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getConceptschema(), conceptDTO.getConceptschema());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getBegindatumGeldigheid(), conceptDTO.getBegindatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getEinddatumGeldigheid(), conceptDTO.getEinddatumGeldigheid());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(concept.getMetadata(), conceptDTO.getMetadata());
        }

        return changed;
    }

    private boolean changedTermenRelations(final Concept concept, final ConceptDTO conceptDTO) {
        boolean changed = false;

        List<String> trefwoorden = null;
        Set<String> trefwoordenSet = new HashSet<String>();

        if (concept.getTrefwoorden().isPresent() && (concept.getTrefwoorden().get() != null)) {
            trefwoorden = concept.getTrefwoorden().get();

            for (String x : trefwoorden) {
                trefwoordenSet.add(x);
            }
        }

        changed = !SetUtils.equals(trefwoordenSet, conceptDTO.getTrefwoorden());

        return changed;
    }

    private boolean changedToelichtingenRelations(final Concept concept, final ConceptDTO conceptDTO) {
        boolean changed = false;

        List<String> toelichtingen = null;
        Set<String> toelichtingenSet = new HashSet<String>();

        if (concept.getToelichtingen().isPresent() && (concept.getToelichtingen().get() != null)) {
            toelichtingen = concept.getToelichtingen().get();

            for (String x : toelichtingen) {
                toelichtingenSet.add(x);
            }
        }

        changed = !SetUtils.equals(toelichtingenSet, conceptDTO.getTrefwoorden());

        return changed;
    }

}
