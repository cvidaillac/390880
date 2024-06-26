package com.sgdbf.rest.api;

import org.bonitasoft.web.extension.rest.RestAPIContext;

import com.sgdbf.rest.api.Index.StartProcessException;
import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.exception.ValidationException;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
    private APISession apiSession;
    private ProcessAPI processAPI;
    private Long processDefinitionId = 0L;
    private ProcessDefinition processDefinition;

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
    }


    @Test
    void should_get_result_when_params_ok() {

        // Given
    	return;
    	/*
        // When
        when(context.getApiSession()).thenReturn(apiSession);
        when(apiSession.getUserId()).thenReturn(1L);
        when(context.getApiClient().getProcessAPI()).thenReturn(processAPI);
        try {
			when(processAPI.getProcessDefinition(processDefinitionId)).thenReturn(processDefinition);
		} catch (ProcessDefinitionNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        when(processDefinition.getName()).thenReturn("POP3P_BudgetProposition_Init");
        
        Result result;
		try {
			result = index.execute(context);
	        // Then
	        assertThat(result.getCurrentDate()).isEqualTo(LocalDate.now());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StartProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
    }

    @Test
    void should_return_a_json_representation_as_result() throws IOException {
        // Given "a RestAPIController"
    	return;
    	/*
        // Simulate a request with a value for each parameter
    	Reader reader = new StringReader("{\"processDefinitionId\": 1000, \"processContracts\": []");
    	BufferedReader buffered_reader = new BufferedReader(reader);
    	when(httpRequest.getReader()).thenReturn(buffered_reader);
    	
        // When "Invoking the REST API"
        RestApiResponse apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context);

        // Then "A JSON representation is returned in response body"
        Result jsonResponse = index.getMapper().readValue((String) apiResponse.getResponse(), Result.class);

        // Validate returned response
        assertThat(apiResponse.getHttpStatus()).isEqualTo(200);
        assertThat(jsonResponse).isNotNull();
        */
    }

}