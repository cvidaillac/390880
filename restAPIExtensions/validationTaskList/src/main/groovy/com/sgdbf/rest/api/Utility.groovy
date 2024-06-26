package com.sgdbf.rest.api

import java.text.SimpleDateFormat
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.api.ProcessAPI
import com.sgdbf.model.Proposition
import com.sgdbf.model.PropositionDAO
import com.sgdbf.model.PropositionDAOImpl

class Utility {

	
	
	
	// CFR 14/12/2020 - Changed log class
	//private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")
	private static final Logger LOGGER = LoggerFactory.getLogger("com.sgdbf.groovy.restapi.validationTaskList");

	public static List getTaskPropositionList(String commande, RestAPIContext context, ProcessAPI processAPI, Map < String, String[] > mapFiltersProcess)throws Exception {
		/**  TENTATIVE AVEC SEARCH BUILDER : KO sur le refresh...
		 List<HumanTaskInstance> tasks=new ArrayList<HumanTaskInstance>();
		 SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 100);
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
		 String refPropositionDAO = "";
		 // Nécessaire pour la rétrocompatibilité
		 if (commande == null || commande.equals("ApproveBudgetProposition")) {
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, userId);
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Approve budget proposition");
		 refPropositionDAO = "proposition_ref";
		 } else if(commande.equals("ApproveDxxSteps")) {
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Recherche d'acquéreur");
		 searchBuilder.or();
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Détermination du prix de vente");
		 searchBuilder.or();
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Facturation par entité");
		 searchBuilder.or();
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Validation de l'acquéreur");
		 searchBuilder.or();
		 searchBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Sortie du matériel");
		 refPropositionDAO = "currentProposition_ref";
		 }
		 ////Only "Approve budget proposition" because other tasks are treated by the GetCustomTasks REST API
		 searchBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.ASC_NULLS_FIRST);
		 SearchResult<HumanTaskInstance> listPendingsAndAssigned = processAPI.searchHumanTaskInstances(searchBuilder.done());
		 tasks.addAll(listPendingsAndAssigned.getResult());**/

		String refPropositionDAO = "";
		
		List < HumanTaskInstance > tasks = new ArrayList < HumanTaskInstance > ();
		tasks.addAll(processAPI.getAssignedHumanTaskInstances(context.getApiSession().getUserId(), 0, Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC));
		tasks.addAll(processAPI.getPendingHumanTaskInstances(context.getApiSession().getUserId(), 0, Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC));
		PropositionDAO propositionDAO = new PropositionDAOImpl(context.getApiSession());
		def result = [];
		
		try {
			//Récuperation des taches afféctees a l'utilisateur connecte
			for (HumanTaskInstance hu: tasks) {
				// List of SMART Actions.
				if (commande == null || commande.equals("ApproveBudgetProposition")) {
					if (getFilterByName_ApproveBudgetProposition(hu.getName())) {
						refPropositionDAO = "currentProposition_ref";
						HashMap item = getResult(processAPI, hu, refPropositionDAO, propositionDAO, mapFiltersProcess);
						if (item != null && isOkWithFiltering(mapFiltersProcess, item)) {
							
							result.add(item);
						}
					}
				} else if(commande.equals("ApproveGEEMCO")) {
					if (getFilterByName_ApproveGEEMCO(hu.getName())) {
						refPropositionDAO = "currentProposition_ref";
						HashMap item = getResult(processAPI, hu, refPropositionDAO, propositionDAO, mapFiltersProcess);
						if (item != null && isOkWithFiltering(mapFiltersProcess, item)) {
							result.add(item);
						}
					}
				} else if(commande.equals("ApproveBO")) {
				if (getFilterByName_ApproveBO(hu.getName())) {
					refPropositionDAO = "currentProposition_ref";
					HashMap item = getResult(processAPI, hu, refPropositionDAO, propositionDAO, mapFiltersProcess);
					if (item != null && isOkWithFiltering(mapFiltersProcess, item)) {
					result.add(item);
				}
			}
		}

			}
			return result;
		} catch (Exception e) {
			LOGGER.error("Exception getTaskPropositionList : " + e.getMessage());
			throw e;
		}


	}
	
