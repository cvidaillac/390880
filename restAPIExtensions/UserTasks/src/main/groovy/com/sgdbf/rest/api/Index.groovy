package com.sgdbf.rest.api;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.swing.text.StyledEditorKit.ForegroundAction

import org.apache.http.HttpHeaders
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values

        // Retrieve id parameter
        def id = request.getParameter "id"
        if (id == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter id is missing"}""")
        }
		
		def tasklist = []
		def result = null
		
		try
		{
			
			ProcessAPI processApi = context.getApiClient().getProcessAPI()
			IdentityAPI idApi = context.getApiClient().getIdentityAPI()
	
	        SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 1000000)
			
			searchBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, Long.parseLong(id))
			searchBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE)
			searchBuilder.sort(HumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, Order.ASC);
			
			
			List<HumanTaskInstance> listPendingsAndAssigned = processApi.searchAssignedAndPendingHumanTasks(searchBuilder.done()).getResult()
			
			for (HumanTaskInstance taskInstance :listPendingsAndAssigned) 
			{
				Map taskItem = new HashMap()
				
				ProcessInstance pi = processApi.getProcessInstance(taskInstance.getParentProcessInstanceId())
				
				taskItem.put("propositionId", pi.getStringIndex2())
				taskItem.put("caseId", taskInstance.getParentProcessInstanceId())
				taskItem.put("taskId", taskInstance.getId())
				
				if(taskInstance.getDisplayName() != null)
				{
					try
					{
						long pid = Long.parseLong(taskInstance.getDisplayName().split("-")[0].trim())
						taskItem.put("taskName", taskInstance.getDisplayName().split("-")[1].trim())
						
					} catch(NumberFormatException e){
						taskItem.put("taskName", taskInstance.getDisplayName().split("-")[0].trim())
					}
				}
				else {
					taskItem.put("taskName", taskInstance.getName())
				}
				
				taskItem.put("taskDate", taskInstance.getReachedStateDate())
				
				tasklist.add(taskItem)
			}
			
	        // Prepare the result
			if(tasklist.size() > 0)
			{
				result = [ "code" : "00" , "status" : "OK" , "data" : tasklist]
			}
			else {
				result = [ "code" : "02" , "status" : "KO" , "description" : "No data found" , "data" : tasklist ]
			}
		} 
		catch(Exception e)
		{
			
			def err = [ "code" : "99", "status" : "K0", "error" : e.getMessage()]
			return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,new JsonBuilder(err).toPrettyString())
		}

        // Send the result as a JSON representation
        // You may use buildPagedResponse if your result is multiple
        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
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
