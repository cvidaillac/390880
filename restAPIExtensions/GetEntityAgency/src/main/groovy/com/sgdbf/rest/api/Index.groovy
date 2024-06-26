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

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.vega_systems.ws.locprotns.ArrayOfENTITE
import com.vega_systems.ws.locprotns.ArrayOfENTITEAGENCE
import com.vega_systems.ws.locprotns.ENTITE
import com.vega_systems.ws.locprotns.ENTITEAGENCE
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.WSBPAGENCESENTITEResponse
import com.vega_systems.ws.locprotns.WSBPAGENCESENTITEResult
import com.vega_systems.ws.locprotns.WSBPLSTENTITESALLResponse
import com.vega_systems.ws.locprotns.WSBPLSTENTITESALLResult


class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
		def resultList=[]
		def finalResult = null
		
		def q = request.getParameter "q"
		if (q == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"code" : "99", "status" : "KO", "error" : "the parameter q is missing"}""")
		}

		
		Properties props = loadProperties("configuration.properties", context.resourceProvider)
		String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
		
		try 
		{
			URL urlBudgePop = new URL(urlWSDL)
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop)
			
			LocproPortType soapService = budgePopService.getLocproPort()
			
			if("getEntities".equals(q))
			{
			
				WSBPLSTENTITESALLResult result = soapService.wsbpLSTENTITESALL(serviceCookie)
				
				if (result != null && result.getENTITES() != null)
				{
					ArrayOfENTITE arrayEntity = result.getENTITES()
					
					for (ENTITE currEntity in arrayEntity.getENTITE()) 
					{
						Map item=new HashMap()
						
						item.entityCode = currEntity.codeEntite
						item.entityName = currEntity.nomEntite
						
						resultList.add(item);
					}
					
					finalResult = [ "code" : "00" , "status" : "OK" , "data" : resultList ]
				}
				else {
					finalResult = [ "code" : "02" , "status" : "KO" , "description" : "No data found" , "data" : [] ]
				}
				
				
			}
			else if("getAgenciesByEntity".equals(q))
			{
				def entity = request.getParameter "entity"
				if (entity == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"code" : "99", "status" : "KO", "error" : "the parameter entity is missing"}""")
				}
				
				WSBPAGENCESENTITEResult result = soapService.wsbpAGENCESENTITE(serviceCookie, entity)
				
				if (result != null && result.getENTITEAGENCES() != null)
				{
					ArrayOfENTITEAGENCE arrayAgency = result.getENTITEAGENCES()
					
					for (ENTITEAGENCE currAgency in arrayAgency.getENTITEAGENCE()) 
					{
						Map item=new HashMap()
						
						item.agencyCode = currAgency.codeAgence
						item.agencyName = currAgency.nomAgence
						item.territotyCode = currAgency.codeTerritoire
						item.territoryName = currAgency.nomTerritoire
						
						resultList.add(item);
					}
					
					finalResult = [ "code" : "00" , "status" : "OK" , "data" : resultList ]
				}
				else {
					finalResult = [ "code" : "02" , "status" : "KO" , "description" : "No data found" , "data" : [] ]
				}
				
			}
			
		} catch(Exception e) {
			
			LOGGER.error("Error while getting entity/agency list : ", e)
			
			def err = [ "code" : "99", "status" : "K0", "error" : e.getMessage()]
			return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,new JsonBuilder(err).toPrettyString())
		}
		
        
        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(finalResult).toString())
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
