package com.sgdbf.rest.api

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.http.HttpStatus
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.vega_systems.ws.locprotns.ArrayOfEPLANN3
import com.vega_systems.ws.locprotns.ArrayOfPLANN3
import com.vega_systems.ws.locprotns.EPLANN3
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.PLANN3
import com.vega_systems.ws.locprotns.PLANNING
import com.vega_systems.ws.locprotns.WSBPENREGPLANNING3Result

class UpdatePlanning implements RestApiController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")
	
		@Override
		public RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
	
					
			StringBuilder buffer = new StringBuilder()
			BufferedReader reader = request.getReader()
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
	
			Map body=new JsonSlurper().parseText(buffer.toString())
			
			
			if (body==null || body.isEmpty()){
				return buildResponse(responseBuilder, HttpStatus.SC_BAD_REQUEST, "POST payload not found");
			}
			
			WSBPENREGPLANNING3Result result
			
			try {
				
				Properties props = loadProperties("configuration.properties", context.resourceProvider)
				String urlWSDL = props["urlWSDL"]
				String serviceCookie = props["serviceCookie"]
				
				URL urlBudgePop = new URL(urlWSDL);
				LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop)
				LocproPortType soapService = budgePopService.getLocproPort()
	
				ArrayOfEPLANN3 plannings = new ArrayOfEPLANN3()
				
				def planning = new HashMap();
				
				for (def i = 0; i< body.budget.size(); i++) 
				{
					planning = body.budget.get(i)
	
					EPLANN3 planningLevel = new EPLANN3()
					
					planningLevel.setCodeNoeud(planning.codeNoeud)
	
					if (planning.datePresentation){
						planningLevel.setDatePresentation(planning.datePresentation.substring(0, 10));
					}else {
						planningLevel.setDatePresentation(null);
					}
	
					if (planning.enveloppeBudget){
						planningLevel.setEnveloppeBudget(""+planning.enveloppeBudget)
					}
					
					if (planning.enveloppeCamions){
						planningLevel.setEnveloppeCamions(""+planning.enveloppeCamions)
					}
					
					if (planning.enveloppeChariots){
						planningLevel.setEnveloppeChariots(""+planning.enveloppeChariots)
					}
					
					if (planning.enveloppeAccessoires){
						planningLevel.setEnveloppeAccessoires(""+planning.enveloppeAccessoires)
					}
										
					if (planning.rf1) {
						planningLevel.setRf1(planning.rf1.substring(0, 10))
					}
						
					if (planning.rf2) {
						planningLevel.setRf2(planning.rf2.substring(0, 10))
					}

					if (planning.rf3) {
						planningLevel.setRf3(planning.rf3.substring(0, 10))
					}
					
					if (planning.rf4) {
						planningLevel.setRf4(planning.rf4.substring(0, 10))
					}
					
	
					plannings.getEPLANN3().add(planningLevel)
				}
				
				result = soapService.wsbpENREGPLANNING3(
										serviceCookie,
										""+body.anneeBudget,
										plannings
										)
	
			} catch (Exception e) {
				LOGGER.error("Error updating budget planning :  " + e.getMessage(), e)
				return buildResponse(responseBuilder, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception:  " + e.stackTrace.toString());
			}
	
			return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString());
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
