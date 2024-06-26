package com.sgdbf.rest.api


import groovy.json.JsonBuilder

import groovy.json.JsonSlurper


//import java.util.logging.Logger


import javax.servlet.http.HttpServletRequest

import javax.servlet.http.HttpServletResponse


import org.apache.http.HttpStatus

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance

import org.bonitasoft.web.extension.ResourceProvider

import org.bonitasoft.web.extension.rest.RestApiResponse

import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import org.slf4j.Logger

import org.slf4j.LoggerFactory


import com.bonitasoft.engine.api.ProcessAPI

import com.bonitasoft.web.extension.rest.RestAPIContext

import com.bonitasoft.web.extension.rest.RestApiController


class ExecuteTask implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft");

	
	@Override

	public RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {

		StringBuilder buffer = new StringBuilder();

		BufferedReader reader = request.getReader();

		String line;

		while ((line = reader.readLine()) != null) {

			buffer.append(line);

		}

		Map body=new JsonSlurper().parseText(buffer.toString());


		String[] filtersProcess = request.getParameterMap().get("f")

		Map<String, String[]> mapFiltersProcess = Utility.parsingFiltersParameters(filtersProcess);


		def taskId=request.getParameter("taskId");

		def listIds=request.getParameter("listIds");

		def commande = request.getParameter("commande");

		

		if ((taskId==null || taskId.isEmpty()) &&(listIds==null || listIds.isEmpty())){

			return buildResponse(responseBuilder, HttpStatus.SC_BAD_REQUEST, "Parameters taskId or listIds not found");

		}

		ProcessAPI processAPI=context.getApiClient().getProcessAPI();

		try {

			if (taskId != null){

				
				HumanTaskInstance hu=processAPI.getHumanTaskInstance(Long.parseLong(taskId));

				processAPI.assignUserTask(Long.parseLong(taskId), context.getApiSession().getUserId());

				processAPI.executeUserTask(Long.parseLong(taskId), body);

			}else if (listIds !=null){

				String[] ids = listIds.split(",")

				for (currentId in ids) {

					HumanTaskInstance hu=processAPI.getHumanTaskInstance(Long.parseLong(currentId));

					processAPI.assignUserTask(Long.parseLong(currentId), context.getApiSession().getUserId());

					processAPI.executeUserTask(Long.parseLong(currentId), body);

	
				}

			}


			def result=Utility.getTaskPropositionList(commande, context, processAPI, mapFiltersProcess);

			return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString());

		} catch (Exception e) {

			return buildResponse(responseBuilder, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception:  " + e.stackTrace.toString());

		}


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
