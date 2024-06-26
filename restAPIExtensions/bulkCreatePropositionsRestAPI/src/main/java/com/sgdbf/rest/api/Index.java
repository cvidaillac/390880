package com.sgdbf.rest.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.dto.Result.ResultBuilder;
import com.sgdbf.rest.api.exception.ValidationException;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.rest.RestAPIContext;
import org.bonitasoft.engine.bpm.contract.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Controller class
 */
public class Index extends AbstractIndex {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getName());
    
    private static final String PROCESS_NAME_BUDGET_PROPOSITION_INIT = "POP3P_BudgetProposition_Init";
    private static final String POST_JSON_FIELD_PROCESS_DEF_ID = "processDefinitionId";
    private static final String POST_JSON_FIELD_PROCESS_CONTRACTS = "processContracts";
    private Long processDefinitionId;
    private JSONArray processInstantiationContracts;
    private List<String> processStartError;

    protected ContractTypeConverter typeConverterUtil = new ContractTypeConverter(ContractTypeConverter.ISO_8601_DATE_PATTERNS);
    
    public class StartProcessException extends Exception {
    	StartProcessException(String msg, Exception e) {
    		
    	}
    };
    
    /**
     * Ensure request is valid
     *
     * @param request the HttpRequest
     */
    @Override
    public void validateInputParameters(HttpServletRequest request) throws ValidationException  {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values

    	try {
	    	// Get POST data
	    	readPostData(request);
    	} catch(IOException e) {
    		throw new ValidationException("Echec de la récupération des données du processus à lancer : " + e.getMessage());
    	}
    }
    
    private void readPostData(HttpServletRequest request) throws IOException {
    	StringBuffer jb = new StringBuffer();
    	String line = null;
    	
		// Read post data
    	try {
    		BufferedReader reader = request.getReader();
    		while ((line = reader.readLine()) != null) {
  		      jb.append(line);    			
    		}    		
    	} catch (Exception e) { 
    		LOGGER.error("readPostData Exception: " + e.toString());
    		throw e;
    	}
    	
    	// Retrieve processDefinitionId and process parameters
    	try {
    		String json_str = jb.toString();
    		LOGGER.debug("readPostData: JSON = " + json_str);
    	    JSONObject jsonObject =  new JSONObject(json_str);
    	    String process_def_id_str = jsonObject.getString(POST_JSON_FIELD_PROCESS_DEF_ID);
    	    LOGGER.debug("readPostData: processDefinitionId = " + process_def_id_str);
    	    processDefinitionId = Long.valueOf(process_def_id_str);
    	    processInstantiationContracts = jsonObject.getJSONArray(POST_JSON_FIELD_PROCESS_CONTRACTS);
    	    
    	} catch (JSONException e) {
    	    // crash and burn
    		LOGGER.error("readPostData JSONException: " + e.toString());
    	    throw new IOException("Error parsing JSON request string : " + e.getMessage(), e);
    	}

    }
    
    /**
     * Execute business logic
     *
     * @param context
     * @return Result
     */
    @Override
    protected Result execute(RestAPIContext context) throws ValidationException, StartProcessException {

        // Get the current user id
        APISession session_api = context.getApiSession();
		long bonita_user_id = session_api.getUserId();
        
        // Start processes with Process API
        ProcessAPI process_api = context.getApiClient().getProcessAPI();
        Result start_result = startProcesses(process_api, bonita_user_id);              

        // Prepare the result
        return start_result ;
    }

    private Serializable getSerializableObject(Object one_value) {
    	Serializable res = null;
    	
    	try {
    		if (one_value != null) {
	   	    	if (one_value instanceof Serializable) {
		    		res = (Serializable) one_value;
				
				} else if (one_value instanceof JSONObject) {
					res = getInstantiationInputs((JSONObject) one_value);
				
				} else if (one_value instanceof JSONArray)  {
					JSONArray array_value = (JSONArray) one_value;
					int nb_values = array_value.length();
					ArrayList<Serializable> values = new ArrayList<Serializable>();
					for (int i=0; i<nb_values; i++) {
						Object one_array_entry = array_value.get(i);
						values.add(getSerializableObject(one_array_entry));
					}
					res = values;
		    		LOGGER.debug("getInstantiationInputs: Serialized Array with " + String.valueOf(nb_values) + " entries");
	
				} else {
					LOGGER.warn("getInstantiationInputs : unexpected Object type for : " + one_value.toString() + " with class " + one_value.getClass().getName());
				}
    		}
    	} catch(Exception e) {
    		LOGGER.error("getSerializableObject Exception: " + e.toString());
    	}
    	return res;
    }
    
    private void addObjectValue(String one_field, Object one_value, HashMap<String, Serializable> instantiation_inputs) {
    	LOGGER.debug("Get seerializable value for field " + one_field);
    	Serializable serializable_value = getSerializableObject(one_value);
    	instantiation_inputs.put(one_field, (Serializable) serializable_value);
    }
    
    private HashMap<String, Serializable> getInstantiationInputs(JSONObject one_contract) {
    	HashMap<String, Serializable> instantiation_inputs = new HashMap<String, Serializable>();
    	
    	try {
    		//Map<String,Object> result = one_contract.toMap();
    		Set<String> fields_set = one_contract.keySet();
    		for (String one_field : fields_set) {
    			Object one_value = one_contract.get(one_field);
    			addObjectValue(one_field, one_value, instantiation_inputs);
    		}
    		LOGGER.debug("getInstantiationInputs: Serialized object with " + String.valueOf(instantiation_inputs.size()) + " fields");
    		
    	} catch(Exception e) {
    		LOGGER.error("getInstantiationInputs Exception: " + e.toString());
    		throw new ValidationException("Echec de la récupération des données du contrat : " + e.getMessage());
    	}
    	
    	return instantiation_inputs;
    }
    
    private Long startProcess(ProcessAPI process_api, final ContractDefinition processContract, HashMap<String, Serializable> instantiationInputs) {
    	Long case_id = null;
    	String error_msg = null;
    	
    	try {    	    
    	    LOGGER.debug("startProcess: Converting types in contract");
    	    final Map<String, Serializable> processedInputs = typeConverterUtil.getProcessedInput(processContract, instantiationInputs);

    	    LOGGER.debug("startProcess: Starting process");
    	    ProcessInstance one_case = process_api.startProcessWithInputs(processDefinitionId, processedInputs);
        	case_id = one_case.getId();
            LOGGER.debug(format("Case created with id :  %d",  case_id));
            
    	} catch (ProcessDefinitionNotFoundException e) {
    		error_msg = "Processus non trouvé, exception: " + e.getMessage();
    		LOGGER.error("startProcesses : Process id [" + String.valueOf(processDefinitionId) + "] non trouvé");
		} catch (ContractViolationException e) {
			error_msg = "Contrat non respecté pour le démarrage du processus, exception: " + e.getMessage();
			String cause_str = (e.getCause() == null) ? "" : " - Cause: " + e.getCause().toString(); 
    		LOGGER.error("startProcesses : Contract violated: " + e.toString() + cause_str);			
		} catch(Exception e) {
			error_msg = "Echec du démarrage du processus, exception: " + e.getMessage();
			LOGGER.error("startProcesses exception: " + e.toString());
		}
    	
    	if (error_msg != null) {
    		processStartError.add(error_msg);
    	}
    	return case_id;
    }
    
	private Result startProcesses(ProcessAPI process_api, long bonita_user_id) throws ValidationException, StartProcessException {
        // Check that process definition id match the expected process name
		final ContractDefinition processContract;
		
		try {
	        ProcessDefinition process_def = process_api.getProcessDefinition(processDefinitionId);
	        String process_name = process_def.getName();
	        if (! PROCESS_NAME_BUDGET_PROPOSITION_INIT.equalsIgnoreCase(process_name)) {
	    		LOGGER.error("startProcesses : Process name [" + process_name + "] does not match the expected one [" + PROCESS_NAME_BUDGET_PROPOSITION_INIT + "]");
	    		throw new ValidationException("Le processus " + process_name + " n'est pas autorisé");	        	
	        }
			LOGGER.debug("startProcess: Getting ontract for process");
		    processContract = process_api.getProcessContract(processDefinitionId);

		} catch (ProcessDefinitionNotFoundException e) {
    		LOGGER.error("startProcesses : Process id [" + String.valueOf(processDefinitionId) + "] non trouvé");
    		throw new StartProcessException("Processus " + String.valueOf(processDefinitionId) + " non trouvé", e);	        				
		}
		
		// Start each process
        int nb_processes = processInstantiationContracts.length();
        List<Long> cases_list = new ArrayList<Long>();
        processStartError = new ArrayList<String>();
        for (int i=0; i<nb_processes ; i++) {
        	// Get the contract
        	JSONObject one_contract = processInstantiationContracts.getJSONObject(i);
        	HashMap<String, Serializable> instantiationInputs = getInstantiationInputs(one_contract);
            
        	// Start the process
        	LOGGER.debug("Starting process " + String.valueOf(i));
        	Long case_id = startProcess(process_api, processContract, instantiationInputs);
        	cases_list.add(case_id);
        }
        ResultBuilder result_builder = Result.builder();
        result_builder.setCasesFromList(cases_list);
        result_builder.setErrorsFromList(processStartError);
        return result_builder.build();

	}
}