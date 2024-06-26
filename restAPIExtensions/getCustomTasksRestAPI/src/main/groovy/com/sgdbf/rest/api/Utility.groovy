package com.sgdbf.rest.api;


//import org.bonitasoft.engine.api.Logger
//import java.lang.System.Logger

import java.text.Collator

import org.apache.commons.lang3.StringUtils
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference
import org.bonitasoft.engine.identity.UserNotFoundException
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestAPIContext;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.api.IdentityAPI
import com.sgdbf.model.Proposition
import com.sgdbf.model.PropositionDAO
import com.sgdbf.model.PropositionDAOImpl



class Utility {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class)

	
	/**
	 * Tasks des SMARTES
	 */
	public static List getTaskSMART(RestAPIContext context, Long userId, ProcessAPI processAPI, IdentityAPI identityApi, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		//Recuperation des taches affectees a l'utilisateur connecte
		List<HumanTaskInstance> tasks=new ArrayList<HumanTaskInstance>();

		SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 1000000);

		searchBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		searchBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
		searchBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.ASC_NULLS_FIRST);

		//Remove "Approve budget proposition" because they are only treated by the ValidationTaskList REST API
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "Approve budget proposition")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "DAD a prendre en charge BO")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "DAD a prendre en charge LAC")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "Relogotage")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "Confirmation date de livraison")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "Réception matériel")
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "DADR à soumettre") 
		searchBuilder.differentFrom(HumanTaskInstanceSearchDescriptor.NAME, "Sortie du matériel")
		
		SearchResult<HumanTaskInstance> listPendingsAndAssigned = processAPI.searchHumanTaskInstances(searchBuilder.done());

		tasks.addAll(listPendingsAndAssigned.getResult());
		def result=[];


		//Pour chaque tache, recuperation des SearchIndex associés au Case
		Long parentProcessInstanceId;
		ProcessInstance pi;
		boolean isValide;
		for (HumanTaskInstance hu:tasks){
			Map taskItem = new HashMap();
			
			taskItem = getHumanTask(context, hu, processAPI, identityApi);
			
			if (!getFilterByName_TasksSmart(hu.displayName, taskItem.get("searchIndex2").toString())) {
			isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
		
			if (isValide) {
				//processAPI.assignUserTask(hu.getId(), userId);
				result.add(taskItem);
			}
			}
		}
		return result;
		
	}
	
	public static List getSAVTasks(RestAPIContext context, Long userId, ProcessAPI processAPI, IdentityAPI identityApi, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		List<HumanTaskInstance> tasks=new ArrayList<HumanTaskInstance>();

		SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 1000000);

		//searchBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		searchBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
		//searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Infomation request");
		//searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "DADR à soumettre");
		
		searchBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.ASC_NULLS_FIRST);
		
		SearchResult<HumanTaskInstance> listPendingsAndAssigned = processAPI.searchHumanTaskInstances(searchBuilder.done());

		tasks.addAll(listPendingsAndAssigned.getResult());
		def result=[];


		try {
			Long parentProcessInstanceId;
			ProcessInstance pi;
			boolean isValide;
	
			for (HumanTaskInstance hu:tasks){
					Map taskItem = new HashMap();
					taskItem = getHumanTask(context, hu, processAPI, identityApi);
					if (getFilterByName_TasksSAV(hu.displayName, taskItem.get("searchIndex2").toString())) {
						isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
	
						if (isValide) {
							// Pas de processAPI.assignUserTask car cette fonction récupère la 
							// liste des tâches assignées
							result.add(taskItem);
						}
					}
			}
			
		} catch (Exception e) {
			throw e;
		}
	
		return result;

	}
	
	
	
	public static String AssignTaskToUser(Long userId, Long taskId, ProcessAPI processAPI) {
		
		try {
		processAPI.assignUserTask(taskId, userId);
		return "ok";
			
		} catch (Exception e) {
			return e.toString();
		}
		
	}
	
	public static List getTaskBO_Patch(RestAPIContext context, Long userId, ProcessAPI processAPI, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		SearchOptionsBuilder searchBuilder1 = new SearchOptionsBuilder(0, 1000000);
		//searchBuilder1.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned1 = processAPI.searchHumanTaskInstances(searchBuilder1.done());
		
		
//		SearchOptionsBuilder searchBuilder2 = new SearchOptionsBuilder(0, 1000000);
//		searchBuilder2.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, -1);
//		SearchResult<HumanTaskInstance> listPendingsAndAssigned2 = processAPI.searchHumanTaskInstances(searchBuilder2.done());
//		
//		SearchOptionsBuilder searchBuilder3 = new SearchOptionsBuilder(0, 1000000);
//		searchBuilder3.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
//		SearchResult<HumanTaskInstance> listPendingsAndAssigned3 = processAPI.searchHumanTaskInstances(searchBuilder3.done());
		
		//List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult()+listPendingsAndAssigned2.getResult()+listPendingsAndAssigned3.getResult();
		List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult();
		PropositionDAO propositionDAO = new PropositionDAOImpl(session);
		def result = [];
		
		try {
			Long parentProcessInstanceId;
			ProcessInstance pi;
			boolean isValide;

			for (HumanTaskInstance hu:listPendingsAndAssigned){
			 // if(hu.getAssigneeId()!=""){
				  Map taskItem = new HashMap();
				 taskItem = getHumanTask(context, hu, processAPI);
			
					if (getFilterByName_TasksBO(hu.displayName,taskItem.get("searchIndex2").toString())) {

						isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
							// Assigne la tâche si la personne cherche les tâches pour le BO.
							processAPI.assignUserTask(hu.getId(), -1);
							result.add(taskItem);
						
					}
				
			//}
			}
		} catch (Exception e) {
			throw e;
		}
	
		return result;
		
	}
	
	
	public static List getTaskBO(RestAPIContext context, Long userId, ProcessAPI processAPI, IdentityAPI identityApi, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		SearchOptionsBuilder searchBuilder1 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder1.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned1 = processAPI.searchHumanTaskInstances(searchBuilder1.done());
		
		
		SearchOptionsBuilder searchBuilder2 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder2.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, -1);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned2 = processAPI.searchHumanTaskInstances(searchBuilder2.done());
		
		SearchOptionsBuilder searchBuilder3 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder3.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned3 = processAPI.searchHumanTaskInstances(searchBuilder3.done());
		
		List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult()+listPendingsAndAssigned2.getResult()+listPendingsAndAssigned3.getResult();
		
		PropositionDAO propositionDAO = new PropositionDAOImpl(session);
		def result = [];
		
		try {
			Long parentProcessInstanceId;
			ProcessInstance pi;
			boolean isValide;

			for (HumanTaskInstance hu:listPendingsAndAssigned){
             // if(hu.getAssigneeId()!=""){
//				LOGGER.info("Tasks List for BO "+hu.displayName.toString())
	 			 Map taskItem = new HashMap();
//				  if(!hu.displayName.split("-")[1].trim().equalsIgnoreCase("Réception du matériel BO"))
//					  {
				 taskItem = getHumanTask(context, hu, processAPI, identityApi);	
//				 LOGGER.info(hu.getDisplayName().toString())	
					 	
						  if (getFilterByName_TasksBO(hu.displayName,taskItem.get("searchIndex2").toString())) 
							  {
//								  if(!hu.getDisplayName().equalsIgnoreCase("Réception du matériel BO"))
//								{
									isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
								
						def isreceptionBO= hu.displayName.toString().equals("Reception du materiel BO")
						final Collator instance = Collator.getInstance();
						
							// This strategy mean it'll ignore the accents
							
						String a="Reception du materiel BO";
						String b=hu.getDisplayName().toString()
							// Will print 0 because its EQUAL
						instance.setStrength(Collator.PRIMARY);
						instance.setDecomposition(java.text.Collator.CANONICAL_DECOMPOSITION);
//							def compareWithAccents=instance.equals(a,b)
						
						def displayName=StringUtils.stripAccents(hu.getDisplayName().toString().trim())
//						def compareWithAccents=displayName.toString().trim()=="Reception du materiel BO"
//							LOGGER.info(compareWithAccents.toString()+"________");
					LOGGER.info(displayName.toString().toLowerCase().trim().equals("reception du materiel bo").toString()+"____POP______"+displayName.toString().toLowerCase().trim())
							// Assigne la tâche si la personne cherche les tâches pour le BO.
							//processAPI.assignUserTask(0, 0)nUserTask(hu.getId(), -1);
						if(!(displayName.toString().toLowerCase().trim().contains("reception du materiel bo")))
						{			  if(isValide )
						{
							result.add(taskItem);
						}}
					}
					
//					  }
				
			//}
			}
		} catch (Exception e) {
			throw e;
		}
	
		return result;
		
	}
	
	public static List getTaskLAC(RestAPIContext context, Long userId, ProcessAPI processAPI, IdentityAPI identityApi, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		SearchOptionsBuilder searchBuilder1 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder1.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned1 = processAPI.searchHumanTaskInstances(searchBuilder1.done());
		
		
		SearchOptionsBuilder searchBuilder2 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder2.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, -1);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned2 = processAPI.searchHumanTaskInstances(searchBuilder2.done());
		
		SearchOptionsBuilder searchBuilder3 = new SearchOptionsBuilder(0, 1000000);
		searchBuilder3.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned3 = processAPI.searchHumanTaskInstances(searchBuilder3.done());
		
		List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult()+listPendingsAndAssigned2.getResult()+listPendingsAndAssigned3.getResult();
		
		
		PropositionDAO propositionDAO = new PropositionDAOImpl(session);
		def result = [];
		
		try {
			Long parentProcessInstanceId;
			ProcessInstance pi;
			boolean isValide;

			for (HumanTaskInstance hu:listPendingsAndAssigned){
				// if(hu.getAssigneeId()!=""){
	             Map taskItem = new HashMap();
				 taskItem = getHumanTask(context, hu, processAPI, identityApi);				

					
					if (getFilterByName_TasksLAC(hu.displayName,taskItem.get("searchIndex2").toString())) {

						isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
					
							// Assigne la tâche si la personne cherche les tâches pour le BO.
							//processAPI.assignUserTask(hu.getId(), -1);
						if(isValide)
						{
							result.add(taskItem);
						}
					
				}
			//}
		}
		} catch (Exception e) {
			throw e;
		}
	
		return result;
		
	}
	

	public static List getTaskLAC_Patch(RestAPIContext context, Long userId, ProcessAPI processAPI, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {
		SearchOptionsBuilder searchBuilder1 = new SearchOptionsBuilder(0, 1000000);
	//	searchBuilder1.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		SearchResult<HumanTaskInstance> listPendingsAndAssigned1 = processAPI.searchHumanTaskInstances(searchBuilder1.done());
		
//		
//		SearchOptionsBuilder searchBuilder2 = new SearchOptionsBuilder(0, 1000000);
//		searchBuilder2.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, -1);
//		SearchResult<HumanTaskInstance> listPendingsAndAssigned2 = processAPI.searchHumanTaskInstances(searchBuilder2.done());
//		
//		SearchOptionsBuilder searchBuilder3 = new SearchOptionsBuilder(0, 1000000);
//		searchBuilder3.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
//		SearchResult<HumanTaskInstance> listPendingsAndAssigned3 = processAPI.searchHumanTaskInstances(searchBuilder3.done());
//		
//		List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult()+listPendingsAndAssigned2.getResult()+listPendingsAndAssigned3.getResult();
//		
		List<HumanTaskInstance> listPendingsAndAssigned = listPendingsAndAssigned1.getResult();
		PropositionDAO propositionDAO = new PropositionDAOImpl(session);
		def result = [];
		
		try {
			Long parentProcessInstanceId;
			ProcessInstance pi;
			boolean isValide;

			for (HumanTaskInstance hu:listPendingsAndAssigned){
				// if(hu.getAssigneeId()!=""){
				 Map taskItem = new HashMap();
				 taskItem = getHumanTask(context, hu, processAPI);

					
					if (getFilterByName_TasksLAC(hu.displayName,taskItem.get("searchIndex2").toString())) {

						isValide = isOkWithFiltering(mapFiltersProcess, taskItem);
					
							// Assigne la tâche si la personne cherche les tâches pour le BO.
							processAPI.assignUserTask(hu.getId(), -1);
							result.add(taskItem);
					
					
				}
			//}
		}
		} catch (Exception e) {
			throw e;
		}
	
		return result;
		
	}
	
	private static boolean getFilterByName_TasksSAV(String getNameTask, String idprop) {
		// CFR 30/10/2020 UAT Lot2 Sprint : avoid crashing task list because of a task with null display name
		if (getNameTask == null) {
			return false;
		} else {
			List<String> strings = new ArrayList<String>();
			
			strings.add("DADR à soumettre");
			strings.add(idprop+" - "+"DADR à soumettre");
			//strings.add("Infomation request");
			strings.add(idprop+" - "+"Demande de compléments pour la demande de réparation");
			strings.add("Demande de compléments pour la demande de réparation "+idprop);
			
			for(int i=0; i<strings.size();i++){
				String temp = strings.get(i);
				if(getNameTask.startsWith(temp)){
					return true;
				}
			}
			return false;
		}
		
	}
	
	private static boolean getFilterByName_TasksSmart(String getNameTask, String idprop) {
		// CFR 30/10/2020 UAT Lot2 Sprint : avoid crashing task list because of a task with null display name
		if (getNameTask == null) {
			return false;
		} else {
	
			List<String> strings = new ArrayList<String>();
			
			strings.add("Demande de compléments pour la demande de réparation "+idprop);
			
			//return strings.contains(getNameTask);
			for(int i=0; i<strings.size();i++){
				String temp = strings.get(i);
				if(getNameTask.startsWith(temp)){
					return true;
				}
			}
			return false;
		}
		
	}
	private static boolean getFilterByName_TasksBO(String getNameTask, String idprop) {
		// CFR 30/10/2020 UAT Lot2 Sprint : avoid crashing task list because of a task with null display name
		if (getNameTask == null) {
			return false;
			
		} else {
			// CFR 17/11/2020 - BPM-459 : New tasks, simplify conditions
			// CFR 02/12/2020 - UAT Sprint2 : Filter out SMARTE tasks with BO or LAC
			if ((getNameTask.contains(" BO") || getNameTask.contains("[BO]"))
				&& (! getNameTask.contains("A transmettre au")) && (! getNameTask.contains("A compléter pour"))) {
				return true;
			} else {
				List<String> strings = new ArrayList<String>();
				
				// CFR 17/11/2020 - BPM-459 : Commented all task name with BO
				//strings.add("DAD a prendre en charge BO "+"-"+idprop);
				strings.add("DAD a prendre en charge "+"-"+idprop);
				strings.add("DAD a prendre en charge"+"-"+idprop);
				strings.add("DAD a prendre en charge");
				strings.add("ValidateCommandes");
				strings.add("Confirmation date de livraison "+"-"+idprop);
				strings.add("Réception matériel "+"-"+idprop);
				//strings.add("[BO] Confirmation date de livraison "+"-"+idprop);
				//strings.add("[BO] Réception matériel "+"-"+idprop);
				strings.add("Relogotage "+"-"+idprop);
				
				//strings.add(idprop+" - "+"DAD a prendre en charge BO");
				strings.add(idprop+" - "+"DAD a prendre en charge");
				//strings.add(idprop+" - "+"[BO] Confirmation date de livraison");
				//strings.add(idprop+" - "+"[BO] Réception matériel");
				
				//strings.add(idprop+" - "+"Prolongation de contrat BO");
				
				strings.add(idprop+" - "+"Relogotage");
				
				strings.add(idprop+" - "+"Confirmation date de livraison");
				
				// CFR 17/11/2020 - BPM-459 : WAS:
				// return (strings.contains(getNameTask) || getNameTask.contains("[BO] Confirmation date de livraison n°") || getNameTask.contains("[BO] Réception matériel n°"));
				return strings.contains(getNameTask);
			}
		}
	}
	
	private static boolean getFilterByName_TasksLAC(String getNameTask, String idprop) {
		// CFR 30/10/2020 UAT Lot2 Sprint : avoid crashing task list because of a task with null display name
		if (getNameTask == null) {
			return false;
		} else {
			// CFR 17/11/2020 - BPM-459 : New LAC tasks, simplify conditions
			 /* WAS: 
			List<String> strings = new ArrayList<String>();
	
			strings.add("DAD a prendre en charge LAC "+"-"+idprop);
			strings.add("[LAC] Confirmation date de livraison "+"-"+idprop);
			strings.add("[LAC] Réception matériel "+"-"+idprop);
			
			strings.add(idprop+" - "+"DAD a prendre en charge LAC");
			strings.add(idprop+" - "+"[LAC] Confirmation date de livraison");
			strings.add(idprop+" - "+"[LAC] Réception matériel");
			
			strings.add(idprop+" - "+"Prolongation de contrat LAC");
			
			strings.add(idprop+" - "+"Confirmation date de livraison");
			
			return (strings.contains(getNameTask) || getNameTask.contains("[LAC] Confirmation date de livraison n°") || getNameTask.contains("[LAC] Réception matériel n°"));
			*/
			
			// CFR 02/12/2020 - UAT Sprint2 : Filter out SMARTE tasks with BO or LAC
			return (((getNameTask.contains(" LAC") || getNameTask.contains("[LAC]")) 				
					&& (! getNameTask.contains("A transmettre au")) && (! getNameTask.contains("A compléter pour")))
					|| getNameTask.endsWith(" - Confirmation date de livraison"));
		}
	}
	
	
	
	private static boolean isOkWithFiltering(Map < String, String[] > mapFiltersProcess, Map taskItem) {
		boolean isValide = true;
		for (Map.Entry < String, String[] > entry: mapFiltersProcess.entrySet()) {
			if (taskItem.containsKey(entry.getKey()) && isValide) {
				String filterValue = entry.getValue()[0];
				filterValue = filterValue.toLowerCase()
				String filterOperator = entry.getValue()[1];
				String taskValue = taskItem.get(entry.getKey()).toString();
				taskValue = taskValue.toLowerCase()

				if (filterOperator.equals("E")) {
					if (taskValue != filterValue) {
						isValide = false;
					}
				} 
				else if(filterOperator.equals("S"))
				{
					if (!taskValue.startsWith(filterValue)) {
						isValide = false;
					}
				} else {
					if (!taskValue.contains(filterValue)) {
						isValide = false;
					}
				}
				if (!isValide) {
					break;
				}

			}
		}
		return isValide;

	}
	
	
	private static Map getHumanTask(RestAPIContext context, HumanTaskInstance hu, ProcessAPI processAPI, IdentityAPI identityApi) {
	
		Map taskItem = new HashMap();
		Long parentProcessInstanceId = hu.getParentProcessInstanceId();
		ProcessInstance pi = processAPI.getProcessInstance(parentProcessInstanceId);
		
		taskItem.expectedEndDate = null;
		if(hu.expectedEndDate) {
			taskItem.expectedEndDate = hu.expectedEndDate.getTime();
		}

		
		taskItem.processName = pi.name;
		taskItem.id = hu.id;
		
		//taskItem.displayName = hu.displayName.split("-")[0];
		
		if(hu.displayName!=null) 
		{
			try 
			{
				long pid = Long.parseLong(hu.displayName.split("-")[0].trim());
				String taskName = hu.displayName.split("-")[1].trim();
				
				
				 if(taskName.contains("[BO]"))
				{
					taskItem.displayName = taskName.substring(5, taskName.length());
				}
				else if(taskName.contains("[LAC]"))
				{
					taskItem.displayName = taskName.substring(6, taskName.length());
				}
				else {
					taskItem.displayName = taskName;
				}
				
			} catch(NumberFormatException e){
				taskItem.displayName = hu.displayName.split("-")[0].trim();
				
			}
		}
		else {
			taskItem.displayName = "";
		}
		 
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
		
		if("Infomation request".equals(hu.getName())) {
			taskItem.searchIndex1 = getIndex1FromProposition(context, processAPI, hu);
		}
		
		try {
			taskItem.assigneeName = identityApi.getUser(hu.assigneeId).getFirstName() + " " + identityApi.getUser(hu.assigneeId).getLastName();
			
		} catch(UserNotFoundException e)
		{
			taskItem.assigneeName = "";
		}
		
		return taskItem;
		
	}
	
	// Pour la tâche Infomation request le champs searchIndex1 est utilisé pour déterminer si c'est le
	// première Workflow d'aprobation DDD ou DAD.
	public static String getIndex1FromProposition(RestAPIContext context, ProcessAPI processAPI, HumanTaskInstance hu) {
		def searchIndex1 = "";
		def refPropositionDAO = "currentProposition_ref";
		PropositionDAO propositionDAO = new PropositionDAOImpl(context.getApiSession());
		
		ProcessInstance processInstance = processAPI.getProcessInstance(hu.getParentProcessInstanceId());
		Map executionContext = processAPI.getProcessInstanceExecutionContext(hu.getParentProcessInstanceId());
		SimpleBusinessDataReference dataReference = executionContext.get(refPropositionDAO);
		
		Proposition proposition = propositionDAO.findByPersistenceId(dataReference.getStorageId());
		if (proposition.materialNumber && proposition.materialNumber.length()>0){
			searchIndex1 = proposition.materialNumber  + " - " + proposition.typeMaterialName
		}else {
			searchIndex1 = "Nouveau materiel - " + proposition.typeMaterialName
		}
		return searchIndex1;
		
	}
	
	private static Map < String,
	String[] > parsingFiltersParameters(String[]filtersProcess) {
		Map < String,
				String[] > mapFiltersProcess = new HashMap < String,
				String[] > ();

		if (filtersProcess != null) {
			for (e in filtersProcess) {
				String[]parts = e.split("=");
				String key = parts[0];
				String[]values = new String[2];
				values[0] = parts[1];
				if (parts.size() > 2) {
					values[1] = parts[2];
				} else {
					values[1] = "E";
				}
				mapFiltersProcess.put(key, values);
			}
		}
		return mapFiltersProcess;
	}

	
}