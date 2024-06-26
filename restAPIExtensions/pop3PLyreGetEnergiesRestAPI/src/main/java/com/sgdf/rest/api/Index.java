package com.sgdf.rest.api;

import com.sgdf.rest.api.dto.Result;
import com.vega_systems.ws.locprotns.ArrayOfENERGIE;
import com.vega_systems.ws.locprotns.ENERGIE;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.WSBPLSTENERGIESResult;

import org.bonitasoft.web.extension.rest.RestAPIContext;

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
    public void validateInputParameters(HttpServletRequest request) {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values


    }

    /**
     * Execute business logic
     *
     * @param context
     * @return Result
     */
    @Override
    protected Result execute(RestAPIContext context) {

        Properties props = loadProperties("configuration.properties", context.getResourceProvider());
        String webservice_cookie = props.getProperty(CONFIG_PARAM_COOKIE);
        String webservice_host = props.getProperty(CONFIG_PARAM_HOST);
        String webservice_url_template = props.getProperty(CONFIG_PARAM_URL); 
        LOGGER.debug("Web service host: " + webservice_host + ", URL template: " + webservice_url_template);
        String webservice_url = format(webservice_url_template, webservice_host);

        LOGGER.info(format("Execute rest api call on [%s]",  webservice_url));

        /*
         * Call Lyre web service
         */
        Result.ResultBuilder energiesResults = callLyreWebService(webservice_url, webservice_cookie);

        // Prepare the result
        return energiesResults.build();
    }
    
    private Result.ResultBuilder callLyreWebService(String webservice_url, String webservice_cookie) {
        Result.ResultBuilder energiesResults = Result.builder();
        
        try {
        	URL urlBudgePop = new URL(webservice_url);
    		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);

    		// Call web service
    		LocproPortType soapService = budgePopService.getLocproPort();
    		WSBPLSTENERGIESResult ws_res = soapService.wsbpLSTENERGIES(webservice_cookie);
    		
    		// Retrieve error message in response
    		String error_msg = ws_res.getERRORMSG();
    		String error_num = ws_res.getERRORNUM();
    		if ((error_msg != null) && (error_msg.length() > 0)) {
    			energiesResults.setError(error_msg, error_num);
    			LOGGER.error(format("Web service response message : %s, error number=%s", error_msg, error_num));
    		} else {
    			energiesResults.setError("", "");
    		}
    		
    		// Retrieve list of energies
    		ArrayOfENERGIE array_energies = ws_res.getENERGIES();
    		List<ENERGIE> list_energies = array_energies.getENERGIE();
    		energiesResults.setEnergies(list_energies);

        } catch(Exception e) {
        	LOGGER.error("Exception callLyreWebService: " + e.toString());
        	energiesResults.setError(ERROR_MSG_WS_ERROR + e.getMessage(), "EXCEPTION");
        }
		
        return energiesResults;
    }
}