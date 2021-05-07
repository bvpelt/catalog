package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.ConceptDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConceptRepository extends JpaRepository<ConceptDTO, Long> {
    Optional<ConceptDTO> findByUri(final String uri);
}
