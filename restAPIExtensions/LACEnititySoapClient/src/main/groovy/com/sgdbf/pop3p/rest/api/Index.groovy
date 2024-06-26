package com.sgdbf.pop3p.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.vega_systems.ws.locprotns.ArrayOfENTITE
import com.vega_systems.ws.locprotns.ENTITE; 
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.WSBPLSTENTITESLACResult;

import groovy.json.JsonBuilder


class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
        // Retrieve sgid parameter
        def sgid = request.getParameter "sgid"
        if (sgid == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter sgid is missing"}""")
        }

		def resultList=[];
        
        Properties props = loadProperties("configuration.properties", context.resourceProvider)
        String urlWSDL = props["urlWSDL"]
		String serviceCookie = props["serviceCookie"]
		
		URL urlBudgePop = new URL(urlWSDL);
		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
		WSBPLSTENTITESLACResult result = budgePopService.getLocproPort().wsbpLSTENTITESLAC(serviceCookie, sgid);

		if (result != null && result.getENTITES() != null){
			ArrayOfENTITE list_entites = result.getENTITES()
			for (ENTITE currentEntity : list_entites.getENTITE()) {
				
				Map item=new HashMap()
				item.entite = currentEntity.getCodeEntite()+" - "+currentEntity.getNomEntite();
				resultList.add(item)
				
			}
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
