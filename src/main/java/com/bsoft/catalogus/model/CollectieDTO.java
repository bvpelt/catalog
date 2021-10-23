package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CollectieDTO")
@Table(name = "COLLECTIE")
@Data
public class CollectieDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "TERM")
    private String term;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CONCEPTSCHEMA_ID")
    private ConceptschemaDTO conceptschema;

    @Column(name = "BEGINDATUM")
    private String begindatumGeldigheid;

    @Column(name = "EINDDATUM")
    private String einddatumGeldigheid;

    @Column(name = "METADATA")
    private String metadata;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectieDTO)) return false;
        CollectieDTO that = (CollectieDTO) o;
        return Objects.equals(uri, that.uri) && Objects.equals(type, that.type) && Objects.equals(term, that.term) && Objects.equals(eigenaar, that.eigenaar) && Objects.equals(conceptschema, that.conceptschema) && Objects.equals(begindatumGeldigheid, that.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, that.einddatumGeldigheid) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, type, term, eigenaar, conceptschema, begindatumGeldigheid, einddatumGeldigheid, metadata);
    }
}
