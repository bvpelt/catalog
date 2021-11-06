package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.TrefwoordDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrefwoordRepository extends JpaRepository<TrefwoordDTO, Long> {

    Optional<TrefwoordDTO> findByTrefwoord(final String trefwoord);
}
