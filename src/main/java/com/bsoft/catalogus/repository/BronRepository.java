package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.BronDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BronRepository extends JpaRepository<BronDTO, Long> {
    Optional<BronDTO> findByUri(final String uri);
}
