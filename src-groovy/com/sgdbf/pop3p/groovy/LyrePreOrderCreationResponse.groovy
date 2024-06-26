package com.sgdbf.pop3p.groovy


import com.sgdbf.pop3p.groovy.LogFile;

import org.w3c.dom.NodeList
import org.w3c.dom.Node

class LyrePreOrderCreationResponse extends AnalyzeWebServiceResponse implements java.io.Serializable {
	private static LogFile log_file = null;
	
	// Expected XML nodes
	private final static String EXPECTED_RESPONSE_NODE = "WSBP_PRECOMMANDEResult";
	private final static String NODE_STATUS = "status";
	private final static String NODE_ERROR = "error";
	private final static String NODE_COMMENTS = "COMMENTS";
	
	// Status value returned by Lyre
	private final static String LYRE_STATUS_OK = "OK";
	
	public String responseSource;
	public String errorMsg;
	public String status;
	public List<String> ordersList;
	public boolean isResponseOk;
	
	public LyrePreOrderCreationResponse(javax.xml.transform.Source sourceResponse, org.w3c.dom.Document responseDocumentBody) {
		isResponseOk = false;	// Not OK by default, unless we get an OK status
		ordersList = new ArrayList<String>();
		
		if (sourceResponse == null) {
			responseSource = "";
		} else {
			responseSource = sourceResponse.getSystemId();
			if (responseSource == null) {
				responseSource = "";
			}
		}
		
		errorMsg = analyzeResponse(responseDocumentBody, EXPECTED_RESPONSE_NODE);
	}
	
	public int getNumberOfLyreOrders() {
		def int nb_orders = 0;
		
		if (ordersList != null) {
			nb_orders = ordersList.size();
		}
		
		return nb_orders;
	}
	
	public String getLyreOrders() {
		def String orders = "";
		
		if (ordersList != null) {
			orders = ordersList.toListString();
		}
		return orders;
	}
	
	protected String analyzeResponseNodes(NodeList nodes_list) {
		String error_msg = "";
		
		try {
			for (Node one_node : nodes_list) {
			String node_name = one_node.getNodeName();
			if (NODE_STATUS.equalsIgnoreCase(node_name)) {
				debug("analyzeResponseNodes", "Found node " + node_name);
				String status_value = getElementValue(one_node);
				if (status_value != null) {
					status = status_value;
								
					// Check if status is OK
					if (LYRE_STATUS_OK.equalsIgnoreCase(status)) {
						debug("analyzeResponseNodes", "Node " + node_name + " with value [" + String.valueOf(status) + "] is OK");
						isResponseOk = true;
					} else {
						debug("analyzeResponseNodes", "Node " + node_name + " with value [" + String.valueOf(status) + "] is NOT OK");
								}
					} else {
						// This should not occur
						error("analyzeResponseNodes", "Node " + node_name + " with no value found !");
						status = "";
					}
							
				} else if (NODE_ERROR.equalsIgnoreCase(node_name)) {
					debug("analyzeResponseNodes", "Found node " + node_name);
					String error_value = getElementValue(one_node);
					if (error_value != null) {
						error_msg = error_value;
						debug("analyzeResponseNodes", "Node " + node_name + " with value [" + String.valueOf(status) + "]");
					} else {
						debug("analyzeResponseNodes", "Node " + node_name + " with no error message");
					}
							
				}  else if (NODE_COMMENTS.equalsIgnoreCase(node_name)) {
					debug("analyzeResponseNodes", "Found node " + node_name);
					def NodeList nodes_comments = one_node.getChildNodes();
					for (Node one_comment_node : nodes_comments) {
						def String error_comment = addOrderNumber(one_comment_node);
						if (!error_comment.isEmpty()) {
							error_msg += error_comment + ". ";
						}
					}
				}		
			}
						
		} catch(e) {
			exception("analyzeResponseNodes", e);
			error_msg += AnalyzeWebServiceResponse.ERROR_EXCEPTION + " : " + e.getMessage();
			isResponseOk = false;
		}
		info("analyzeResponseNodes", "Returning result: [" + error_msg + "]");
		return error_msg;
	}
	
	private String addOrderNumber(Node one_comment_node) {
		def String error_msg = "";
		
		try {
			def NodeList comment_fields = one_comment_node.getChildNodes();
			for (Node one_field : comment_fields) {
				def String field_name = one_field.getNodeName();
				def String order_number = getElementValue(one_field);
				if ((order_number != null) && (!order_number.isEmpty())) {
					debug("analyzeResponseNodes", "Lyre order created : [" + order_number + "]");
					ordersList.add(order_number);
				}
			}
			
		} catch(e) {
			exception("analyzeResponseNodes", e);
			error_msg += AnalyzeWebServiceResponse.ERROR_EXCEPTION + " : " + e.getMessage();
			isResponseOk = false;
		}
		
		return error_msg;
	}
	
	protected LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("LyrePreOrderCreationResponse");
		}
		
		return log_file;
	}
	
}
