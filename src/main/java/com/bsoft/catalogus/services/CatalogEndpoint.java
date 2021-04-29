package com.bsoft.catalogus.services;

import com.bsoft.catalogus.api.ConceptschemasApi;
import com.bsoft.catalogus.model.InlineResponse200;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
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
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseEntity.getBody());
                log.info("getConceptschemas:: result: {}", jsonString);
            } catch (Exception e) {
                log.error("cannot convert object to json");
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return OperationResult.success(responseEntity.getBody());
            } else {
                return OperationResult.failure("Result failed, http status code: " + responseEntity.getStatusCode());
            }
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
        if ((expandScope != null) && (expandScope.size() > 0)) {
            if (parameters.length() == 0) {
                parameters = "?";
            }
            //parameters = parameters + andSign + String.format("_expandScope=%s", String.join(",", expandScope));
            for(int i = 0; i < expandScope.size(); i++) {
                parameters = parameters + andSign + String.format("_expandScope=%s", expandScope.get(i));
            }
        }

        log.info("-------------------------------------------" + System.lineSeparator() +
                "REQUEST PARAMETERS START" + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Request url: " + getBaseUrl() + CONCEPT_SCHEMA_PREFIX + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "Used headers: " + buildGetRequestHeaders().toString() + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "REQUEST PARAMETERS BEGIN" + System.lineSeparator() +
                "uri: " + (uri == null ? "" : uri) + System.lineSeparator() +
                "gepubliceerdDoor: " + (gepubliceerdDoor == null ? "" : gepubliceerdDoor) + System.lineSeparator() +
                "geldigOp: " + (geldigOp == null ? "" : geldigOp) + System.lineSeparator() +
                "page: " + page.toString() + System.lineSeparator() +
                "pageSize: " + pageSize.toString() + System.lineSeparator() +
                "_expandScope: " + (expandScope == null ? "": expandScope.get(0) )+ System.lineSeparator() +
                "REQUEST PARAMETERS END" + System.lineSeparator() +
                "-------------------------------------------" + System.lineSeparator() +
                "PARAMETERS: " + parameters + System.lineSeparator() +
                "-------------------------------------------");


        return getRestTemplate().exchange(
                getBaseUrl() + CONCEPT_SCHEMA_PREFIX + parameters,
                HttpMethod.GET,
                new HttpEntity<>(buildGetRequestHeaders()),
                InlineResponse200.class
        );

    }
}
