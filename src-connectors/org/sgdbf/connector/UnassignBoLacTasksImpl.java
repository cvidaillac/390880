/**
 * 
 */
package org.sgdbf.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

/**
*The connector execution will follow the steps
* 1 - setInputParameters() --> the connector receives input parameters values
* 2 - validateInputParameters() --> the connector can validate input parameters values
* 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
* 4 - executeBusinessLogic() --> execute the connector
* 5 - getOutputParameters() --> output are retrieved from connector
* 6 - disconnect() --> the connector can close connection to remote server (if any)
*/
public class UnassignBoLacTasksImpl extends AbstractUnassignBoLacTasksImpl {
	
	Logger logger= Logger.getLogger("org.bonitasoft");

	@Override
	protected void executeBusinessLogic() throws ConnectorException
	{
		ProcessAPI processApi = apiAccessor.getProcessAPI();
		
		int countBoTasks = 0, countLacTasks = 0;
		
		try
		{
			SearchOptionsBuilder humanTaskSearchBuilder = new SearchOptionsBuilder(0, 1000000);
			humanTaskSearchBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME,"ready");
			
			SearchResult<HumanTaskInstance> listPendingsAndAssignedTasks = apiAccessor.getProcessAPI().searchAssignedAndPendingHumanTasks(humanTaskSearchBuilder.done());
			for (HumanTaskInstance taskInstance : listPendingsAndAssignedTasks.getResult()) 
			{
				ProcessInstance pi = processApi.getProcessInstance(taskInstance.getParentProcessInstanceId());
				
				if(taskInstance.getAssigneeId() != -1)
				{	
					if(isBOTask(taskInstance.getDisplayName(), pi.getStringIndex2().toString()))
					{
						processApi.assignUserTask(taskInstance.getId(), -1);
						countBoTasks ++;
					}
					else if(isLACTask(taskInstance.getDisplayName(), pi.getStringIndex2().toString()))
					{
						processApi.assignUserTask(taskInstance.getId(), -1);
						countLacTasks ++;
					}
				}
				
			}
			
						
		} catch (SearchException | ProcessInstanceNotFoundException | UpdateException e) {
			throw new ConnectorException("UnassignBoLacTasksImpl.executeBusinessLogic() Exception:"+e.getMessage());
		}
	
		setCountBOTasks(countBoTasks);
		setCountLACTasks(countLacTasks);
		
	
	 }
	
	
	private boolean isBOTask(String taskName, String idprop)
	{
		List<String> strings = new ArrayList<String>();

		strings.add("DAD a prendre en charge BO "+"-"+idprop);
		strings.add("DAD a prendre en charge "+"-"+idprop);
		strings.add("DAD a prendre en charge"+"-"+idprop);
		strings.add("DAD a prendre en charge");
		strings.add("ValidateCommandes");
		strings.add("Confirmation date de livraison "+"-"+idprop);
		strings.add("Réception matériel "+"-"+idprop);
		strings.add("[BO] Confirmation date de livraison "+"-"+idprop);
		strings.add("[BO] Réception matériel "+"-"+idprop);
		strings.add("Relogotage "+"-"+idprop);
		
		strings.add(idprop+" - "+"DAD a prendre en charge BO");
		strings.add(idprop+" - "+"DAD a prendre en charge");
		strings.add(idprop+" - "+"[BO] Confirmation date de livraison");
		strings.add(idprop+" - "+"[BO] Réception matériel");
		
		strings.add(idprop+" - "+"Relogotage");
		
		return strings.contains(taskName);
	}
	
	private boolean isLACTask(String taskName, String idprop)
	{
		List<String> strings = new ArrayList<String>();

		strings.add("DAD a prendre en charge LAC "+"-"+idprop);
		strings.add("[LAC] Confirmation date de livraison "+"-"+idprop);
		strings.add("[LAC] Réception matériel "+"-"+idprop);
		
		strings.add(idprop+" - "+"DAD a prendre en charge LAC");
		strings.add(idprop+" - "+"[LAC] Confirmation date de livraison");
		strings.add(idprop+" - "+"[LAC] Réception matériel");
		
	
		return strings.contains(taskName);
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
