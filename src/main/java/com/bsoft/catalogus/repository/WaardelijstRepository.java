package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.WaardelijstDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaardelijstRepository extends JpaRepository<WaardelijstDTO, Long> {
    Optional<WaardelijstDTO> findByUri(final String uri);
}
