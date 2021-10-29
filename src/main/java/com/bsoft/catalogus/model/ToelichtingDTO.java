package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "ToelichtingDTO")
@Table(name = "TOELICHTING")
@Data
public class ToelichtingDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TOELICHTING")
    private String toelichting;

    @ManyToMany(mappedBy = "trefwoorden", fetch = FetchType.LAZY)
    private Set<ConceptDTO> concept = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToelichtingDTO)) return false;
        ToelichtingDTO that = (ToelichtingDTO) o;
        return Objects.equals(toelichting, that.toelichting) && Objects.equals(concept, that.concept);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toelichting, concept);
    }
}
