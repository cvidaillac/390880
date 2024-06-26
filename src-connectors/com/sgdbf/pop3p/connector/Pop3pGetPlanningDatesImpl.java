/**
 * 
 */
package com.sgdbf.pop3p.connector;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bonitasoft.engine.connector.ConnectorException;

import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.PROPBUDGET;
import com.vega_systems.ws.locprotns.WSBPPLANNINGResult;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class Pop3pGetPlanningDatesImpl extends
AbstractPop3pGetPlanningDatesImpl {

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//Get access to the connector input parameters
		//;
		//getCookie();
		//getAnneeBudget();
		//getCodeAgence();

		//Get access to the connector input parameters
		WSBPPLANNINGResult result;
		TreeMap<String,Map<String, Object>> treeMap = new TreeMap<String,Map<String, Object>>();

		try {
			URL urlBudgePop = new URL(getWsdlUrl());
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();

			result = soapService.wsbpPLANNING(getCookie(), getAnneeBudget(), getCodeAgence());


			if (result.getPROPBUDGETS() != null){
				List<PROPBUDGET> planningDatesList = result.getPROPBUDGETS().getPROPBUDGET();
				for (int i = 0; i < planningDatesList.size(); i++) {
					Map<String, Object> item=new HashMap<String, Object>();
					PROPBUDGET currentApprob = planningDatesList.get(i);
					item.put("niveau",currentApprob.getNiveau());
					item.put("intitule",currentApprob.getIntitule());
					item.put("codeNoeud",currentApprob.getCodeNoeud());
					item.put("nomNoeud",currentApprob.getNomNoeud());
					if(currentApprob.getDatePresentation() != null){
						item.put("datePresentation",currentApprob.getDatePresentation());
					}
					if(currentApprob.getRf1() != null){
						item.put("rf1",currentApprob.getRf1());
					}
					if(currentApprob.getRf2() != null){
						item.put("rf2",currentApprob.getRf2());
					}
					if(currentApprob.getRf3() != null){
						item.put("rf3",currentApprob.getRf3());
					}
					if(currentApprob.getRf4() != null){
						item.put("rf4",currentApprob.getRf4());
					}

					treeMap.put(currentApprob.getNiveau(), item);
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(e);
		}
		//WARNING : Set the output of the connector execution. If outputs are not set, connector fails
		setResult(new ArrayList<>(treeMap.values()));

	}

	@Override
	public void connect() throws ConnectorException{
		//[Optional] Open a connection to remote server

	}

	@Override
	public void disconnect() throws ConnectorException{
		//[Optional] Close connection to remote server

	}

}