	private static boolean getFilterByName_ApproveGEEMCO(String getNameTask) {
		List<String> strings = new ArrayList<String>();
		
		strings.add("Recherche d'acquéreur");
		strings.add("Détermination du prix de vente");
		strings.add("Facturation par entité");
		strings.add("Validation de l'acquéreur");
		strings.add("Sortie du matériel");
		
		return strings.contains(getNameTask);
	}

	
	private static boolean getFilterByName_ApproveBO(String getNameTask) {
		List<String> strings = new ArrayList<String>();

//		strings.add("Relogotage");
		strings.add("Réception du matériel BO");

		return strings.contains(getNameTask);
	}
	

	
	private static boolean getFilterByName_ApproveBudgetProposition(String getNameTask) {
		List<String> strings = new ArrayList<String>();

		strings.add("Approve budget proposition");
		
		return strings.contains(getNameTask);
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
	
	
	
	private static HashMap getResult(ProcessAPI processAPI, HumanTaskInstance hu, String refPropositionDAO, PropositionDAO propositionDAO, Map < String, String[] > mapFiltersProcess) {
		
		//Pour chaque tache, recuperation de la proposition liee
		ProcessInstance processInstance = processAPI.getProcessInstance(hu.getParentProcessInstanceId());
		Map executionContext = processAPI.getProcessInstanceExecutionContext(hu.getParentProcessInstanceId());
		SimpleBusinessDataReference dataReference = executionContext.get(refPropositionDAO);
		Proposition proposition = propositionDAO.findByPersistenceId(dataReference.getStorageId());
		Map item = new HashMap();
		
		if(proposition != null) {
			item.agencyName = proposition.agencyName;
			
			//Données de la proposition
			item.agencyCode = proposition.getAgencyCode();
			
			// BPM-474 : On validation tasks list, the amount displayed is the actualRequestAmount when BudgetProposition has been approved
			// New field initialPropositionAmount added in order to also return the propsitionAmount
			item.initialPropositionAmount = proposition.getPropositionAmount();
			def Boolean is_budget_approved = proposition.isIsBudgetPropositionApproved();
			if ((is_budget_approved != null) && (is_budget_approved)) {
				item.amount = proposition.getActualRequestAmount();
				if (item.amount == null) {
					item.amount = item.initialPropositionAmount;
				}
			} else {
				item.amount = item.initialPropositionAmount;
			}
			
			item.budgetYear = proposition.getBudgetYear();
			
			if ((proposition.existing == "newMaterial")) {
				item.materialNumber = "Nouveau matériel - " + proposition.typeMaterialName
			} else {
				item.materialNumber = proposition.getMaterialNumber() + " - " +proposition.getTypeMaterialName();
			}
			
			item.niveauApprob = proposition.getNiveauApprob()
			item.propositionId = proposition.getPersistenceId();
			item.propositionStatus = proposition.getPropositionStatus()
			
			item.territoryName = proposition.getTerritoryCode() + " - " + proposition.getTerritoryName();
			item.territoryCode = proposition.getTerritoryCode();

			item.entiteName = proposition.getEntiteCode() + " - " + proposition.getEntiteName();
			item.entiteCode = proposition.getEntiteCode();
			
			item.sectorName = proposition.getSectorCode() + " - " + proposition.getSectorName();
			item.sectorCode = proposition.getSectorCode();
			
			item.siteName = proposition.getSiteCode() + " - " + proposition.getSiteName();
			item.siteCode = proposition.getSiteCode();
			
			item.agencyName = proposition.getAgencyCode() + " - " + proposition.getAgencyName();
			item.agencyCode = proposition.getAgencyCode();
			
			item.typeMaterial = proposition.getTypeMaterialName();
			item.typeProposition = proposition.getTypeProposition()
			item.typeWorkflow = proposition.getTypeWorkflow()
			item.typeWorkflowTargetAgency = proposition.getTypeWorkflowTargetAgency()
			
			//Données de la tache
			item.taskId = hu.getId();
			item.caseId = hu.getParentProcessInstanceId();
			item.rootCaseId = hu.getRootContainerId();
			
			item.dueDate = null;
			if(hu.getExpectedEndDate()) {
				item.dueDate = hu.getExpectedEndDate().getTime();
			}
			
			item.nameTask = hu.getName();
			
			
			long processDefinitionId = hu.getParentProcessInstanceId();
			
			ProcessInstance pi = processAPI.getProcessInstance(processDefinitionId);
			String isFirstWF = pi.getStringIndex1();
			
			if ("Transfert".equalsIgnoreCase(proposition.typeProposition) && isFirstWF && isFirstWF.equalsIgnoreCase("false")) {
				item.typeWorkflow = item.typeWorkflowTargetAgency;
				if(item.typeWorkflow.equalsIgnoreCase("dad")) {
					def predicat = "(T) ";
					item.territoryName = predicat + proposition.getTransferTerritoryCode() + " - " + proposition.getTransferTerritoryName();
					item.territoryCode = proposition.getTerritoryCode();
		
					item.entiteName = predicat + proposition.getTransferEntityCode() + " - " + proposition.getTransferEntityName();
					item.entiteCode = proposition.getTransferEntityCode();
					
					item.sectorName = predicat + proposition.getTransferSectorCode() + " - " + proposition.getTransferSectorName();
					item.sectorCode = proposition.getTransferSectorCode();
					
					item.siteName = predicat + proposition.getTransferSiteCode() + " - " + proposition.getTransferSiteName();
					item.siteCode = proposition.getTransferSiteCode();
					
					item.agencyName = predicat + proposition.getTransferAgencyCode() + " - " + proposition.getTransferAgencyName();
					item.agencyCode = proposition.getTransferAgencyCode();
		
					item.propositionStatus = proposition.propositionStatusTargetAgency;
					item.niveauApprob = proposition.niveauApprobTargetAgency;
				}
			}
			if ("A renouveler".equalsIgnoreCase(proposition.typeProposition) && isFirstWF && isFirstWF.equalsIgnoreCase("false")) {
				item.typeWorkflow = item.typeWorkflowTargetAgency;
				item.propositionStatus = proposition.propositionStatusTargetAgency;
				item.niveauApprob = proposition.niveauApprobTargetAgency;
			}
			
			return item
			
		} else {
			return null;
		}
	}
	
	public static Map < String,
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
