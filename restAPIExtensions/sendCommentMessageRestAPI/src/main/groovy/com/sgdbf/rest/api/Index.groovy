package com.sgdbf.rest.api;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.expression.Expression
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchOptionsBuilder
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

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		ProcessAPI processAPI=context.getApiClient().getProcessAPI();
		
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}

		String body = buffer.toString();

		def bodyMap=new JsonSlurper().parseText(body);
		// Retrieve comment parameter
		def comment =bodyMap.comment
		if (comment == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter comment is missing"}""")
		}

		def propositionId =bodyMap.propositionId
		if (propositionId == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter propositionId is missing"}""")
		}

		def senderId =bodyMap.senderId
		if (senderId == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter senderId is missing"}""")
		}

		def processDefinitionId =bodyMap.processDefinitionId
		if (processDefinitionId == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter processDefinitionId is missing"}""")
		}

		// Retrieve caseId parameter
		def caseId = bodyMap.caseId
		if (caseId == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter caseId is missing"}""")
		}
		
		try {
			// Retrieve the targetedProcessName
			String targetProcessName = processAPI.getProcessDeploymentInfo(Long.valueOf(processDefinitionId)).getDisplayName();
			Expression targetProcess = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
			Expression targetEvent = new ExpressionBuilder().createConstantStringExpression("MessageAddComment");

			//CORRELATION KEY
			Map<Expression,Expression> correlations=new HashMap<Expression,Expression>();
			Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("caseId");
			Expression correlationValue = new ExpressionBuilder().createConstantLongExpression(Long.valueOf(caseId));
			correlations.put(correlationKey, correlationValue);
			//DATA TO SEND
			Map<Expression,Expression> dataToSend=new HashMap<Expression,Expression>();
			Expression dataKey = new ExpressionBuilder().createConstantStringExpression("propositionId");
			Expression dataValue = new ExpressionBuilder().createConstantStringExpression(propositionId);
			dataToSend.put(dataKey, dataValue);
			Expression dataKey2 = new ExpressionBuilder().createConstantStringExpression("comment");
			Expression dataValue2 = new ExpressionBuilder().createConstantStringExpression(comment);
			dataToSend.put(dataKey2, dataValue2);
			Expression dataKey3 = new ExpressionBuilder().createConstantStringExpression("senderId");
			Expression dataValue3 = new ExpressionBuilder().createConstantStringExpression(senderId);
			dataToSend.put(dataKey3, dataValue3);

			processAPI.sendMessage("msgUpdateProposition", targetProcess, targetEvent, dataToSend, correlations);
			def result= "Message saved"
			return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString())
		} catch (Exception e) {
			LOGGER.error("An unexpected error occured : "+ e.getMessage());
			e.printStackTrace()
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
