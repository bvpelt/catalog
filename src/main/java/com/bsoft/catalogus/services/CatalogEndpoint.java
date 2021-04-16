package com.bsoft.catalogus.services;

import com.bsoft.catalogus.api.ConceptschemasApi;
import com.bsoft.catalogus.model.InlineResponse200;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Component
@Slf4j
public class CatalogEndpoint extends AbstractBaseEndpoint implements ConceptschemasApi {

    private final String CONCEPT_SCHEMA_PREFIX = "/conceptschemas";

    public CatalogEndpoint(RestTemplate restTemplate,
                           @Value("${catalog.rest.api.baseurl}") String catalogBaseUrl,
                           @Value("${catalog.rest.api.key}") String apiKey) {
        super(catalogBaseUrl, apiKey, restTemplate);

    }

    public OperationResult getConceptschemas(String uri,
                                             String gepubliceerdDoor,
                                             String geldigOp,
                                             Integer page,
                                             Integer pageSize,
                                             List<String> expandScope) {
        try {
            ResponseEntity<InlineResponse200> responseEntity = conceptschemasGet(uri, gepubliceerdDoor, geldigOp, page, pageSize, expandScope);
            return OperationResult.success(responseEntity.getBody());
        } catch (HttpClientErrorException httpClientErrorException) {
            return OperationResult.failure(httpClientErrorException.getMessage());
        }
    }


    @Override
    public ResponseEntity<InlineResponse200> conceptschemasGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                               @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                               @Valid @RequestParam(value = "geldigOp", required = false) String geldigOp,
                                                               @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                               @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                               @Valid @RequestParam(value = "_expandScope", required = false) List<String> expandScope) {

        log.info("-------------------------------------------" + System.lineSeparator() +
                "REQUEST PARAMETERS START" + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Request url: " + getBaseUrl() + CONCEPT_SCHEMA_PREFIX + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Used headers: " + buildGetRequestHeaders().toString() + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "REQUEST PARAMETERS BEGIN" + System.lineSeparator() +
                "uri: " + uri + System.lineSeparator() +
                "gepubliceerdDoor: " + gepubliceerdDoor + System.lineSeparator() +
                "geldigOp: " + geldigOp + System.lineSeparator() +
                "page: " + page.toString() + System.lineSeparator() +
                "pageSize: " + pageSize.toString() + System.lineSeparator() +
                "_expandScope: " + expandScope.get(0) + System.lineSeparator() +
                "REQUEST PARAMETERS END" + System.lineSeparator() +
                "-------------------------------------------");

        return getRestTemplate().exchange(
                getBaseUrl() + CONCEPT_SCHEMA_PREFIX,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse200.class
        );
    }
}
