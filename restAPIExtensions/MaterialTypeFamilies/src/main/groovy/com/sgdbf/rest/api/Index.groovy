package com.sgdbf.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.vega_systems.ws.locprotns.ArrayOfGENRE
import com.vega_systems.ws.locprotns.GENRE
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.WSBPGENRESFAMILLESResult






import groovy.json.JsonBuilder

class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
		
		def finalResult = null
		List resultList = new ArrayList()
		Map typeObj = new HashMap()
		Map typeLabel = new TreeMap()
		
		Properties props = loadProperties("configuration.properties", context.resourceProvider)
		String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
		
		try
		{
			URL urlBudgePop = new URL(urlWSDL)
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop)
			
			LocproPortType soapService = budgePopService.getLocproPort()
			
			WSBPGENRESFAMILLESResult result = soapService.wsbpGENRESFAMILLES(serviceCookie)
			
			if(result != null && result.getGENRES() != null)
			{
				ArrayOfGENRE arrayGenre = result.getGENRES()
				
				for (GENRE currGenre in arrayGenre.getGENRE())
				{
					typeLabel.put(currGenre.genre, currGenre.libelleGenre)
					
					List typeList = null
					
					if(typeObj.containsKey(currGenre.genre))
					{
						typeList = typeObj.get(currGenre.genre)
						
						Map<String, String> itemGenre = new TreeMap<String, String>(Collections.reverseOrder())
						
						itemGenre.put("key", currGenre.sousGenre)
						itemGenre.put("label", currGenre.libelleSousGenre)
						itemGenre.put("famille", currGenre.famille)
						
						typeList.add(itemGenre)
					}
					else 
					{
						typeList = new ArrayList()
						
						Map<String, String> itemGenre = new TreeMap<String, String>(Collections.reverseOrder())
						
						itemGenre.put("key", currGenre.sousGenre)
						itemGenre.put("label", currGenre.libelleSousGenre)
						itemGenre.put("famille", currGenre.famille)
						
						typeList.add(itemGenre)
					}
					
					typeObj.put(currGenre.genre, typeList)
					
				}
				
				for (Map.Entry<String,String> entry : typeLabel.entrySet())
				{
					Map genreFamily = new TreeMap()
					
					genreFamily.put("key", entry.getKey())
					genreFamily.put("label", entry.getValue())
					genreFamily.put("ssG", typeObj.get(entry.getKey()))
					
					resultList.add(genreFamily)
				}
				
				finalResult = [ "code" : "00" , "status" : "OK" , "data" : resultList]
				
			} else {
				finalResult = [ "code" : "02" , "status" : "KO" , "description" : "No data found" , "data" : [] ]
			}
			
			
		} catch(Exception e) {
			
			LOGGER.error("Error while getting material type list : ", e)
			
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
