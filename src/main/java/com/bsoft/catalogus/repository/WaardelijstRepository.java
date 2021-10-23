package com.bsoft.catalogus.repository;

import com.bsoft.catalogus.model.WaardelijstDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WaardelijstRepository extends JpaRepository<WaardelijstDTO, Long> {
    String FIND_WAARDELIJST_WITH_URI = "select w.*, ww.*, c.* from waardelijst w, waardelijst_waarde ww, concept c " +
            "where w.uri = :uri and w.id = ww.waardelijst_id and ww.concept_id = c.id";

    Optional<WaardelijstDTO> findByUri(final String uri);

    @Query(value = FIND_WAARDELIJST_WITH_URI, nativeQuery = true)
    List<WaardelijstDTO> findWaardelijstUrl(@Param("uri") final String uri);
}
