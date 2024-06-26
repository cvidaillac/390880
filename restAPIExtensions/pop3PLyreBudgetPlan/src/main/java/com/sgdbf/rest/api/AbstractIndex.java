package com.sgdbf.rest.api;

import org.bonitasoft.web.extension.rest.RestAPIContext;
import org.bonitasoft.web.extension.rest.RestApiController;
import com.sgdbf.rest.api.dto.Error;
import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bonitasoft.web.extension.ResourceProvider;
import org.bonitasoft.web.extension.rest.RestApiResponse;
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Parent Controller class to hide technical parts
 */
public abstract class AbstractIndex implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getName());

    // Name of configuration parameters in configuration.properties
    public static final String CONFIG_PARAM_COOKIE = "serviceCookie";
    public static final String CONFIG_PARAM_HOST = "webserviceHost";
    public static final String CONFIG_PARAM_URL = "urlWSDL";
    
    // Name of REST-API request parameters
    public static final String PARAM_METHOD = "method";
    public static final String PARAM_ID = "id";
    public static final String PARAM_YEAR = "annee";
    public static final String PARAM_NODE_TYPE="targetNodeType";	// entite or region
    public static final String PARAM_NODE_CODE="targetNodeCode";	
    public static final String PARAM_YEAR_START="yearStart";
    public static final String PARAM_YEAR_END="yearEnd";
    public static final String PARAM_SELECT_RACHAT="rachatLocfi";
    public static final String PARAM_SELECT_CONTRAT="contrat";
    public static final String PARAM_GENRE="genre";
    public static final String PARAM_SOUS_GENRE="sousGenre";
    public static final String PARAM_ENERGY="codeEnergie";
    public static final String PARAM_RETROFIT="retrofit";
    
    // Node types
    public static final String NODE_TYPE_ENTITE = "entite";
    public static final String NODE_TYPE_REGION = "region";
        
    // Parameter value for method
    public static final String METHOD_QUERY = "query";
    public static final String METHOD_DETAILS = "getDetails";
    
    // Mandatory parameters for each method
    public static final String[] QUERY_MANDATORY_PARAMETERS = {PARAM_NODE_TYPE, PARAM_NODE_CODE, PARAM_YEAR_START, PARAM_YEAR_END, PARAM_SELECT_RACHAT, PARAM_SELECT_CONTRAT}; 
    public static final String[] DETAILS_MANDATORY_PARAMETERS = {PARAM_ID, PARAM_YEAR}; 

    // Optional parameters for each method
    public static final String[] QUERY_OPTIONAL_PARAMETERS = {PARAM_GENRE, PARAM_SOUS_GENRE, PARAM_ENERGY, PARAM_RETROFIT};
    public static final String[] DETAILS_OPTIONAL_PARAMETERS = {};
    
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    private String method;
    protected Map<String, String> methodParameters;

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {

        // Validate request
        try {
            method = validateInputParameters(request);
        } catch (ValidationException e) {
            LOGGER.error("Request for this REST API extension is not valid", e);
            return jsonResponse(responseBuilder, SC_BAD_REQUEST, Error.builder().message(e.getMessage()).build());
        }

        // Execute business logic
        Result result = execute(context, method);

        // Send the result as a JSON representation
        // You may use pagedJsonResponse if your result is multiple
        return jsonResponse(responseBuilder, SC_OK, result);
    }

    protected abstract Result execute(RestAPIContext context, String method);

    protected abstract String validateInputParameters(HttpServletRequest request);

    /**
     * Build an HTTP response.
     *
     * @param responseBuilder the Rest API response builder
     * @param httpStatus      the status of the response
     * @param body            the response body
     * @return a RestAPIResponse
     */
    RestApiResponse jsonResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Object body) {
        try {
            return responseBuilder
                    .withResponseStatus(httpStatus)
                    .withResponse(mapper.writeValueAsString(body))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write body response as JSON: " + body, e);
        }
    }


    /**
     * Returns a paged result like Bonita BPM REST APIs.
     * Build a response with a content-range.
     *
     * @param responseBuilder the Rest API response builder
     * @param body            the response body
     * @param p               the page index
     * @param c               the number of result per page
     * @param total           the total number of results
     * @return a RestAPIResponse
     */
    RestApiResponse pagedJsonResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Object body, int p, int c, long total) {
        try {
            return responseBuilder
                    .withContentRange(p, c, total)
                    .withResponseStatus(httpStatus)
                    .withResponse(mapper.writeValueAsString(body))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write body response as JSON: " + body, e);
        }
    }

    /**
     * Load a property file into a java.util.Properties
     */
    protected Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
        try {
            Properties props = new Properties();
            props.load(resourceProvider.getResourceAsStream(fileName));
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties for REST API extension");
        }
    }
}
