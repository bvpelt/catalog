package com.bsoft.catalogus.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
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

    @OneToMany(mappedBy = "conceptschema", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<ConceptschemaTypeDTO> types;

    @Column(name = "NAAM")
    private String naam;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "BEGINDATUMGELDIGHEID")
    private String begindatumGeldigheid;
}


