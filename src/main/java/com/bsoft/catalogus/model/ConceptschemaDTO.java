package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity(name = "ConceptschemaDTO")
@Table(name = "CONCEPTSCHEMA")
@Data
public class ConceptschemaDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "NAAM")
    private String naam;

    @Column(name = "UITLEG")
    private String uitleg;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "BEGINDATUMGELDIGHEID")
    private String begindatumGeldigheid;

    @Column(name = "EINDDATUMGELDIGHEID")
    private String einddatumGeldigheid;

    @Column(name = "METADATA")
    private String metadata;

    //
    // Relations
    //

    //    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    //@ManyToMany
    @JoinTable(name = "CONCEPTSCHEMAS_TYPES",
            joinColumns = {
                    @JoinColumn(name = "conceptschema_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "conceptschematype_id", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<ConceptschemaTypeDTO> types = new HashSet<>();


    @OneToMany(mappedBy = "conceptschema", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH})
    private List<ConceptDTO> concepten;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptschemaDTO)) return false;
        ConceptschemaDTO that = (ConceptschemaDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(uri, that.uri) && Objects.equals(naam, that.naam) && Objects.equals(uitleg, that.uitleg) && Objects.equals(eigenaar, that.eigenaar) && Objects.equals(begindatumGeldigheid, that.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, that.einddatumGeldigheid) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uri, naam, uitleg, eigenaar, begindatumGeldigheid, einddatumGeldigheid, metadata);
    }
}


