/**
 * 
 */
package com.sgdbf.pop3p.connector;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorException;

import com.vega_systems.ws.locprotns.DESTINATAIRE;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.WSBPCOPIEMAILResult;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class Pop3pGetMailCopyImpl extends AbstractPop3pGetMailCopyImpl {

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//Get access to the connector input parameters
		//getWsdlURL();
		//getCookie();
		//getCodeNoeud();
		//getCodeWorkflow();
		//getGenreMateriel();

		//Get access to the connector input parameters
		WSBPCOPIEMAILResult result;
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

		try {
			URL urlBudgePop = new URL(getWsdlURL());
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();

			result = soapService.wsbpCOPIEMAIL(getCookie(), getCodeNoeud(), getCodeWorkflow(), getGenreMateriel());

			//Traitement du resultat
			if (result != null && result.getDESTINATAIRES() != null){
				for (DESTINATAIRE currentDest : result.getDESTINATAIRES().getDESTINATAIRE()) {
					Map<String, String> item=new HashMap<String, String>();
					item.put("userSGID",currentDest.getSgidEnCopie());
					item.put("userMail",currentDest.getEmailEnCopie());

					resultList.add(item);
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(e);
		}
		//WARNING : Set the output of the connector execution. If outputs are not set, connector fails
		setResult(resultList);

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
