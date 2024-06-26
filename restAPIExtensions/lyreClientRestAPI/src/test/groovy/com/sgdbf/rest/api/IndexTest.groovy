package com.sgdbf.rest.api;

import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import spock.lang.Specification

import com.bonitasoft.web.extension.rest.RestAPIContext

/**
 * @see http://spockframework.github.io/spock/docs/1.0/index.html
 */
class IndexTest extends Specification {

    // Declare mocks here
    // Mocks are used to simulate external dependencies behavior
    def httpRequest = Mock(HttpServletRequest)
    def resourceProvider = Mock(ResourceProvider)
    def context = Mock(RestAPIContext)

    /**
     * You can configure mocks before each tests in the setup method
     */
    def setup(){
        // Simulate access to configuration.properties resource
        context.resourceProvider >> resourceProvider
        resourceProvider.getResourceAsStream("configuration.properties") >> IndexTest.class.classLoader.getResourceAsStream("testConfiguration.properties")
    }

//    def should_return_a_json_representation_as_result() {
//        given: "a RestAPIController"
//        def index = new Index()
//        // Simulate a request with a value for each parameter
//        httpRequest.getParameter("method") >> "aValue1"
//
//        when: "Invoking the REST API"
//        def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)
//
//        then: "A JSON representation is returned in response body"
//        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
//        // Validate returned response
//        apiResponse.httpStatus == 200
//        jsonResponse.method == "aValue1"
//        jsonResponse.myParameterKey == "testValue"
//    }

}