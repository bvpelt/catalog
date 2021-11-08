package com.bsoft.catalogus.controller;

import com.bsoft.catalogus.model.*;
import com.bsoft.catalogus.repository.*;
import com.bsoft.catalogus.services.CatalogService;
import com.bsoft.catalogus.services.OperationResult;
import com.bsoft.catalogus.util.SetUtils;
import com.bsoft.catalogus.util.StringChanged;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class WaardelijstLoader {

    private WaardelijstRepository waardelijstRepository;

    private ConceptRepository conceptRepository;

    private TrefwoordRepository trefwoordRepository;

    private ToelichtingRepository toelichtingRepository;

    public WaardelijstLoader(final WaardelijstRepository waardelijstRepository,
                             final ConceptRepository conceptRepository,
                             final TrefwoordRepository trefwoordRepository,
                             final ToelichtingRepository toelichtingRepository) {
        this.waardelijstRepository = waardelijstRepository;
        this.conceptRepository = conceptRepository;
        this.trefwoordRepository = trefwoordRepository;
        this.toelichtingRepository = toelichtingRepository;
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

        Instant start = Instant.now();
        result = catalogService.getWaardelijst(uri, gepubliceerdDoor, zoekTerm, expandScope, page, pageSize);
        Instant finish = Instant.now();
        long time = Duration.between(start, finish).toMillis();
        log.debug("Timing data getwaardelijsten: {} ms ", time);

        if ((result != null) && result.isSuccess()) {
            InlineResponse2004 inlineResponse2004 = result.getSuccessResult();
            InlineResponse2004Embedded embedded = inlineResponse2004.getEmbedded();
            List<Waardelijst> waardelijsts = embedded.getWaardelijsten();

            if ((waardelijsts != null) && (!waardelijsts.isEmpty())) {
                persistWaardelijsten(waardelijsts, procesResult);
            }

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

            if ((result != null) && result.getFailureResult() != null) {
                log.debug("WaardelijstLoader getPage, result: " + result.getFailureResult().toString());
                procesResult.setMessage(result.getFailureResult().toString());
            } else {
                procesResult.setMessage("Error during processing request /waardelijsten");
            }

            log.info("WaardelijstLoader getPage: no result stop processing");
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

        try {
            boolean attributesChanged = true; // always true for new entry
            boolean relationWaardenChanged = true; // always true for new entry

            if (!newEntry) {
                attributesChanged = changedAttributes(waardelijst, waardelijstDTO);
                relationWaardenChanged = changedWaarden(waardelijst, waardelijstDTO);
            }

            log.debug("WaardelijstLoader convertToWaardelijstDTO newEntry               : {}", newEntry);
            log.debug("WaardelijstLoader convertToWaardelijstDTO attributesChanged      : {}", attributesChanged);
            log.debug("WaardelijstLoader convertToWaardelijstDTO relationWaardenChanged : {}", relationWaardenChanged);

            if (attributesChanged || newEntry) {
                log.info("ConceptLoader convertToConceptDTO new or changed attributes");
                //
                // for new all attributes and for existing some attributes
                //
                waardelijstDTO.setUri(waardelijst.getUri());
                waardelijstDTO.setNaam(waardelijst.getNaam().isPresent() ? waardelijst.getNaam().get() : null);
                waardelijstDTO.setTitel(waardelijst.getTitel());
                waardelijstDTO.setBeschrijving(waardelijst.getBeschrijving().isPresent() ? waardelijst.getBeschrijving().get() : null);
                waardelijstDTO.setVersie(waardelijst.getVersie());
                waardelijstDTO.setVersienotities(waardelijst.getVersienotities().isPresent() ? waardelijst.getVersienotities().get() : null);
                waardelijstDTO.setEigenaar(waardelijst.getEigenaar());
                waardelijstDTO.setMetadata(waardelijst.getMetadata());

                savedWaardelijst = waardelijstRepository.save(waardelijstDTO);
            } else {
                log.debug("WaardelijstLoader convertToWaardelijstDTO existing, not new or changed attributes");
                savedWaardelijst = waardelijstDTO;
            }

            if (relationWaardenChanged) {
                log.debug("WaardelijstLoader convertToWaardelijstDTO relation termen changed, remove all relations");
                if (savedWaardelijst.getWaarden() != null) {
                    savedWaardelijst.getWaarden().removeAll(savedWaardelijst.getWaarden());
                }
            }

            if (newEntry || relationWaardenChanged) {
                log.debug("WaardelijstLoader convertToWaardelijstDTO new or relations changed, add all relations");

                Set<ConceptDTO> waarden = new HashSet<>();
                Set<WaardelijstDTO> waardelijsten = new HashSet<>();
                waardelijsten.add(savedWaardelijst);

                // Waarden
                if (waardelijst.getEmbedded().getWaarden().isPresent() && waardelijst.getEmbedded().getWaarden().get() != null) {
                    List<Concept> concepten = waardelijst.getEmbedded().getWaarden().get(); // de waarden van de waardelijst

                    for (int i = 0; i < concepten.size(); i++) { // for each concept (waarde)
                        Concept concept = concepten.get(i);
                        ConceptDTO savedConcept;

                        log.trace("WaardelijstLoader convertToWaardelijstDTO: checking [{}] concept: {}", i, concept.getUri());
                        Optional<ConceptDTO> conceptDTOOptional = conceptRepository.findByUri(concept.getUri());

                        if (conceptDTOOptional.isPresent()) { // existing concept (waarde)
                            ConceptDTO conceptDTOold = conceptDTOOptional.get();
                            log.trace("WaardelijstLoader convertToWaardelijstDTO: found concept - id: {} uri: {}", conceptDTOold.getId(), conceptDTOold.getUri());
                            conceptDTOold.setWaardelijst(savedWaardelijst);
                            savedConcept = conceptRepository.save(conceptDTOold);
                        } else { // new concept (waarde)
                            ConceptDTO newConcept = new ConceptDTO();
                            newConcept.setUri(concept.getUri());
                            newConcept.setType(concept.getType());
                            newConcept.setNaam(concept.getNaam());
                            newConcept.setTerm(concept.getTerm());
                            newConcept.setUitleg(concept.getUitleg().isPresent() ? concept.getUitleg().get() : null);
                            newConcept.setDefinitie(concept.getDefinitie().isPresent() ? concept.getDefinitie().get() : null);
                            newConcept.setEigenaar(concept.getEigenaar().isPresent() ? concept.getEigenaar().get() : null);
                            newConcept.setConceptschema(concept.getConceptschema());
                            newConcept.setBegindatumGeldigheid(concept.getBegindatumGeldigheid());
                            newConcept.setEinddatumGeldigheid(concept.getEinddatumGeldigheid().isPresent() ? concept.getEinddatumGeldigheid().get() : null);
                            newConcept.setMetadata(concept.getMetadata());
                            newConcept.setWaardelijst(savedWaardelijst);
                            savedConcept = conceptRepository.save(newConcept);

                            Set<ConceptDTO> conceptSet = new HashSet<>();
                            conceptSet.add(savedConcept);

                            // Trefwoorden
                            if (concept.getTrefwoorden().isPresent() && concept.getTrefwoorden().get() != null) {
                                addTrefwoorden(concept.getTrefwoorden().get(), savedConcept,  conceptSet);
                            }

                            // Toelichtingen
                            if (concept.getToelichtingen().isPresent() && concept.getToelichtingen().get() != null) {
                                addToelichtingen(concept.getToelichtingen().get(), savedConcept, conceptSet);
                            }
                        }
                        waarden.add(savedConcept);
                    }
                    savedWaardelijst.setWaarden(waarden);
                    waardelijstRepository.save(savedWaardelijst);
                }
            }

            if (newEntry) { // new entry
                procesResult.setNewEntries(procesResult.getNewEntries() + 1);
            } else if (attributesChanged || relationWaardenChanged) { // changed
                procesResult.setUpdatedEntries(procesResult.getUpdatedEntries() + 1);
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

    private boolean changedAttributes(final Waardelijst waardelijst, final WaardelijstDTO waardelijstDTO) {
        boolean changed = false;

        changed = StringChanged.stringChanged(waardelijst.getUri(), waardelijstDTO.getUri());

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getNaam(), waardelijstDTO.getNaam());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getTitel(), waardelijstDTO.getTitel());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getBeschrijving(), waardelijstDTO.getBeschrijving());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getVersie(), waardelijstDTO.getVersie());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getVersienotities(), waardelijstDTO.getVersienotities());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getEigenaar(), waardelijstDTO.getEigenaar());
        }

        if (!changed) {
            changed = StringChanged.stringChanged(waardelijst.getMetadata(), waardelijstDTO.getMetadata());
        }

        return changed;
    }

    private boolean changedWaarden(final Waardelijst waardelijst, final WaardelijstDTO waardelijstDTO) {
        boolean changed = false;

        List<Concept> waarden = null;
        Set<Concept> waardenSet = new HashSet<>();

        if (waardelijst.getEmbedded().getWaarden().isPresent() && waardelijst.getEmbedded().getWaarden().get() != null) {
            // waarden aanwezig
            waarden = waardelijst.getEmbedded().getWaarden().get();

            for (Concept c: waarden) {
                waardenSet.add(c);
            }
        }

        // both sets need values
        // waardenSet is empty set
        if (waardelijstDTO.getWaarden() != null) {
            changed = !SetUtils.equals(waardenSet, waardelijstDTO.getWaarden());
        } else {
            changed = waardenSet.size() != 0;
        }
        return changed;
    }

}
