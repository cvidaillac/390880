package com.sgdbf.rest.api;

import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.exception.ValidationException;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.PPREV;
import com.vega_systems.ws.locprotns.SYNTHESE;
import com.vega_systems.ws.locprotns.WSBPPLANPREVResult;
import com.vega_systems.ws.locprotns.WSBPSYNTHPLANPREVResult;
import com.vega_systems.ws.locprotns.ArrayOfPPREV;
import com.vega_systems.ws.locprotns.ArrayOfSYNTHESE;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.rest.RestAPIContext;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

import java.net.URL;

/**
 * Controller class
 */
public class Index extends AbstractIndex {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getName());
    private final static String ERROR_MSG_WS_ERROR = "Echec de l'appel du web service Lyre"; 

    /**
     * Ensure request is valid
     *
     * @param request the HttpRequest
     */
    @Override
    public String validateInputParameters(HttpServletRequest request) {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values

        // Retrieve method parameter
        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            throw new ValidationException(format("the parameter %s is missing", PARAM_METHOD));
        } else {
        	// Check mandatory parameters depending on the method
        	if (METHOD_QUERY.equalsIgnoreCase(method)) {
        		checkMethodParameters(request, QUERY_MANDATORY_PARAMETERS, QUERY_OPTIONAL_PARAMETERS);
        		
        		// Check node type value
        		String node_type = methodParameters.get(PARAM_NODE_TYPE);
        		if (! (NODE_TYPE_ENTITE.equalsIgnoreCase(node_type) || NODE_TYPE_REGION.equalsIgnoreCase(node_type))) {
            		node_type = (node_type == null) ? "" : node_type;
        			throw new ValidationException(format("invalid value {%s] for parameter %s", node_type, PARAM_NODE_TYPE));
        		}
        		
        	} else if (METHOD_DETAILS.equalsIgnoreCase(method)) {
        		checkMethodParameters(request, DETAILS_MANDATORY_PARAMETERS, DETAILS_OPTIONAL_PARAMETERS);
        		        		
        	} else {
                throw new ValidationException(format("invalid value [%s] for parameter %s", method, PARAM_METHOD));        		
        	}
        }
        
        return method;
    }

    /**
     * Execute business logic
     *
     * @param context
     * @param method
     * @return Result
     */
    @Override
    protected Result execute(RestAPIContext context, String method) {
    	// Get URL from configuration parameters
        Properties props = loadProperties("configuration.properties", context.getResourceProvider());
        String webservice_cookie = props.getProperty(CONFIG_PARAM_COOKIE);
        String webservice_host = props.getProperty(CONFIG_PARAM_HOST);
        String webservice_url_template = props.getProperty(CONFIG_PARAM_URL); 
        LOGGER.debug("Web service host: " + webservice_host + ", URL template: " + webservice_url_template);
        String webservice_url = format(webservice_url_template, webservice_host);

		// Get sgid
		APISession session_api = context.getApiSession();
		String sgid = session_api.getUserName();
        
        /*
         * Call Lyre web service
         */
        LOGGER.info(format("Execute rest api call %s on [%s]",  method, webservice_url));
        Result.ResultBuilder planResults = callLyreWebService(method, webservice_url, webservice_cookie, sgid);

        // Prepare the result
        return planResults.build();

    }
    
    private Result.ResultBuilder callLyreWebService(String method, String webservice_url, String webservice_cookie, String sgid) {
        Result.ResultBuilder planResults = Result.builder();
		String error_msg;
		String error_num;
        
        try {
        	// Get SOAP service
        	URL urlBudgePop = new URL(webservice_url);
    		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
    		LocproPortType soapService = budgePopService.getLocproPort();
    		
    		// Call web service
    		if (METHOD_DETAILS.equalsIgnoreCase(method)) {
    			// Get parameters for getDetails method
    			String id = methodParameters.get(PARAM_ID);
    			String year = methodParameters.get(PARAM_YEAR);
    			
    			// Call Lyre web service
    			WSBPPLANPREVResult ws_res = soapService.wsbpPLANPREV(webservice_cookie, sgid, id, year);
        		error_msg = ws_res.getERRORMSG();
        		error_num = ws_res.getERRORNUM();
        		ArrayOfPPREV array_prev = ws_res.getPPREVS();
        		List<PPREV> list_prev = array_prev.getPPREV();
        		planResults.setDetails(list_prev);
        				
    		} else if (METHOD_QUERY.equalsIgnoreCase(method)) {
    			// Get entite and region parameters for query method
    			String node_type = methodParameters.get(PARAM_NODE_TYPE);
    			String node_code = methodParameters.get(PARAM_NODE_CODE);
    			String entite = (NODE_TYPE_ENTITE.equalsIgnoreCase(node_type)) ? node_code : null;
    			String region = (NODE_TYPE_REGION.equalsIgnoreCase(node_type)) ? node_code : null;
    			
    			// Get select criteria 
    			String select_contrat = methodParameters.get(PARAM_SELECT_CONTRAT).toLowerCase();
    			String select_rachat = methodParameters.get(PARAM_SELECT_RACHAT).toLowerCase();
    			
    			// Get start year and end year
    			String start_year = methodParameters.get(PARAM_YEAR_START);
    			String end_year = methodParameters.get(PARAM_YEAR_END);
    			
    			// Get optional filter criteria
    			String genre = methodParameters.get(PARAM_GENRE);
    			String sous_genre = methodParameters.get(PARAM_SOUS_GENRE);
    			String energy = methodParameters.get(PARAM_ENERGY);
    			String retrofit = methodParameters.get(PARAM_RETROFIT);
    			
    			// Call Lyre web service
    			WSBPSYNTHPLANPREVResult ws_res = soapService.wsbpSYNTHPLANPREV(webservice_cookie, sgid, entite, region, select_contrat, select_rachat, start_year, end_year, genre, sous_genre, energy, retrofit);
    	        error_msg = ws_res.getERRORMSG();
    	        error_num = ws_res.getERRORNUM();
    	        String id = ws_res.getId();
    	        ArrayOfSYNTHESE array_syntheses = ws_res.getSYNTHESES();
    	        if (array_syntheses != null) {
    	        	List<SYNTHESE> list_syntheses = array_syntheses.getSYNTHESE();
    	        	if (list_syntheses != null) {
    	        		planResults.setSyntheses(id, list_syntheses);
    	        	}
    	        }
    	        
    		} else {
    			// This should never occur
    			error_msg = ERROR_MSG_WS_ERROR;
    			error_num = "UNKNOWN_METHOD";
    		}
    		
    		// Retrieve error message in response
    		if ((error_msg != null) && (error_msg.length() > 0)) {
    			planResults.setError(error_msg, error_num);
    			LOGGER.error(format("Web service %s response message : %s, error number=%s", method, error_msg, error_num));
    		} else {
    			planResults.setError("", "");
    		}

        } catch(Exception e) {
        	LOGGER.error("Exception callLyreWebService: " + e.toString());
        	planResults.setError(ERROR_MSG_WS_ERROR + "(" + e.toString() + ")", "EXCEPTION");
        }
		
        return planResults;
    }

    
    private void checkMethodParameters(HttpServletRequest request, String[] mandatory_method_parameters, String[] optional_parameters) {
    	methodParameters = new HashMap<String, String>();
    	
    	// Check and retrieve mandatory parameters
    	int nb_parameters = mandatory_method_parameters.length;
    	for (int i=0; i<nb_parameters; i++) {
    		String parameter_name = mandatory_method_parameters[i];
    		String parameter_value = request.getParameter(parameter_name);
            if (parameter_value == null) {
                throw new ValidationException(format("the parameter %s is missing", parameter_name));
            } else {
            	methodParameters.put(parameter_name, parameter_value);
            }
    	}
    	
    	// Retrieve optional parameters
    	nb_parameters = optional_parameters.length;
    	for (int i=0; i<nb_parameters; i++) {
    		String parameter_name = optional_parameters[i];
    		String parameter_value = request.getParameter(parameter_name);
            if (parameter_value != null) {
            	methodParameters.put(parameter_name, parameter_value);
            }
    	}
    }
}