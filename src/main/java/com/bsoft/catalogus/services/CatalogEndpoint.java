package com.bsoft.catalogus.services;

import com.bsoft.catalogus.api.BronnenApi;
import com.bsoft.catalogus.api.ConceptschemasApi;
import com.bsoft.catalogus.api.WaardelijstenApi;
import com.bsoft.catalogus.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Component
@Slf4j
public class CatalogEndpoint extends AbstractBaseEndpoint implements ConceptschemasApi, BronnenApi, WaardelijstenApi {

    private final String CONCEPT_SCHEMA_PREFIX = "/conceptschemas";
    private final String COLLECTIES_PREFIX = "/collecties";
    private final String CONCEPTEN_PREFIX = "/concepten";
    private final String BRONNEN_PREFIX = "/bronnen";
    private final String WAARDELIJSTEN_PREFIX = "/waardelijsten";

    public CatalogEndpoint(RestTemplate restTemplate,
                           @Value("${catalog.rest.api.baseurl}") String catalogBaseUrl,
                           @Value("${catalog.rest.api.key}") String apiKey) {
        super(catalogBaseUrl, apiKey, restTemplate);

    }

    public OperationResult getConceptschemas(final String uri,
                                             final String gepubliceerdDoor,
                                             final String geldigOp,
                                             final String zoekTerm,
                                             final Integer page,
                                             final Integer pageSize,
                                             final List<String> expandScope) {
        try {
            ResponseEntity<InlineResponse200> responseEntity = conceptschemasGet(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize, expandScope);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                log.info("CatalogEndpoint getConceptschemas:: result: {}", jsonString);
            } catch (Exception e) {
                log.error("CatalogEndpoint getConceptschemas cannot convert object to json");
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("CatalogEndpoint getConceptschemas result failed, http status code: " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            return OperationResult.failure(httpClientErrorException.getMessage());
        }
    }

    public ResponseEntity<InlineResponse200> conceptschemasGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                               @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                               @Valid @RequestParam(value = "geldigOp", required = false) String geldigOp,
                                                               @Valid @RequestParam(value = "zoekTerm", required = false) String zoekTerm,
                                                               @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                               @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                               @Valid @RequestParam(value = "_expandScope", required = false) List<String> expandScope) {

        logGetRequest(CONCEPT_SCHEMA_PREFIX);

        String parameters = getParameters(uri, gepubliceerdDoor, geldigOp, zoekTerm, null, null, null, page, pageSize, expandScope);

        return getRestTemplate().exchange(
                getBaseUrl() + CONCEPT_SCHEMA_PREFIX + parameters,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse200.class
        );
    }

    public OperationResult getCollecties(final String uri,
                                         final String gepubliceerdDoor,
                                         final String geldigOp,
                                         final String zoekTerm,
                                         final String conceptschema,
                                         final Integer page,
                                         final Integer pageSize,
                                         final List<String> expandScope) {
        try {
            ResponseEntity<InlineResponse2001> responseEntity = collectiesGet(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, page, pageSize, expandScope);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                log.info("CatalogEndpoint getCollecties:: result: {}", jsonString);
            } catch (Exception e) {
                log.error("CatalogEndpoint getCollecties cannot convert object to json");
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("CatalogEndpoint getCollecties result failed, http status code: " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            return OperationResult.failure(httpClientErrorException.getMessage());
        }
    }

    public ResponseEntity<InlineResponse2001> collectiesGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                            @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                            @Valid @RequestParam(value = "geldigOp", required = false) String geldigOp,
                                                            @Valid @RequestParam(value = "zoekTerm", required = false) String zoekTerm,
                                                            @Valid @RequestParam(value = "conceptschema", required = false) String conceptschema,
                                                            @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                            @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                            @Valid @RequestParam(value = "_expandScope", required = false) List<String> expandScope) {

        logGetRequest(COLLECTIES_PREFIX);

        String parameters = getParameters(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, null, null, page, pageSize, expandScope);

        return getRestTemplate().exchange(
                getBaseUrl() + COLLECTIES_PREFIX + parameters,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse2001.class);
    }

