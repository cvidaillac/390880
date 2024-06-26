package com.sgdbf.pop3p.connector;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bonitasoft.engine.connector.ConnectorException;

import com.vega_systems.ws.locprotns.APPROBATION;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.SGIDAPPROB;
import com.vega_systems.ws.locprotns.WSBPHIERARCHIEResult;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class Pop3pGetHierarchyImpl extends AbstractPop3pGetHierarchyImpl {

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//Get access to the connector input parameters
		WSBPHIERARCHIEResult result;
		TreeMap<String,Map<String, Object>> treeMap = new TreeMap<String,Map<String, Object>>();

		try {
			URL urlBudgePop = new URL(getWsdlURL());
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();

			result = soapService.wsbpHIERARCHIE(getCookie(), getCodeWorkflow(), getFamilleMat(), getCodeNoeudMat(), getMontantProposition());

			//Gestion des erreurs fonctionnelles :
			if (result.getERRORNUM() != null && !result.getERRORNUM().isEmpty()
					&& result.getERRORMSG() != null && !result.getERRORMSG().isEmpty()){
				//Une erreur est retournée pour permettre aux administrateurs de rejouer après modification dans LYRE
				String errorMessage = "Le connecteur POP3P wsbpHIERARCHIE a rencontré l'erreur suivante : "
						+ result.getERRORNUM() + " - " + result.getERRORMSG();
				throw new ConnectorException(errorMessage);
			}


			if (result.getAPPROBATIONS() != null){
				List<APPROBATION> approbationsList = result.getAPPROBATIONS().getAPPROBATION();
				for (int i = 0; i < approbationsList.size(); i++) {
					Map<String, Object> item=new HashMap<String, Object>();
					APPROBATION currentApprob = approbationsList.get(i);
					item.put("SGidSmarte",currentApprob.getSGidSmarte());
					item.put("codeNoeudApprob",currentApprob.getCodeNoeudApprob());

					item.put("libNoeudApprob",currentApprob.getLibNoeudApprob());
					item.put("nomNoeudApprob",currentApprob.getNomNoeudApprob());

					if (currentApprob.getSGIDAPPROBS()!= null && !currentApprob.getSGIDAPPROBS().getSGIDAPPROB().isEmpty()){
						List<SGIDAPPROB> listApprobs = currentApprob.getSGIDAPPROBS().getSGIDAPPROB();
						ArrayList<String> arrayApprobs = new ArrayList<String>();
						for (int j = 0; j < listApprobs.size(); j++) {
							arrayApprobs.add(listApprobs.get(j).getSgidApprob());
						}
						item.put("listApprobs",arrayApprobs);
					}

					treeMap.put(currentApprob.getNiveauApprob(), item);
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
