package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.ConceptschemaDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConceptschemaRepository extends JpaRepository<ConceptschemaDTO, Long> {
    Optional<ConceptschemaDTO> findByUri(final String uri);
}