    public OperationResult getConcepten(final String uri,
                                        final String gepubliceerdDoor,
                                        final String geldigOp,
                                        final String zoekTerm,
                                        final String conceptschema,
                                        final String collectie,
                                        final String waardelijst,
                                        final Integer page,
                                        final Integer pageSize) {
        try {
            ResponseEntity<InlineResponse2002> responseEntity = conceptenGet(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, collectie, waardelijst, page, pageSize);

            try {
                if (responseEntity != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                    log.info("CatalogEndpoint getConcepten: result: {}", jsonString);
                } else {
                    log.error("CatalogEndpoint getConcepten, no result");
                    return OperationResult.failure("CatalogEndpoint getConcepten no results");
                }
            } catch (Exception e) {
                log.error("CatalogEndpoint getConcepten cannot convert object to json: {}", e);
            }

            if ((responseEntity != null) && responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("CatalogEndpoint getConcepten result failed, http status code: " + ((responseEntity == null) ? "unknown" : responseEntity.getStatusCode()));
            }
        } catch (Exception e) {
            log.error("CatalogEndpoint getConcepten: {}", e);
            return OperationResult.failure(e.getMessage());
        }
    }

    public ResponseEntity<InlineResponse2002> conceptenGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                           @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                           @Valid @RequestParam(value = "geldigOp", required = false) String geldigOp,
                                                           @Valid @RequestParam(value = "zoekTerm", required = false) String zoekTerm,
                                                           @Valid @RequestParam(value = "conceptschema", required = false) String conceptschema,
                                                           @Valid @RequestParam(value = "collectie", required = false) String collectie,
                                                           @Valid @RequestParam(value = "waardelijst", required = false) String waardelijst,
                                                           @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                           @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        logGetRequest(CONCEPTEN_PREFIX);

        String parameters = getParameters(uri, gepubliceerdDoor, geldigOp, zoekTerm, conceptschema, collectie, waardelijst, page, pageSize, null);

        ResponseEntity<InlineResponse2002> response = null;

