package com.sgdbf.pop3p.groovy

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class ValidationDueDateCalculation {
	private static LogFile log_file = null;
	
	public static Long calculateDueDateDelay(com.sgdbf.model.Proposition current_proposition, Map planningMap, int dxx_working_days_delay) {
		def Long delay_in_ms = null;
		
		try {
			
			if ("BUDGET".equalsIgnoreCase(current_proposition.typeWorkflow)) {
				if (planningMap && planningMap.datePresentation){
					def dueDate = parseDatePresentation(planningMap.datePresentation, "yyyy-MM-dd");
					if (dueDate == null) {
						// Failed, try previous format
						dueDate = parseDatePresentation(planningMap.datePresentation,"dd/MM/yyyy");
					}
					if (dueDate == null) {
						error("calculateDueDateDelay", "Due date calculation failed, could not parse Workflow BUDGET datePresentation=[" + planningMap.datePresentation + ']');
					} else {
						LocalDate now = java.time.LocalDate.now()
						Long nowToMillis = now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
							
						Long dueDateToMillis = dueDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
							
						delay_in_ms = dueDateToMillis - nowToMillis;
						debug("calculateDueDateDelay", "Workflow BUDGET with datePresentation=[" + planningMap.datePresentation + '], delayInMs = ' + String.valueOf(delay_in_ms));
					}
				} else {
					debug("calculateDueDateDelay", "Workflow BUDGET with no datePresentation in Planning");
				}
				
			} else {
					// Dxx or DADR
					if (dxx_working_days_delay > 0) {
						def int nb_days_delays = FrenchBusinessDays.getActualNumberOfDaysForDelay(dxx_working_days_delay, LocalDate.now());
						delay_in_ms = nb_days_delays * 24L * 3600L * 1000L;
						debug("calculateDueDateDelay", "Workflow Dxx or DADR with delay in working days=" + String.valueOf(dxx_working_days_delay) + ', delayInMs = ' + String.valueOf(delay_in_ms));
					} else {
						debug("calculateDueDateDelay", "Workflow Dxx or DADR with no delay set");
					}
				}
			
		} catch (Exception e) {
			delay_in_ms = null;
			exception("calculateDueDateDelay", e);
		}

		return delay_in_ms;
	}
	
	private static java.time.LocalDate parseDatePresentation(datePresentation, String expected_format) {
		def java.time.LocalDate local_date = null;
		
		try {
			debug("parseDatePresentation", "Parsing Workflow BUDGET datePresentation=[" + datePresentation + '] with format [' + expected_format + ']');
			DateTimeFormatter df = DateTimeFormatter.ofPattern(expected_format)
			local_date = java.time.LocalDate.parse(datePresentation, df);
		} catch(e) {
			exception("parseDatePresentation (with format=" + expected_format + ")", e);
		}
		return local_date;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("ValidationDueDateCalculation");
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
