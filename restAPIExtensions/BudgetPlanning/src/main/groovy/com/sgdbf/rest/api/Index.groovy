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

import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.PLANN3
import com.vega_systems.ws.locprotns.PROPBUDGET
import com.vega_systems.ws.locprotns.WSBPPLANNING3Result

import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
		
		def agencyCode = request.getParameter "agencyCode"
		if (agencyCode == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter agencyCode is needed"}""")
		}
		
		def budgetYear = request.getParameter "budgetYear"
		if (budgetYear == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter annualBudget is needed"}""")
		}
		
		def resultList=[]
		
		//DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd")
		//DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy")
		
		try
		{
	        Properties props = loadProperties("configuration.properties", context.resourceProvider)
			String urlWSDL = props["urlWSDL"]
			String serviceCookie = props["serviceCookie"]
			
			URL urlBudgePop = new URL(urlWSDL);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop)
	
			LocproPortType soapService = budgePopService.getLocproPort()
	
			WSBPPLANNING3Result result = soapService.wsbpPLANNING3(
					serviceCookie,
					budgetYear,
					agencyCode
					)
			
			if (result != null && result.getPLANN3S() != null)
			{
				for (PLANN3 currentPlan in result.getPLANN3S().getPLANN3()) 
				{
					Map item=new HashMap()
					item.niveau = currentPlan.niveau
					item.intitule = currentPlan.intitule
					item.codeNoeud = currentPlan.codeNoeud
					item.nomNoeud = currentPlan.nomNoeud
					
					if (currentPlan.datePresentation != null && currentPlan.datePresentation.length()>0 && !"NULL".equals(currentPlan.datePresentation)){
						
						item.datePresentation = df.format(df.parse(currentPlan.datePresentation))
					}
					
					item.enveloppeBudget = currentPlan.enveloppeBudget != null && !"NULL".equals(currentPlan.enveloppeBudget) ? currentPlan.enveloppeBudget : "0"
					
					if(currentPlan.enveloppeCamions)
					{
						item.enveloppeCamions = currentPlan.enveloppeCamions != null && !"NULL".equals(currentPlan.enveloppeCamions) ? currentPlan.enveloppeCamions : "0"
					}
					if(currentPlan.enveloppeChariots)
					{
						item.enveloppeChariots = currentPlan.enveloppeChariots != null && !"NULL".equals(currentPlan.enveloppeChariots) ? currentPlan.enveloppeChariots : "0"
					}
					if(currentPlan.enveloppeAccessoires)
					{
						item.enveloppeAccessoires = currentPlan.enveloppeAccessoires != null && !"NULL".equals(currentPlan.enveloppeAccessoires) ? currentPlan.enveloppeAccessoires : "0"
					}
					
					if (currentPlan.rf1 != null && currentPlan.rf1.length()>0 && !"NULL".equals(currentPlan.rf1)){
						item.rf1 = df.format(df.parse(currentPlan.rf1))
					}
					if (currentPlan.rf2 != null && currentPlan.rf2.length()>0 && !"NULL".equals(currentPlan.rf2)){
						item.rf2 = df.format(df.parse(currentPlan.rf2))
					}
					if (currentPlan.rf3 != null && currentPlan.rf3.length()>0 && !"NULL".equals(currentPlan.rf3)){
						item.rf3 = df.format(df.parse(currentPlan.rf3))
					}
					if (currentPlan.rf4 != null && currentPlan.rf4.length()>0 && !"NULL".equals(currentPlan.rf4)){
						item.rf4 = df.format(df.parse(currentPlan.rf4))
					}
					
					resultList.add(item);
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
