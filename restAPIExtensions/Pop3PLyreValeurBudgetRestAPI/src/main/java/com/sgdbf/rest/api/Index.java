package com.sgdbf.rest.api;

import static java.lang.String.format;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.extension.rest.RestAPIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.dto.Result.BudgetValue;
import com.sgdbf.rest.api.exception.ValidationException;
import com.vega_systems.ws.locprotns.ArrayOfCOUPLE;
import com.vega_systems.ws.locprotns.COUPLE;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.WSBPVALBUDGETResult;
//import com.vega_systems.ws.valbudget.COUPLE;
//import com.vega_systems.ws.valbudget.ArrayOfCOUPLE;
//import com.vega_systems.ws.valbudget.LocproPortType;
//import com.vega_systems.ws.valbudget.LocproServiceBUDGEPOP;
//import com.vega_systems.ws.valbudget.WSBPVALBUDGETResult;

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
    public void validateInputParameters(HttpServletRequest request) {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values

        // Retrieve genre parameter
        String genre = request.getParameter(PARAM_GENRE);
        if (genre == null) {
            throw new ValidationException(format("the parameter %s is missing", PARAM_GENRE));
        }
        // Retrieve sousGenre parameter
        String sousGenre = request.getParameter(PARAM_SOUSGENRE);
        if (sousGenre == null) {
            throw new ValidationException(format("the parameter %s is missing", PARAM_SOUSGENRE));
        }
        // Retrieve exercice parameter
        String exercice = request.getParameter(PARAM_EXERCICE);
        if (exercice == null) {
            throw new ValidationException(format("the parameter %s is missing", PARAM_EXERCICE));
        }
        // Retrieve entityCode parameter
        String entityCode = request.getParameter(PARAM_ENTITYCODE);
        if (entityCode == null) {
            throw new ValidationException(format("the parameter %s is missing", PARAM_ENTITYCODE));
        }
        
        LOGGER.debug("REST API service parameters are OK");
    }

    /**
     * Execute business logic
     *
     * @param context
     * @param p
     * @param c
     * @param genre
     * @param sousGenre
     * @param exercice
     * @param entityCode
     * @return Result
     */
    @Override
    protected Result execute(RestAPIContext context, String genre, String sousGenre, String exercice, String entityCode) {

        // Here is an example of how you can retrieve configuration parameters from a properties file
        // It is safe to remove this if no configuration is required
        Properties props = loadProperties("configuration.properties", context.getResourceProvider());
        String webservice_cookie = props.getProperty(CONFIG_PARAM_COOKIE);
        String webservice_host = props.getProperty(CONFIG_PARAM_HOST);
        String webservice_url_template = props.getProperty(CONFIG_PARAM_URL); 
        LOGGER.debug("Web service host: " + webservice_host + ", URL template: " + webservice_url_template);
        String webservice_url = format(webservice_url_template, webservice_host);

        LOGGER.info(format("Execute rest api call on [%s] with params: %s, %s, %s, %s",  webservice_url, genre,  sousGenre,  exercice,  entityCode));

        /*
         * Call Lyre web service
         */
        Result.ResultBuilder suggestedResults = callLyreWebService(webservice_url, webservice_cookie, genre,  sousGenre,  exercice,  entityCode);

        // Prepare the result
        return suggestedResults.build();
    }
    
    private Result.ResultBuilder callLyreWebService(String webservice_url, String webservice_cookie, String genre, String sousGenre, String exercice, String entityCode) {
        Result.ResultBuilder suggestedResults = Result.builder();
        
        try {
        	URL urlBudgePop = new URL(webservice_url);
    		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);

    		// Call web service
    		LocproPortType soapService = budgePopService.getLocproPort();
    		WSBPVALBUDGETResult ws_res = soapService.wsbpVALBUDGET(webservice_cookie, entityCode, exercice, genre, sousGenre);
    		
    		// Retrieve message in response
    		String res_msg = ws_res.getMESSAGE();
    		if ((res_msg != null) && (res_msg.length() > 0)) {
    			suggestedResults.errorMsg(res_msg);
    			LOGGER.error(format("Web service response message : %s", res_msg));
    		} else {
    			suggestedResults.errorMsg("");
    		}
    		
    		// Retrieve list of suggestions
    		ArrayOfCOUPLE res_couples = ws_res.getCOUPLES();
			List<BudgetValue> suggested_values = new ArrayList<BudgetValue>();
    		if (res_couples != null)  {
    			List<COUPLE> list_couples = res_couples.getCOUPLE();
    			int nb_couples =list_couples.size();
    			LOGGER.info(format("Web service response with %d suggested values", nb_couples));
    			if (nb_couples > 0) {
    				for (int i=0; i<nb_couples; i++) {
    					COUPLE one_couple = list_couples.get(i);
    					BudgetValue one_value = new BudgetValue(one_couple.getTexte(), one_couple.getMontant());
    					suggested_values.add(one_value);
    				}
    			}
				suggestedResults.setSuggestedValuesFromList(suggested_values);
    		} else {
    			LOGGER.info("No suggested values in Web service response"); 
				suggestedResults.setSuggestedValuesFromList(suggested_values);
    		}
    		
        	
        } catch(Exception e) {
        	LOGGER.error("Exception callLyreWebService: " + e.toString());
        	suggestedResults.errorMsg(ERROR_MSG_WS_ERROR + e.getMessage());
        }
		
        return suggestedResults;
    }
}