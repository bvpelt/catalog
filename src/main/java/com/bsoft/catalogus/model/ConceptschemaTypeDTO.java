package com.bsoft.catalogus.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ConceptschemaTypeDTO")
@Table(name = "CONCEPTSCHEMATYPE")
@Data
public class ConceptschemaTypeDTO implements Serializable {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE")
    private String type;

    @ManyToMany(mappedBy = "types", fetch = FetchType.LAZY)
    private Set<ConceptschemaDTO> conceptschemas = new HashSet<>();
}


