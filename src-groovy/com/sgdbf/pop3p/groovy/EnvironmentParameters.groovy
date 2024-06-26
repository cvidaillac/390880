package com.sgdbf.pop3p.groovy
import com.bonitasoft.engine.api.APIAccessor
import com.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.parameter.ParameterInstance
import com.sgdbf.pop3p.groovy.LogFile;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.NotFoundException

class EnvironmentParameters {
	
	// Default process name to load parameters
	public static String DEFAULT_PROCESS_NAME = "POP3P_BudgetProposition_Init";
	public static String DADR_PROCESS_NAME = "Pop3P_DADR_BudgetProposition_Init";
	
	// Name of email parameters
	public static String PARAM_SMTP_HOST = "smtp_host";
	public static String PARAM_SMTP_PORT = "smtp_port";
	public static String PARAM_SMTP_USERNAME = "smtp_username";
	public static String PARAM_SMTP_PASSWORD = "smtp_userpassword";
	public static String PARAM_EMAIL_SENDER = "noReplyMail";
	public static String PARAM_IS_TEST = "test";
	public static String PARAM_TEST_RECIPIENTS_LIST = "testRecipientsList";
	public static String PARAM_DEFAULT_RECIPIENT_WHEN_MISSING = "defaultToRecipientWhenMissing";
	
	// Bonita parameters
	public static String PARAM_APPLICATION_SMARTE = "pop3_app";
	public static String PARAM_BONITA_HOST = "bonita_server_host";
	public static String PARAM_BONITA_PORT = "bonita_server_port";
	public static String PARAM_BONITA_TENANT = "bonita_server_tenant";
	
	// Lyre parameters
	public static String PARAM_LYRE_WSDL = "urlWsdlLyre"; 
	public static String PARAM_LYRE_COOKIE = "lyre_ws_cookie";
	
	// Name of Due date calculation parameter
	public static String PARAM_DUE_DATE_DELAY = "dxxValidationDueDateDelay";
	
	// List of parameters
	public static String[] EMAIL_PARAM_NAMES = [PARAM_SMTP_HOST, PARAM_SMTP_PORT, PARAM_SMTP_USERNAME, PARAM_SMTP_PASSWORD, PARAM_EMAIL_SENDER, PARAM_IS_TEST, PARAM_TEST_RECIPIENTS_LIST, PARAM_DEFAULT_RECIPIENT_WHEN_MISSING];
	public static String[] LYRE_PARAM_NAMES = [PARAM_LYRE_WSDL, PARAM_LYRE_COOKIE];
	public static String[] BONITA_PARAM_NAMES = [PARAM_BONITA_HOST, PARAM_BONITA_PORT, PARAM_BONITA_TENANT, PARAM_APPLICATION_SMARTE];
	public static String[] ALL_PARAM_NAMES = EMAIL_PARAM_NAMES + LYRE_PARAM_NAMES + BONITA_PARAM_NAMES;
	
	private static EnvironmentParameters defaultEnvtParameters = null;
	private static LogFile log_file = null;
	
	public String processName;
	public Long processDefinitionId;
	
	private APIAccessor api;
	private ProcessAPI processApi;
	private Map<String, Object> mapParameterNameToValue;
	
	public EnvironmentParameters(String process_name, com.bonitasoft.engine.api.APIAccessor apiAccessor) {
		mapParameterNameToValue = new HashMap<String, Object>();
		api = apiAccessor;
		processName = process_name;
		
		try {
			processApi = api.getProcessAPI();
			processDefinitionId =  processApi.getLatestProcessDefinitionId(processName);
		} catch(ProcessDefinitionNotFoundException  ex) {
			error("EnvironmentParameters", "Process definition not found for [" + processName + "]");
			
		} catch(e) {
			exception("EnvironmentParameters", e);
		}
	}
	
	public static EnvironmentParameters getProcessParameters(com.bonitasoft.engine.api.APIAccessor apiAccessor) {
		if (defaultEnvtParameters == null) {
			defaultEnvtParameters = new EnvironmentParameters(DEFAULT_PROCESS_NAME, apiAccessor); 
		}
		return defaultEnvtParameters;
	}
	
	public static Map<String, Object> getAllParameters(com.bonitasoft.engine.api.APIAccessor apiAccessor) {
		EnvironmentParameters envt_params = getProcessParameters(apiAccessor);
		return envt_params.getListOfParameters(ALL_PARAM_NAMES);
	}

	public Object getParameter(String parameter_name) {
		def Object parameter_value = null;
		
		try {
			parameter_value = mapParameterNameToValue.get(parameter_name);
			if (parameter_value == null) {
				def ParameterInstance param_instance = processApi.getParameterInstance(processDefinitionId, parameter_name);
				if (param_instance != null) {
					parameter_value = param_instance.getValue();
					if (!parameter_name.contains("password")) {
						debug("getParameter", "Parameter [" + parameter_name + "] = [" + String.valueOf(parameter_value) + "]");
					}
					mapParameterNameToValue.put(parameter_name, parameter_value);
				}
			}
		} catch(NotFoundException   ex) {
			error("getParameter", "Process parameter [" + parameter_name + "] not found for process [" + processName + "]");
			
		} catch(e) {
			exception("getParameter", e);
		}
		
		return parameter_value;
	}
	
	public Map<String, Object> getListOfParameters(String[] list_parameters) {
		for (String one_param : list_parameters) {
			getParameter(one_param);
		}
		
		return mapParameterNameToValue;
	}
	
	public static int getValidationDueDateDelay(com.sgdbf.model.Proposition current_proposition, com.bonitasoft.engine.api.APIAccessor apiAccessor) {
		def int delay_in_days = 0;
		def boolean is_dadr = false;
		
		try {
			// Determine if current proposition is a DADR
			if ((current_proposition != null) && ("DADR".equalsIgnoreCase(current_proposition.typeWorkflow))) {
				is_dadr = true;
			}
			
			def String target_process_name = (is_dadr) ? DADR_PROCESS_NAME : DEFAULT_PROCESS_NAME;
			def EnvironmentParameters process_params = new EnvironmentParameters(target_process_name, apiAccessor);
			def String param_value = process_params.getParameter(PARAM_DUE_DATE_DELAY);
			if ((param_value != null) && (param_value != "")) {
				delay_in_days = Integer.valueOf(param_value);
			}
			debug("getValidationDueDateDelay", "Delay in days found for process [" + target_process_name + "] : " + String.valueOf(delay_in_days));
			
		} catch(e) {
			exception("getValidationDueDateDelay", e);
		}
		
		return delay_in_days;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("EnvironmentParameters");
		}
		
		return log_file;
	}
	
	private static void debug(String method, String message) {
		try {
			log_file = getLogFile();
			log_file.debug(method, message);
			
		} catch(e) {
			// Ignore any error
		}
	}

	private static void error(String method, String message) {
		try {
			log_file = getLogFile();
			log_file.error(method, message);
			
		} catch(e) {
			// Ignore any error
		}
	}
	
	private static void exception(String method, Exception e) {
		try {
			log_file = getLogFile();
			log_file.exception(method, e);
			
		} catch(es) {
			// Ignore any error
		}
	}
}
