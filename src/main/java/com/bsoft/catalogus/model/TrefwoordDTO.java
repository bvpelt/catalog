package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Objects;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conceptdto_id", nullable = false)
    private ConceptDTO concept;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrefwoordDTO)) return false;
        TrefwoordDTO that = (TrefwoordDTO) o;
        return Objects.equals(trefwoord, that.trefwoord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trefwoord);
    }
}
