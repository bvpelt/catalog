package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.ConceptschemaTypeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConceptschemaTypeRepository extends JpaRepository<ConceptschemaTypeDTO, Long> {

    Optional<ConceptschemaTypeDTO> findByType(final String type);
}
