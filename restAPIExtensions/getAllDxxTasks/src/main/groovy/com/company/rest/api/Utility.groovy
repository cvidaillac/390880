package com.company.rest.api;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import com.bonitasoft.engine.api.ProcessAPI;
import com.sgdbf.model.Proposition;
import com.sgdbf.model.PropositionDAO;
import com.sgdbf.model.PropositionDAOImpl;

class Utility {
	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

		public static List getTaskPropositionList(Long userId, ProcessAPI processAPI, APISession session, Map < String, String[] > mapFiltersProcess)throws Exception {

		List < String > tasksNames = new ArrayList < String > ();
		tasksNames.add("Recherche d'acquéreur");
		tasksNames.add("Détermination du prix de vente");
		tasksNames.add("Facturation par entité");
		tasksNames.add("Validation de l'acquéreur");
		tasksNames.add("Sortie du matériel");
		
		List < HumanTaskInstance > tasks = new ArrayList < HumanTaskInstance > ();
		tasks.addAll(processAPI.getAssignedHumanTaskInstances(userId, 0, Integer.MAX_VALUE, ActivityInstanceCriterion.DEFAULT));
		tasks.addAll(processAPI.getPendingHumanTaskInstances(userId, 0, Integer.MAX_VALUE, ActivityInstanceCriterion.DEFAULT));
		PropositionDAO propositionDAO = new PropositionDAOImpl(session);
		def result = [];

		//Recuperation des taches affectees a l'utilisateur connecte
		for (HumanTaskInstance hu: tasks) {
			if (tasksNames.contains(hu.getName())) {
				//Pour chaque tache, recuperation de la proposition liee
				ProcessInstance processInstance = processAPI.getProcessInstance(hu.getParentProcessInstanceId());
				Map executionContext = processAPI.getProcessInstanceExecutionContext(hu.getParentProcessInstanceId());
				SimpleBusinessDataReference dataReference = executionContext.get("currentProposition_ref");
				Proposition proposition = propositionDAO.findByPersistenceId(dataReference.getStorageId());
				Map item = new HashMap();
				//Données de la proposition
				item.propositionId = proposition.getPersistenceId();
				item.typeMaterial = proposition.getTypeMaterial();
				item.agencyCode = proposition.getAgencyCode();
				item.agencyName = proposition.getAgencyName();
				item.siteCode = proposition.getSiteCode();
				item.siteName = proposition.getSiteName();
				item.sectorCode = proposition.getSectorCode();
				item.sectorName = proposition.getSectorName();
				item.territoryCode = proposition.getTerritoryCode();
				item.territoryName = proposition.getTerritoryName();
				item.amount = proposition.getPropositionAmount();
				item.budgetYear = proposition.getBudgetYear();
				item.materialNumber = proposition.getMaterialNumber()
				item.typeProposition = proposition.getTypeProposition()
				item.propositionStatus = proposition.getPropositionStatus()

				//Données de la tache
				item.taskId = hu.getId();
				item.caseId = hu.getParentProcessInstanceId();
				item.rootCaseId = hu.getRootContainerId();
				item.dueDate = hu.getExpectedEndDate();
				item.nameTask = hu.getName();
				
					if (isOkWithFiltering(mapFiltersProcess, item)) {
						result.add(item);
					}

			}

		}
		return result;
	}

		
	public static validateTask(ProcessAPI processAPI, long userId, String taskId) {
		def result = {};
		processAPI.assignUserTask(taskId, userId);
		processAPI.executeUserTask(taskId, new HashMap());

		return result;
 		
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

	public static Map < String, String[] > parsingFiltersParameters(String[]filtersProcess) {
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