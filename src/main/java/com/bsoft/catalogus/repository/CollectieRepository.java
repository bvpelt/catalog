package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.CollectieDTO;
import com.bsoft.catalogus.model.ConceptDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectieRepository extends JpaRepository<CollectieDTO, Long> {
    Optional<CollectieDTO> findByUri(final String uri);
}
