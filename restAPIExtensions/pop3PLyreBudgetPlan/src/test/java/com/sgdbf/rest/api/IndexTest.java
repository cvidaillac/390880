package com.sgdbf.rest.api;

import org.bonitasoft.web.extension.rest.RestAPIContext;
import com.sgdbf.rest.api.dto.Error;
import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.exception.ValidationException;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.ResourceProvider;
import org.bonitasoft.web.extension.rest.RestApiResponse;
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class IndexTest {

    // Declare mocks here
    // Mocks are used to simulate external dependencies behavior
    @Mock(lenient = true)
    private HttpServletRequest httpRequest;
    @Mock(lenient = true)
    private ResourceProvider resourceProvider;
    @Mock(lenient = true)
    private RestAPIContext context;

    @Mock(lenient = true)
    private APISession session_api;

    // The controller to test
    private Index index;

    /**
     * You can configure mocks before each tests in the setup method
     */
    @BeforeEach
    void setUp() throws FileNotFoundException {
        // Create a new instance under test
        index = new Index();

        // Simulate access to configuration.properties resource
        when(context.getResourceProvider()).thenReturn(resourceProvider);
        when(resourceProvider.getResourceAsStream("configuration.properties"))
                .thenReturn(IndexTest.class.getResourceAsStream("/testConfiguration.properties"));
        
        when(context.getApiSession()).thenReturn(session_api);
        when(session_api.getUserName()).thenReturn("TEST_USER");
    }

    @Test
    void should_throw_exception_if_mandatory_input_is_missing() {
        assertThrows(ValidationException.class, () ->
                index.validateInputParameters(httpRequest)
        );
    }

    @Test
    void should_get_result_when_params_ok() {

        // Simulate a request with a value for each parameter
        when(httpRequest.getParameter("method")).thenReturn("getDetails");
        when(httpRequest.getParameter("id")).thenReturn("aValue2");
        when(httpRequest.getParameter("annee")).thenReturn("2022");
        String method = index.validateInputParameters(httpRequest);

        // When
        Result result = index.execute(context, method);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void should_return_a_json_representation_as_result() throws IOException {
        // Given "a RestAPIController"

        // Simulate a request with a value for each parameter
        when(httpRequest.getParameter("method")).thenReturn("getDetails");
        when(httpRequest.getParameter("id")).thenReturn("aValue2");
        when(httpRequest.getParameter("annee")).thenReturn("2022");

        // When "Invoking the REST API"
        RestApiResponse apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context);

        // Then "A JSON representation is returned in response body"
        Result jsonResponse = index.getMapper().readValue((String) apiResponse.getResponse(), Result.class);

        // Validate returned response
        assertThat(apiResponse.getHttpStatus()).isEqualTo(200);
        assertThat(jsonResponse).isNotNull();
    }

    @Test
    void should_return_an_error_response_if_method_is_not_set() throws IOException {
        // Given "a request without method"
        when(httpRequest.getParameter("method")).thenReturn(null);

        // When "Invoking the REST API"
        RestApiResponse apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context);

        // Then "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
        Error jsonResponse = index.getMapper().readValue((String) apiResponse.getResponse(), Error.class);
        // Validate returned response
        assertThat(apiResponse.getHttpStatus()).isEqualTo(400);
        assertThat(jsonResponse.getMessage()).isEqualTo("the parameter method is missing");
    }

}