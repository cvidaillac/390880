package com.sgdbf.pop3p.groovy

import com.bonitasoft.engine.api.APIAccessor
import com.bonitasoft.engine.api.LogAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.log.Log
import com.bonitasoft.engine.log.LogSearchDescriptor
import org.bonitasoft.engine.bpm.connector.ConnectorInstance
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo
import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor
import org.bonitasoft.engine.bpm.connector.ConnectorState
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult

class CheckConnectorExecution {
	private LogFile logFile = null;
	public final static String CONNECTOR_TYPE_EMAIL = "email";
	
	public Long taskId;
	public boolean isFailed;
	public String connectorName;
	public String connectorType;
	public long connectorInstanceId;
	public String errorMsg;
	public String stackTrace;
	
	public CheckConnectorExecution(Long task_id, APIAccessor apiAccessor, LogFile log_file ) {
		taskId = task_id;
		errorMsg = "";
		stackTrace = "";
		logFile = log_file;
		
		isFailed = checkConnectorsOnTask(task_id, apiAccessor.getProcessAPI());
	}
	
	private boolean checkConnectorsOnTask(Long task_id, ProcessAPI process_api) {
		boolean is_failed = false;
		
		try {
			// Search failed connector matching the task id
			def SearchOptionsBuilder search_connector_options = new SearchOptionsBuilder(0, 1)
										.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, task_id)
										.filter(ConnectorInstancesSearchDescriptor.STATE, ConnectorState.FAILED.name());
			def SearchResult<ConnectorInstance> connector_search_result = process_api.searchConnectorInstances(search_connector_options.done());
			def int nb_connectors = connector_search_result.getCount();
			if (nb_connectors == 0) {
				logFile.debug("checkConnectorsOnTask", "No failed connector found for task " + String.valueOf(task_id));
				
			} else {
				// Found at least one failed connector, checking the first one
				is_failed = true;
				ConnectorInstance connectorInstance = connector_search_result.getResult().get(0);
				connectorType = connectorInstance.getConnectorId();
				connectorName = connectorInstance.getName();
				connectorInstanceId = connectorInstance.getId();
				logFile.debug("checkConnectorsOnTask", "Found failed connector [" + connectorName + "] of type [" + connectorType + "] with connectorInstanceId [" + String.valueOf(connectorInstanceId) + "] for activityInstance with id " + String.valueOf(task_id));
				
				// Searching exceptions
				ConnectorInstanceWithFailureInfo  failure_info = process_api.getConnectorInstanceWithFailureInformation(connectorInstanceId);
				errorMsg = failure_info.getExceptionMessage()
				stackTrace = failure_info.getStackTrace();
				logFile.debug("checkConnectorsOnTask", "Failed connector message : " + errorMsg + " - Stack trace : " + stackTrace);
			}
			
		} catch(Exception e) {
			logFile.exception("checkConnectorsOnTask", e);
		}
		
		return is_failed;
	}
	
	public boolean isWebServicePermanentError() {
		def boolean is_permanent_error = isWebServiceInternalServerError();
		if (! is_permanent_error) {
			is_permanent_error = isSoapFaultException();
		}
		
		return is_permanent_error;
	}
	
	public boolean isSoapFaultException() {
		boolean is_soap_error = isConnectorLogError("ServerSOAPFaultException");
		logFile.info("isSoapFaultException", "Is permanent SOAP Fault exception : " + String.valueOf(is_soap_error));
		
		return is_soap_error;
	}

	public boolean isWebServiceInternalServerError() {
		boolean is_permanent_error = isConnectorLogError("HTTP status code 500");
		logFile.info("isWebServiceInternalServerError", "Is permanent error HTTP 500 : " + String.valueOf(is_permanent_error));
		
		return is_permanent_error;
	}
	
	public boolean isEmailPermanentError() {
		def boolean is_email_permanent_error = false;
		
		if (connectorType.equalsIgnoreCase(CONNECTOR_TYPE_EMAIL)) {
			is_email_permanent_error = isConnectorLogError("User Unknown");
			logFile.debug("isEmailPermanentError", "Is permanent email error User Unknown : " + String.valueOf(is_email_permanent_error));
		} else {
			logFile.debug("isEmailPermanentError", "Connector type [" + connectorType + "] is not email");
		}
		
		return is_email_permanent_error;
	}
	
	public boolean isConnectorLogError(String error) {
		return errorMsg.contains(error) || stackTrace.contains(error); 
	}
	
	public throwTemporaryErrorException() throws ConnectorExecutionTemporaryException {
		throw new ConnectorExecutionTemporaryException(errorMsg, stackTrace);
	}
	
	public class ConnectorExecutionTemporaryException extends Exception {
		public ConnectorExecutionTemporaryException(String error_msg, String stackTrace) {
			super(error_msg, new Exception(stackTrace));
		}
	}
}
