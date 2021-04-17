package com.bsoft.catalogus.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ConceptschemaDTO")
@Table(name = "CONCEPTSCHEMA")
@Data
public class ConceptschemaDTO implements Serializable {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @ManyToMany(mappedBy = "conceptschemas", fetch = FetchType.LAZY)
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
}


