package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

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

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "CONCEPTSCHEMAS_TYPES",
            joinColumns = {
                    @JoinColumn(name = "CONCEPTSCHEMA_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "CONCEPTSCHEMATYPE_ID", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<ConceptschemaTypeDTO> types = new HashSet<>();

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


//    @OneToMany(mappedBy = "conceptschema", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
//            CascadeType.REFRESH})
/*
    @OneToMany(mappedBy = "conceptschema", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ConceptDTO> concepten;
*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptschemaDTO)) return false;
        ConceptschemaDTO that = (ConceptschemaDTO) o;
        return uri.equals(that.uri) && Objects.equals(naam, that.naam) && Objects.equals(uitleg, that.uitleg) && Objects.equals(eigenaar, that.eigenaar) && begindatumGeldigheid.equals(that.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, that.einddatumGeldigheid) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, naam, uitleg, eigenaar, begindatumGeldigheid, einddatumGeldigheid, metadata);
    }
}