        try {
            response = getRestTemplate().exchange(
                    getBaseUrl() + CONCEPTEN_PREFIX + parameters,
                    HttpMethod.GET,
                    new HttpEntity<>(buildGetRequestHeaders()),
                    InlineResponse2002.class);
        } catch (RestClientException e) {
            log.error("CatalogEndpoint conceptenGet: {}", e.fillInStackTrace());
            throw e;
        }
        return response;
    }


    public OperationResult getBron(final String uri,
                                   final String gepubliceerdDoor,
                                   final String geldigOp,
                                   final String zoekTerm,
                                   final Integer page,
                                   final Integer pageSize) {
        try {
            ResponseEntity<InlineResponse2003> responseEntity = bronnenGet(uri, gepubliceerdDoor, geldigOp, zoekTerm, page, pageSize);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                log.info("CatalogEndpoint getBron result: {}", jsonString);
            } catch (Exception e) {
                log.error("CatalogEndpoint getBron cannot convert object to json");
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("CatalogEndpoint getBron result failed, http status code: " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            return OperationResult.failure(httpClientErrorException.getMessage());
        }
    }


    public ResponseEntity<InlineResponse2003> bronnenGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                         @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                         @Valid @RequestParam(value = "geldigOp", required = false) String geldigOp,
                                                         @Valid @RequestParam(value = "zoekTerm", required = false) String zoekTerm,
                                                         @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                         @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        logGetRequest(BRONNEN_PREFIX);

        String parameters = getParameters(uri, gepubliceerdDoor, geldigOp, zoekTerm, null, null, null, page, pageSize, null);

        return getRestTemplate().exchange(
                getBaseUrl() + BRONNEN_PREFIX + parameters,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse2003.class);
    }

    public OperationResult getWaardenlijst(String uri,
                                           String gepubliceerdDoor,
                                           String zoekTerm,
                                           List<String> expandScope,
                                           Integer page,
                                           Integer pageSize
    ) {
        try {
            ResponseEntity<InlineResponse2004> responseEntity = waardelijstenGet(uri, gepubliceerdDoor, expandScope, zoekTerm, page, pageSize);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                log.info("CatalogEndpoint getWaardenlijst result: {}", jsonString);
            } catch (Exception e) {
                log.error("CatalogEndpoint getWaardenlijst cannot convert object to json");
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("CatalogEndpoint getWaardenlijst result failed, http status code: " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            return OperationResult.failure(httpClientErrorException.getMessage());
        }
    }

    public ResponseEntity<InlineResponse2004> waardelijstenGet(@Valid @RequestParam(value = "uri", required = false) String uri,
                                                               @Valid @RequestParam(value = "gepubliceerdDoor", required = false) String gepubliceerdDoor,
                                                               @Valid @RequestParam(value = "_expandScope", required = false) List<String> expandScope,
                                                               @Valid @RequestParam(value = "zoekTerm", required = false) String zoekTerm,
                                                               @Min(1) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                               @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        logGetRequest(WAARDELIJSTEN_PREFIX);

        String parameters = getParameters(uri, gepubliceerdDoor, null, zoekTerm, null, null, null, page, pageSize, expandScope);

        return getRestTemplate().exchange(
                getBaseUrl() + WAARDELIJSTEN_PREFIX + parameters,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse2004.class);
    }

    private void logGetRequest(final String prefix) {
        log.info("CatalogEndpoint " + prefix + " -------------------------------------------" + System.lineSeparator() +
                "REQUEST START" + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Request url: " + getBaseUrl() + prefix + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Used headers: " + buildGetRequestHeaders().toString() + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator());
    }

    private String getExpandScope(final List<String> expandScope, final String andSign) {
        String expandScopeString = "";
        if ((expandScope != null) && (expandScope.size() > 0)) {

            log.debug("CatalogEndpoint conceptschemasGet expandscope length: {} - content: {}", expandScope.size(), String.join(",", expandScope));

            for (int i = 0; i < expandScope.size(); i++) {
                expandScopeString = expandScopeString + andSign + String.format("_expandScope=%s", expandScope.get(i));
            }
        }

        return expandScopeString;
    }


    private String getParameters(final String uri,
                                 final String gepubliceerdDoor,
                                 final String geldigOp,
                                 final String zoekTerm,
                                 final String conceptschema,
                                 final String collectie,
                                 final String waardelijst,
                                 final Integer page,
                                 final Integer pageSize,
                                 final List<String> expandScope) {
        String parameters = "";
        String andSign = "";

        if ((uri != null) && (uri.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }

            parameters = parameters + String.format("uri=%s", uri);
            andSign = "&";
        }
        if ((gepubliceerdDoor != null) && (gepubliceerdDoor.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("gepubliceerdDoor=%s", gepubliceerdDoor);
            andSign = "&";
        }
        if ((geldigOp != null) && (geldigOp.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("geldigOp=%s", geldigOp);
            andSign = "&";
        }
        if ((zoekTerm != null) && (zoekTerm.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("zoekTerm=%s", zoekTerm);
            andSign = "&";
        }
        if ((conceptschema != null) && (conceptschema.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("conceptschema=%s", conceptschema);
            andSign = "&";
        }
        if ((collectie != null) && (collectie.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("collectie=%s", collectie);
            andSign = "&";
        }
        if ((waardelijst != null) && (waardelijst.length() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("waardelijst=%s", waardelijst);
            andSign = "&";
        }
        if (page != null) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("page=%d", page);
            andSign = "&";
        }
        if (pageSize != null) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            parameters = parameters + andSign + String.format("pageSize=%d", pageSize);
            andSign = "&";
        }
        String expandScopeString = getExpandScope(expandScope, andSign);
        if (expandScopeString.length() > 0) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            log.debug("CatalogEndpoint getParameters expandscope length: {} - content: {}", expandScope.size(), String.join(",", expandScope));

            parameters = parameters + expandScopeString;
        }

        log.info("CatalogEndpoint getParameters Parameters-------------------------------------------" + System.lineSeparator() +
                "REQUEST PARAMETERS BEGIN" + System.lineSeparator() +
                "uri: " + (uri == null ? "" : uri) + System.lineSeparator() +
                "gepubliceerdDoor: " + (gepubliceerdDoor == null ? "" : gepubliceerdDoor) + System.lineSeparator() +
                "geldigOp: " + (geldigOp == null ? "" : geldigOp) + System.lineSeparator() +
                "zoekTerm: " + (zoekTerm == null ? "" : zoekTerm) + System.lineSeparator() +
                "conceptschema: " + (conceptschema == null ? "" : conceptschema) + System.lineSeparator() +
                "collectie: " + (collectie == null ? "" : collectie) + System.lineSeparator() +
                "waardelijst: " + (waardelijst == null ? "" : waardelijst) + System.lineSeparator() +
                "page: " + page.toString() + System.lineSeparator() +
                "pageSize: " + pageSize.toString() + System.lineSeparator() +
                "expandScope: " + expandScopeString + System.lineSeparator() +
                "REQUEST PARAMETERS END" + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "PARAMETERS: " + parameters + System.lineSeparator() +
                "-------------------------------------------");
        return parameters;
    }
}
