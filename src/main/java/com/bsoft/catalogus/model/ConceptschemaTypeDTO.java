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

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "TYPES_CONCEPTSCHEMAS",
            joinColumns = {
                    @JoinColumn(name = "conceptschematype_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "conceptschema_id", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<ConceptschemaDTO> conceptschemas = new HashSet<>();
}


