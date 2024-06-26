package com.sgdbf.rest.api;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.sgdbf.model.Proposition
import com.sgdbf.model.PropositionDAO
import com.sgdbf.model.PropositionDAOImpl

class Index implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		// To retrieve query parameters use the request.getParameter(..) method.
		// Be careful, parameter values are always returned as String values

		// Retrieve type parameter
		def type = request.getParameter "type"
		Boolean calculateSum = false;
		if (type == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter type is missing"}""")
		}
		if ("count".equalsIgnoreCase(type)){
			calculateSum = false;
		}else if ("sum".equalsIgnoreCase(type)){
			calculateSum = true;
		}else {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "this type is not allowed"}""")
		}


		// Retrieve budgetYear parameter
		def budgetYear = request.getParameter "budgetYear"
		if (budgetYear == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter budgetYear is missing"}""")
		}
		Integer budgetYearAsInt = Integer.parseInt(budgetYear);

		PropositionDAO propositionDAO=new PropositionDAOImpl(context.apiSession);
		ArrayList<Proposition> listPropositions =propositionDAO.findByBudgetYear(budgetYearAsInt, 0, Integer.MAX_VALUE);
		def resultListProp=[];
		def counts=[];
		HashMap territoryMap = new HashMap();

		def result = new HashMap();
		def nbDraft= 0;
		def nbValSmarte= 0;
		def nbValAgency = 0;
		def nbValSite = 0;
		def nbValSector = 0;
		def nbValTerritory  = 0;
		def nbRejected =0;


		for (currentProp in listPropositions){
			def item = new HashMap();
			item.persistenceId = currentProp.persistenceId
			item.budgetYear = currentProp.budgetYear
			item.territoryCode = currentProp.territoryName
			item.sectorCode = currentProp.sectorName
			item.siteCode = currentProp.siteName
			item.agencyCode = currentProp.agencyName
			item.propositionAmount = currentProp.propositionAmount
			item.typeProposition = currentProp.typeProposition
			item.propositionStatus = currentProp.propositionStatus

			if (territoryMap.get(currentProp.territoryName)==null){
				def numbers = new HashMap();
				numbers.nbValSmarte = 0;
				numbers.nbValAgency = 0;
				numbers.nbValSite = 0;
				numbers.nbValSector = 0;
				numbers.nbValTerritory = 0;
				if (!calculateSum){
					numbers.nbDraft = 0;
					numbers.nbRejected = 0;
				}
				territoryMap.put(currentProp.territoryName, numbers)
			}


			if ("Draft".equalsIgnoreCase(currentProp.propositionStatus)){
				if (!calculateSum){
					//On ne recupere que le nombre de Draft, pas le montant
					territoryMap.get(currentProp.territoryName).nbDraft++;
				}
			}
			if ("Validated by SMARTE".equalsIgnoreCase(currentProp.propositionStatus)){
				if (calculateSum){
					territoryMap.get(currentProp.territoryName).nbValSmarte+= currentProp.propositionAmount;
				}else{
					territoryMap.get(currentProp.territoryName).nbValSmarte++;
				}
			}
			if ("Validated agency".equalsIgnoreCase(currentProp.propositionStatus)){
				if (calculateSum){
					territoryMap.get(currentProp.territoryName).nbValAgency+= currentProp.propositionAmount;
				}else{
					territoryMap.get(currentProp.territoryName).nbValAgency++;
				}
			}
			if ("Validated site".equalsIgnoreCase(currentProp.propositionStatus)){
				if (calculateSum){
					territoryMap.get(currentProp.territoryName).nbValSite+= currentProp.propositionAmount;
				}else{
					territoryMap.get(currentProp.territoryName).nbValSite++;
				}
			}
			if ("Validated sector".equalsIgnoreCase(currentProp.propositionStatus)){
				if (calculateSum){
					territoryMap.get(currentProp.territoryName).nbValSector+= currentProp.propositionAmount;
				}else{
					territoryMap.get(currentProp.territoryName).nbValSector++;
				}
			}
			if ("Validated region".equalsIgnoreCase(currentProp.propositionStatus)){
				if (calculateSum){
					territoryMap.get(currentProp.territoryName).nbValTerritory+= currentProp.propositionAmount;
				}else{
					territoryMap.get(currentProp.territoryName).nbValTerritory++;
				}
			}
			if ("Rejected".equalsIgnoreCase(currentProp.propositionStatus)){
				if (!calculateSum){
					//On ne recupere que le nombre de Rejected, pas le montant
					territoryMap.get(currentProp.territoryName).nbRejected++;
				}
			}

			resultListProp.add(item)
		}

		result.listPropositions = resultListProp;
		counts.add(territoryMap)
		result.counts = counts;

		// Send the result as a JSON representation
		// You may use buildPagedResponse if your result is multiple
		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString())
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
