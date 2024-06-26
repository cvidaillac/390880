package com.sgdbf.pop3p.groovy
import com.sgdbf.pop3p.groovy.LogFile;

import org.w3c.dom.Node
import org.w3c.dom.NodeList

import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

abstract class AnalyzeWebServiceResponse {

	// Error messages
	protected final static String ERROR_EMPTY_RESPONSE = "Error: Lyre web service response is empty";
	protected final static String ERROR_UNEXPECTED_RESPONSE = "Error: Lyre web service response does not contain the expected node"
	protected final static String ERROR_EXCEPTION = "Error: Exception analyzing Lyre web service response";
	
	// Main method to analyze the web service response
	public String analyzeResponse(org.w3c.dom.Document responseDocumentBody, String expected_response_node) {
		String error_msg = "";
		
		try {
			// Debug message
			debug("analyzeResponse", "Analyzing response from Lyre");
			if (responseDocumentBody == null) {
				debug("analyzeResponse", "NULL response from Lyre");
				error_msg = ERROR_EMPTY_RESPONSE;
				
			} else {
				def String xml_content = xmlToString(responseDocumentBody);
				if (xml_content.length() > 2048) {
					xml_content = xml_content.take(2048) + "...";
				}
				debug("analyzeResponse", "Response received: " + xml_content);
					
				def NodeList nodes_list = responseDocumentBody.childNodes;
				if ((nodes_list == null) || (nodes_list.getLength() == 0)) {
					debug("analyzeResponse", "No node in response from Lyre");
					error_msg = ERROR_EMPTY_RESPONSE;
					
				} else {
					// Get root response node
					def Node current_node = nodes_list.item(0);
					def boolean response_found = false;
					while ((current_node != null) && (!response_found)) {
						if (expected_response_node.equalsIgnoreCase(current_node.getNodeName())) {
							response_found = true;
						} else {
							current_node = current_node.getFirstChild();
						}
					}
					
					if (!response_found) {
						error_msg = ERROR_UNEXPECTED_RESPONSE + " ["  + expected_response_node + "]";
					} else {
						// Get status, error message and list of Comments
						nodes_list = current_node.getChildNodes();
						error_msg = analyzeResponseNodes(nodes_list);
					}
				}
			}
			
		} catch(e) {
			exception("analyzeResponse", e);
			error_msg += ERROR_EXCEPTION + " : " + e.getMessage();
		}
		info("analyzeResponse", "Returning result: [" + error_msg + "]");
		return error_msg;
	}
	
	// Abstract methods to be implemented by sub-classes
	protected abstract String analyzeResponseNodes(NodeList nodes_list);
	protected abstract LogFile getLogFile();
	
	// Utility methods
	protected String xmlToString(org.w3c.dom.Document newDoc) {
		def String res="";
		
		try {
			DOMSource domSource = new DOMSource(newDoc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			res = sw.toString();
		} catch(e) {
			exception("xmlToString", e);
		}
		return res;
	}
	  
	protected String getElementValue(Node one_node) {
		def String element_value = "";
	
		try {
			NodeList  child_nodes = one_node.getChildNodes();
			if ((child_nodes  != null) && (child_nodes.getLength() > 0)) {
				element_value  = child_nodes.item(0).getNodeValue();
			}
			
		} catch(e) {
			exception("getElementValue", e);
		}
		return element_value ;
	}
		
	protected void debug(String method, String message) {
		try {
			LogFile log_file = getLogFile();
			log_file.debug(method, message);
			
		} catch(e) {
			// Ignore any error
		}
	}
	
	protected void info(String method, String message) {
		try {
			LogFile log_file = getLogFile();
			log_file.info(method, message);
			
		} catch(e) {
			// Ignore any error
		}
	}

	protected void error(String method, String message) {
		try {
			LogFile log_file = getLogFile();
			log_file.error(method, message);
			
		} catch(e) {
			// Ignore any error
		}
	}
	
	protected void exception(String method, Exception e) {
		try {
			LogFile log_file = getLogFile();
			log_file.exception(method, e);
			
		} catch(es) {
			// Ignore any error
		}
	}
}
