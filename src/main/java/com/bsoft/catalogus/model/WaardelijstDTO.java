package com.bsoft.catalogus.model;

import lombok.Data;
import org.springframework.data.domain.Page;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "WaardelijstDTO")
@Table(name = "WAARDELIJST")
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
    private String versienotities;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "METADATA")
    private String metadata;

    @OneToMany(mappedBy = "waardelijst", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ConceptDTO> waarden;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaardelijstDTO)) return false;
        WaardelijstDTO that = (WaardelijstDTO) o;
        return Objects.equals(uri, that.uri) && Objects.equals(naam, that.naam) && Objects.equals(titel, that.titel) && Objects.equals(beschrijving, that.beschrijving) && Objects.equals(versie, that.versie) && Objects.equals(versienotities, that.versienotities) && Objects.equals(eigenaar, that.eigenaar) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, naam, titel, beschrijving, versie, versienotities, eigenaar, metadata);
    }
}


