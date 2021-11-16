package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "ConceptDTO")
@Table(name = "CONCEPT")
@Data
public class ConceptDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "NAAM")
    private String naam;

    @Column(name = "TERM")
    private String term;

    @Column(name = "UITLEG")
    private String uitleg;

    @Column(name = "DEFINITIE")
    private String definitie;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "CONCEPT_TREFWOORD",
            joinColumns = {
                    @JoinColumn(name = "CONCEPT_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "TREFWOORD_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<TrefwoordDTO> trefwoorden = new HashSet<>();

    @Column(name = "EIGENAAR")
    private String eigenaar;

    private String conceptschema;

    //   private List<String> toelichtingen; //
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "CONCEPT_TOELICHTING",
            joinColumns = {
                    @JoinColumn(name = "CONCEPT_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "TOELICHTING_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<ToelichtingDTO> toelichtingen = new HashSet<>();

    //   private List<String> rationales;
    //   private List<String> verbeeldingen;
    //   private List<String> codes;
    @Column(name = "BEGINDATUM")
    private String begindatumGeldigheid;

    @Column(name = "EINDDATUM")
    private String einddatumGeldigheid;
    //   private List<String> verborgenZoektermen;
    //   private List<String> synoniemen;
    //   private List<String> bronnen;
    //   private List<String> heeftBetrekkingOp;
    //   private List<String> isOnderdeelVan;
    //   private List<String> bestaatUit;
    //   private List<String> isSpecialisatieVan;
    //   private List<String> isGeneralisatieVan;
    //   private List<String> isHetzelfdeAls;
    //   private List<String> isOngeveerHetzelfdeAls;
    //   private List<String> zieOok;
    //   private List<String> isBrederDan;
    //   private List<String> isEngerDan;
    //   private List<String> isGerelateerd;
    //   private List<String> isHarmonisatie;
    @Column(name = "METADATA")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    private WaardelijstDTO waardelijst;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConceptDTO that = (ConceptDTO) o;
        return Objects.equals(uri, that.uri) && Objects.equals(type, that.type) && Objects.equals(naam, that.naam) && Objects.equals(term, that.term) && Objects.equals(uitleg, that.uitleg) && Objects.equals(definitie, that.definitie) && Objects.equals(eigenaar, that.eigenaar) && Objects.equals(conceptschema, that.conceptschema) && Objects.equals(begindatumGeldigheid, that.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, that.einddatumGeldigheid) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, type, naam, term, uitleg, definitie, eigenaar, conceptschema, begindatumGeldigheid, einddatumGeldigheid, metadata);
    }
}
