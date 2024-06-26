package com.sgdbf.rest.api;

import com.sgdbf.rest.api.dto.Result;
import com.sgdbf.rest.api.dto.Result.RegionValue;
import com.vega_systems.ws.locprotns.ArrayOfREGION;
import com.vega_systems.ws.locprotns.LocproPortType;
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP;
import com.vega_systems.ws.locprotns.REGION;
import com.vega_systems.ws.locprotns.WSBPLSTREGIONSResult;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.rest.RestAPIContext;

import java.util.ArrayList;
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

        // Here is an example of how you can retrieve configuration parameters from a properties file
        // It is safe to remove this if no configuration is required
        Properties props = loadProperties("configuration.properties", context.getResourceProvider());
        String webservice_cookie = props.getProperty(CONFIG_PARAM_COOKIE);
        String webservice_host = props.getProperty(CONFIG_PARAM_HOST);
        String webservice_url_template = props.getProperty(CONFIG_PARAM_URL); 
        LOGGER.debug("Web service host: " + webservice_host + ", URL template: " + webservice_url_template);
        String webservice_url = format(webservice_url_template, webservice_host);

        // Get SGID
        APISession session_api = context.getApiSession();
		String sgid = session_api.getUserName();
        LOGGER.info(format("Execute rest api call on [%s] for user %s",  webservice_url, sgid));

        /*
         * Call Lyre web service
         */
        Result.ResultBuilder regionResults = callLyreWebService(webservice_url, webservice_cookie, sgid);

        // Prepare the result
        return regionResults.build();
    }
    
    private Result.ResultBuilder callLyreWebService(String webservice_url, String webservice_cookie, String sgid) {
        Result.ResultBuilder regionResults = Result.builder();
        
        try {
        	URL urlBudgePop = new URL(webservice_url);
    		LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);

    		// Call web service
    		LocproPortType soapService = budgePopService.getLocproPort();
    		WSBPLSTREGIONSResult ws_res = soapService.wsbpLSTREGIONS(webservice_cookie, sgid);
    		
    		// Retrieve message in response
    		String res_msg = ws_res.getERRORMSG();
    		if ((res_msg != null) && (res_msg.length() > 0)) {
    			regionResults.errorMsg(res_msg);
    			LOGGER.error(format("Web service response message : %s", res_msg));
    		} else {
    			regionResults.errorMsg("");
    		}
    		
    		// Retrieve list of regions
    		ArrayOfREGION res_regions = ws_res.getREGIONS();
			List<RegionValue> regions_values = new ArrayList<RegionValue>();
    		if (res_regions != null)  {
    			List<REGION> list_regions = res_regions.getREGION();
    			int nb_regions = list_regions.size();
    			LOGGER.info(format("Web service response with %d regions", nb_regions));
    			if (nb_regions > 0) {
    				for (int i=0; i<nb_regions; i++) {
    					REGION one_region = list_regions.get(i);
    					RegionValue one_value = new RegionValue(one_region.getRegionId(), one_region.getRegionName());
    					regions_values.add(one_value);
    				}
    			}
    			regionResults.setRegionsFromList(regions_values);
    		} else {
    			LOGGER.info("No suggested values in Web service response"); 
    			regionResults.setRegionsFromList(regions_values);
    		}
    		
        	
        } catch(Exception e) {
        	LOGGER.error("Exception callLyreWebService: " + e.toString());
        	regionResults.errorMsg(ERROR_MSG_WS_ERROR + e.getMessage());
        }
		
        return regionResults;
    }
}
