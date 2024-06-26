package com.sgdbf.rest.api;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

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
/* CFR 09/01/2023 - Migration 2022.1 : Remove Jersey libraries
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.config.DefaultClientConfig
*/
import com.vega_systems.ws.locprotns.AGENCE
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.TERRITOIRE
import com.vega_systems.ws.locprotns.ENTITE
import com.vega_systems.ws.locprotns.WSBPAGENCESTERRResult
import com.vega_systems.ws.locprotns.WSBPLSTTERRITOIRESResult

/* CFR 09/01/2023 - Migration 2022.1 : Remove library
import javax.ws.rs.core.MediaType
*/

class Index implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {

		def method = request.getParameter "method"
		if (method == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter method is missing"}""")
		}
		 
		Properties props = loadProperties("configuration.properties", context.resourceProvider)
				
		String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
			
		List<Map> resultList = new ArrayList<Map>();
		try {

			URL urlBudgePop = new URL(urlWSDL);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			
			LocproPortType soapService = budgePopService.getLocproPort()
			
			String userSGID=context.getApiSession().getUserName()
						
			if ("getTerritories".equalsIgnoreCase(method))
			{
				
				WSBPLSTTERRITOIRESResult result = soapService.wsbpLSTTERRITOIRES(serviceCookie, userSGID)
				
				if (result != null && result.getTERRITOIRES() != null)
				{
					for (TERRITOIRE currentTerritory in result.getTERRITOIRES().getTERRITOIRE()) 
					{
						Map item=new HashMap();
						
						item.territoryID = currentTerritory.territoryId
						item.territoryName = currentTerritory.territoryName

						resultList.add(item);
					}
				}
				else if(! result.getERRORMSG().isEmpty()) {
					def err = [ "message" : result.getERRORMSG() ]
					return buildResponse(responseBuilder, HttpServletResponse.SC_OK,new JsonBuilder(err).toPrettyString())
				}
				
				
			}
			else if ("getTerritoryAgencies".equalsIgnoreCase(method))
			{
				
				def territoryId = request.getParameter "territoryId"
				if (territoryId == null) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter territoryId is missing"}""")
				}
				
				WSBPAGENCESTERRResult result = soapService.wsbpAGENCESTERR(serviceCookie, territoryId)
				
				if (result != null && result.getAGENCES() != null)
				{
					for (AGENCE currentAgency in result.getAGENCES().getAGENCE()) {
						Map item=new HashMap();
						
						item.agencyLabel = currentAgency.agencyName + " - " + currentAgency.agencyCode
						item.agencyCode = currentAgency.agencyCode
						item.agencyName = currentAgency.agencyName
						item.entityCode = currentAgency.entityCode
						item.entityName = currentAgency.entityName

						resultList.add(item);
					}
				}
				else if(! result.getERRORMSG().isEmpty()) {
					def err = [ "message" : result.getERRORMSG() ]
					return buildResponse(responseBuilder, HttpServletResponse.SC_OK,new JsonBuilder(err).toPrettyString())
				}
				
					
			}else {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "this method is not allowed"}""")
			}

		} catch (Exception e) {
			LOGGER.error("Erreur pendant l'appel au web service Lyre : " + org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e));
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, new JsonBuilder(e.getMessage()).toPrettyString())
		}
		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(resultList).toPrettyString())

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
