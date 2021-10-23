package com.bsoft.catalogus.model;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity(name = "ConceptschemaTypeDTO")
@Table(name = "CONCEPTSCHEMATYPE")
@Data
public class ConceptschemaTypeDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE")
    private String type;

    @ManyToMany(mappedBy = "types", fetch = FetchType.LAZY)
    private Set<ConceptschemaDTO> conceptschema = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptschemaTypeDTO)) return false;
        ConceptschemaTypeDTO that = (ConceptschemaTypeDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}


