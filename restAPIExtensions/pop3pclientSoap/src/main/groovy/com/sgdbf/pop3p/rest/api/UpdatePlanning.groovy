package com.sgdbf.pop3p.rest.api;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.util.Map
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime;
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.http.HttpStatus
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController

// BPM-521 : For new library LyreWebServices-2.3.0.jar, changed package name from com.vega_systems.ws.locprotn to com.vega_systems.ws.locprotns_dec20
import com.vega_systems.ws.locprotns.ArrayOfPLANNING
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.PLANNING
import com.vega_systems.ws.locprotns.WSBPENREGPLANNINGResult

class UpdatePlanning implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

	@Override
	public RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {

		Properties props = loadProperties("configuration.properties", context.resourceProvider)
		String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
		WSBPENREGPLANNINGResult result;
		
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}

		Map body=new JsonSlurper().parseText(buffer.toString());
		
		
		if (body==null || body.isEmpty()){
			return buildResponse(responseBuilder, HttpStatus.SC_BAD_REQUEST, "POST payload not found");
		}
		try {
			
			URL urlBudgePop = new URL(urlWSDL);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();

			ArrayOfPLANNING plannings = new ArrayOfPLANNING();
			
			def planning = new HashMap();
			
			for (def i = 0; i< body.budget.size(); i++) {
				planning = body.budget.get(i);

				PLANNING planningLevel = new PLANNING();
				planningLevel.setCodeNoeud(planning.codeNoeud);

				if (planning.datePresentation){
					planningLevel.setDatePresentation(planning.datePresentation.substring(0, 10));
				}else {
					planningLevel.setDatePresentation(null);
				}

				if (planning.enveloppeBudget){
					planningLevel.setEnveloppeBudget(""+planning.enveloppeBudget);
				}	
				
				if(planning.dates) {
					if (planning.dates.rf1) {
						planningLevel.setRf1(planning.dates.rf1.substring(0, 10));
					}
						
					if (planning.dates.rf2) {
						planningLevel.setRf2(planning.dates.rf2.substring(0, 10));
					} 

					if (planning.dates.rf3) {
						planningLevel.setRf3(planning.dates.rf3.substring(0, 10));
					}
					
					if (planning.dates.rf4) {
						planningLevel.setRf4(planning.dates.rf4.substring(0, 10));
					}
				} 

				plannings.getPLANNING().add(planningLevel);
			}
			
			result = soapService.wsbpENREGPLANNING(serviceCookie, ""+body.anneeBudget, plannings);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'enregistrement du planning budgétaire :  " + e.getMessage(), e)
			return buildResponse(responseBuilder, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception:  " + e.stackTrace.toString());
		}

		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString());
	}

	
	private PLANNING setRF(PLANNING planning, Map dates) {
		//Gestion des dates RF
		
		if (dates.rf1){
			planning.setRf1(dates.rf1.substring(0, 10))
		}else {
			planning.setRf1(null)
		}
		if (dates.rf2){
			planning.setRf2(dates.rf2.substring(0, 10))
		}else {
			planning.setRf2(null);
		}
		if (dates.rf3){
			planning.setRf3(dates.rf3.substring(0, 10))
		}else {
			planning.setRf3(null);
		}
		if (dates.rf4){
			planning.setRf4(dates.rf4.substring(0, 10))
		}else {
			planning.setRf4(null);
		}
		
		return planning;
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
