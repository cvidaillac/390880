package com.sgdbf.rest.api;

import java.util.List;
import java.util.Map;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ActivityStates;

import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

class Index implements RestApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def result=[];
		
		ProcessAPI processAPI=context.getApiClient().getProcessAPI();
		Long userId = context.getApiSession().getUserId();
		IdentityAPI identityApi = context.getApiClient().getIdentityAPI();
		
		String commande = request.getParameter("commande");
//		String taskId = request.getParameter("taskId");
//		String assigeeUserId = request.getParameter("assigeeUserId");
		 
		APISession apiSession = context.getApiSession();
		String[] filtersProcess = request.getParameterMap().get("f")
		Map<String, String[]> mapFiltersProcess = Utility.parsingFiltersParameters(filtersProcess);
		
		if(commande == null || commande.equals("SMARTETasks")) {
			result = Utility.getTaskSMART(context, userId, processAPI, identityApi, context.getApiSession(), mapFiltersProcess);
		} else if(commande.equals("BOTasks")){
			result = Utility.getTaskBO(context, userId, processAPI, identityApi, context.getApiSession(), mapFiltersProcess);
		} else if(commande.equals("SAVTasks")){
			result = Utility.getSAVTasks(context, userId, processAPI, identityApi, context.getApiSession(), mapFiltersProcess);
		}
		else if(commande.equals("LACTasks")){
			result = Utility.getTaskLAC(context, userId, processAPI, identityApi, context.getApiSession(), mapFiltersProcess);
		}
	 else if(commande.equals("patchBO")){
		result = Utility.getTaskBO_Patch(context, userId, processAPI, context.getApiSession(), mapFiltersProcess);
	}
	else if(commande.equals("patchLAC")){
		result = Utility.getTaskLAC_Patch(context, userId, processAPI, context.getApiSession(), mapFiltersProcess);
	}
		else if(commande.contains("assignTask")){
			
			String assigeeUserId = commande.split("AND")[1];
			String taskId = commande.split("AND")[2];
			
			result = Utility.AssignTaskToUser(Long.valueOf(assigeeUserId), Long.valueOf(taskId), processAPI);
		}
		


		// Send the result as a JSON representation
		// You may use buildPagedResponse if your result is multiple
		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toPrettyString())
	}

	private Map getDataFromHumanTask(String commande, HumanTaskInstance hu) {
		Map taskItem = new HashMap();
		parentProcessInstanceId = hu.getParentProcessInstanceId();
		pi = processAPI.getProcessInstance(parentProcessInstanceId);
		isValide = true;

		taskItem.processName = pi.name;
		taskItem.id = hu.id;
		taskItem.displayName = hu.displayName;
		taskItem.reachedStateDate = hu.reachedStateDate;
		taskItem.expectedEndDate = hu.expectedEndDate;
		taskItem.processId = pi.id;
		if (pi.stringIndex1 != null && pi.stringIndex1.size() >0){
			taskItem.searchIndex1 = pi.stringIndex1;
		}
		if (pi.stringIndex2 != null && pi.stringIndex2.size() >0){
			taskItem.searchIndex2 = pi.stringIndex2;
		}
		if (pi.stringIndex3 != null && pi.stringIndex3.size() >0){
			taskItem.searchIndex3 = pi.stringIndex3;
		}
		if (pi.stringIndex4 != null && pi.stringIndex4.size() >0){
			taskItem.searchIndex4 = pi.stringIndex4;
		}
		if (pi.stringIndex5 != null && pi.stringIndex5.size() >0){
			taskItem.searchIndex5 = pi.stringIndex5;
		}
		
	}
	
	
	
	private static boolean getFilterByName_ApproveGEEMCO(String getNameTask) {
		List<String> strings = new ArrayList<String>();
		
		strings.add("Recherche d'acquéreur");
		strings.add("Détermination du prix de vente");
		strings.add("Facturation par entité");
		strings.add("Validation de l'acquéreur");
		strings.add("Sortie du matériel");
		
		//return strings.contains(getNameTask);
		
		for(int i=0; i<strings.size();i++){
			String temp = strings.get(i);
			if(getNameTask.startsWith(temp)){
				return true;
			}
		}
		return false;
	}

	
	private static boolean getFilterByName_ApproveBO(String getNameTask) {
		List<String> strings = new ArrayList<String>();

		strings.add("Relogotage");
		strings.add("Réception du matériel");

		//return strings.contains(getNameTask);
		
				for(int i=0; i<strings.size();i++){
			String temp = strings.get(i);
			if(getNameTask.startsWith(temp)){
//				if(!(commande.equals("BOTasks")&&getNameTask.equalsIgnoreCase("Réception du matériel BO")))
				return true;
			}
		}
		return false;
	}
	
	private static boolean getFilterByName_ApproveBudgetProposition(String getNameTask) {
		List<String> strings = new ArrayList<String>();

		strings.add("Approve budget proposition");
		
		//return strings.contains(getNameTask);
		
		for(int i=0; i<strings.size();i++){
			String temp = strings.get(i);
			if(getNameTask.startsWith(temp)){
				return true;
			}
		}
		return false;
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

	boolean isOkWithFiltering(Map<String, String[]> mapFiltesProcess, Map taskItem) {
		boolean isValide = true;
		for (Map.Entry<String, String[]> entry : mapFiltesProcess.entrySet()) {
			if(taskItem.containsKey(entry.getKey()) && isValide) {
				String filterValue = entry.getValue()[0];
				filterValue = filterValue.toLowerCase()
				String filterOperator = entry.getValue()[1];
				String taskValue = taskItem.get(entry.getKey()).toString();
				taskValue = taskValue.toLowerCase()

				if (filterOperator.equals("E")){
					if (taskValue!= filterValue) {
						isValide = false;
					}
				} else {
					if (!taskValue.contains(filterValue)) {
						isValide = false;
					}
				}

				if(!isValide) {
					break;
				}

			}
		}
		return isValide;

	}


	void parsingFiltersParameters(Map mapFiltesProcess, String[] filtersProcess) {
		if (filtersProcess != null) {
			for(e in filtersProcess){
				String[] parts = e.split("=");
				String key = parts[0];
				String[] values = new String[2];
				values[0] = parts[1];
				if (parts.size() > 2){
					values[1] = parts[2];
				} else {
					values[1] = "E";
				}
				mapFiltesProcess.put(key, values);
			}
		}
	}

	/**
	 * Example : How to add a new Filter
	 * THIS METHOD IS NEVER CALLED, JUST AN EXAMPLE
	 * @param mapFiltesProcess
	 */
	private void addFilterBudgetPropositon(Map mapFiltesProcess){
		String[] init = new String[2];
		init[0] = "BudgetProposition"
		init[1] = "E";
		mapFiltesProcess.put("processName", init);
	}

}
