package com.sgdbf.pop3p.groovy

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogFile {
	private Logger logger;
	private String processName = "";
	
	public static LogFile getLogFile(String process_name) {
		def String log_class = "com.sgdbf.groovy." + process_name;
		def LogFile log_file = new LogFile();
		
		log_file.logger = LoggerFactory.getLogger(log_class);
		log_file.processName = process_name;
		
		return log_file;
	}
	
	
	public debug(String method, String message) {
		try {
			logger.debug(processName + "." + method + " : " + message);
		} catch(e) {
			// Ignore any error
		}
	}
	
	public info(String method, String message) {
		try {
			logger.info(processName + "." + method + " : " + message);
		} catch(e) {
			// Ignore any error
		}
	}

	public warning(String method, String message) {
		try {
			logger.warn(processName + "." + method + " : " + message);
		} catch(e) {
			// Ignore any error
		}
	}
	
	public error(String method, String message) {
		try {
			logger.error(processName + "." + method + " : " + message);
		} catch(e) {
			// Ignore any error
		}
	}
	
	public exception(String method, Exception e) {
		try {
			logger.error(processName + "." + method + " : Exception: " + e.message + " - Stack: " + e.stackTrace);
		} catch(ex) {
			// Ignore any error
		}
	}
	
}
