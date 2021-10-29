package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "TrefwoordDTO")
@Table(name = "TREFWOORD")
@Data
public class TrefwoordDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TREFWOORD")
    private String trefwoord;

    @ManyToMany(mappedBy = "trefwoorden", fetch = FetchType.LAZY)
    private Set<ConceptDTO> concept = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrefwoordDTO)) return false;
        TrefwoordDTO that = (TrefwoordDTO) o;
        return Objects.equals(trefwoord, that.trefwoord) && Objects.equals(concept, that.concept);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trefwoord, concept);
    }
}