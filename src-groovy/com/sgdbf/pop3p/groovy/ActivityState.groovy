package com.sgdbf.pop3p.groovy
import com.bonitasoft.engine.api.ProcessAPI
import com.sgdbf.pop3p.groovy.LogFile;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance

class ActivityState {

	public static String getActivityState(long failedTaskId, String taskStateNotFound, ProcessAPI process_api, LogFile log_file) {
		def String task_state ="";
		
		try {
			log_file.debug("getActivityState(" + String.valueOf(failedTaskId) + ")", "Checking current state for task");
			task_state = process_api.getActivityInstanceState(failedTaskId);
			log_file.info("getActivityState(" + String.valueOf(failedTaskId) + ")", "Current activity state is [" + task_state + "]");
		
		} catch (org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException ex) {
			log_file.error("getActivityState(" + String.valueOf(failedTaskId) + ")", "Activity instance no longer exists, searching archived activity");
			task_state = searchArchivedActivityState(failedTaskId, taskStateNotFound, process_api, log_file);
			
		} catch(e) {
			log_file.exception("getActivityState(" + String.valueOf(failedTaskId) + ")", e);
		}
		
		return task_state;
	}
	
	public static String searchArchivedActivityState(long failedTaskId, String taskStateNotFound, ProcessAPI process_api, LogFile log_file) {
		def String task_state = "";
		
		try {
			ArchivedActivityInstance archived_task = process_api.getArchivedActivityInstance(failedTaskId);
			if (archived_task != null) {
				task_state = archived_task.getState();
				log_file.info("searchArchivedActivityState(" + String.valueOf(failedTaskId) + ")", "Archived activity state is [" + task_state + "]");
				
			} else {
				log_file.error("searchArchivedActivityState(" + String.valueOf(failedTaskId) + ")", "Null Archived Activity instance");
				task_state = taskStateNotFound;
		
			}
		} catch (org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException ex) {
			log_file.error("searchArchivedActivityState(" + String.valueOf(failedTaskId) + ")", "Archived Activity instance not found");
			task_state = taskStateNotFound;
			
		} catch(e) {
			task_state = taskStateNotFound;
			log_file.exception("searchArchivedActivityState(" + String.valueOf(failedTaskId) + ")", e);
		}
		
		return task_state;
	}
}
