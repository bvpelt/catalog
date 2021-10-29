package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.ToelichtingDTO;
import com.bsoft.catalogus.model.TrefwoordDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToelichtingRepository extends JpaRepository<ToelichtingDTO, Long> {

    Optional<ToelichtingDTO> findByToelichting(final String toelichting);
}
