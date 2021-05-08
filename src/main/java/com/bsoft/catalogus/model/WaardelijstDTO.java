package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "BronDTO")
@Table(name = "BRON")
@Data
public class WaardelijstDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "NAAM")
    private String naam;

    @Column(name = "TITEL")
    private String titel;

    @Column(name = "BESCHRIJVING")
    private String beschrijving;

    @Column(name = "VERSIE")
    private String versie;

    @Column(name = "VERSIENOTITIES")
    private String versienotitie;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "METADATA")
    private String metadata;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "WAARDELIJST_WAARDE",
            joinColumns = {
                    @JoinColumn(name = "waardelijst_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "concept_id", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<ConceptDTO> waarden = new HashSet<>();
}


