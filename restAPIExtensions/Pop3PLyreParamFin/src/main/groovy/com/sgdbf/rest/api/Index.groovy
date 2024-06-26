package com.sgdbf.rest.api;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.http.HttpHeaders
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.vega_systems.ws.locprotns.COUPLEFIN
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.WSBPPARAMFINResult

import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController

import java.time.LocalDate

class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
		def entityCode = request.getParameter "entity"
		if (entityCode == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter entity is needed"}""")
		}
		
		def budgetYear = request.getParameter "budgetYear"
		if (budgetYear == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter annualBudget is needed"}""")
		}
		
		def materialType = request.getParameter "materialType"
		if (materialType == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter materialType is needed"}""")
		}
		
		def materialSubType = request.getParameter "materialSubType"
		if (materialSubType == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter materialSubType is needed"}""")
		}
		
		def requestType = request.getParameter "requestType"
		if (requestType == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter requestType is needed"}""")
		}
		
		def resultList=[]
		
		try
		{
			Properties props = loadProperties("configuration.properties", context.resourceProvider)
			String urlWSDL = props["urlWSDL"]
			String serviceCookie = props["serviceCookie"]
			
			URL urlBudgePop = new URL(urlWSDL)
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
	
			LocproPortType soapService = budgePopService.getLocproPort()
			
			WSBPPARAMFINResult result = soapService.wsbpPARAMFIN(
														serviceCookie,
														entityCode,
														budgetYear,
														materialType,
														materialSubType,
														requestType
														)
														
			if (result != null && result.getCOUPLEFINS()!= null)
			{
				for (COUPLEFIN currentPair in result.getCOUPLEFINS().getCOUPLEFIN())
				{
					Map item=new HashMap()
					
					item.nombreMois = currentPair.nombreMois
					item.pourcentageNegocie = currentPair.pourcentageNegocie
					
					resultList.add(item)
					
				}	
			}												
			
		} catch(Exception e) {
			LOGGER.error("Error from Lyre web service : ", e)
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, new JsonBuilder(e).toPrettyString())
		}
		
        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(resultList).toString())
    }

    /**
     * Build an HTTP response.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  httpStatus the status of the response
     * @param  body the response body
     * @return a RestAPIResponse
     */
    RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder.with {
            withResponseStatus(httpStatus)
            withResponse(body)
            build()
        }
    }

    /**
     * Returns a paged result like Bonita BPM REST APIs.
     * Build a response with a content-range.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  body the response body
     * @param  p the page index
     * @param  c the number of result per page
     * @param  total the total number of results
     * @return a RestAPIResponse
     */
    RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
        return responseBuilder.with {
            withContentRange(p,c,total)
            withResponse(body)
            build()
        }
    }

    /**
     * Load a property file into a java.util.Properties
     */
    Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
        Properties props = new Properties()
        resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
            props.load s
        }
        props
    }

}
