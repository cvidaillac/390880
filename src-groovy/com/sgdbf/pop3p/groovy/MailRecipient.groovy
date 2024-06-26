package com.sgdbf.pop3p.groovy

import com.bonitasoft.engine.api.APIAccessor
import com.bonitasoft.engine.api.IdentityAPI
import com.sgdbf.pop3p.groovy.LogFile;
import org.bonitasoft.engine.identity.ContactData
import org.bonitasoft.engine.identity.User

class MailRecipient {
	private static LogFile log_file = null; 
	
	public static String secureEmailToRecipient(String email_to, boolean test, String defaultToRecipientWhenMissing, String testRecipientsList) {
		def String to_recipient = "";
		
		try {
			if ((email_to == null) || (email_to.isEmpty())) {
				error("secureEmailToRecipient", "No toRecipient found, sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;

			} else if (test) {
				to_recipient = testRecipientsList;
				debug("secureEmailToRecipient", "Test environment, using test recipients : " + testRecipientsList);
			} else {
				to_recipient = email_to;
			}
			
		} catch(e) {
			exception("secureEmailToRecipient", e);
			
		} finally {
			if (to_recipient.isEmpty()) {
				error("secureEmailToRecipient", "No toRecipient found, sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;
			}
		}
		
		return to_recipient;
	}
	
	public static String secureEmailCcRecipient(String email_cc, boolean test, String testRecipientsList) {
		def String to_recipient = "";
		
		try {
			if (test) {
				to_recipient = testRecipientsList;
				debug("secureEmailCcRecipient", "Test environment, using test recipients : " + testRecipientsList);
			} else {
				to_recipient = email_cc;
			}
			
		} catch(e) {
			exception("secureEmailCcRecipient", e);
			
		} 
		
		return to_recipient;
	}
	
	public static String getToRecipientFromUserName(String user_name, boolean test, String defaultToRecipientWhenMissing, String testRecipientsList, APIAccessor apiAccessor) {
		def String to_recipient = "";
		
		try {
			debug("getToRecipientFromUserName", "Searching email for user [" + user_name + "]");
			to_recipient = getEmailFromUserName(user_name, apiAccessor);
			
			if ((to_recipient == null) || (to_recipient.isEmpty())) {
				error("getToRecipientFromUserName", "Email not found for user [" + user_name + "], sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;

			} else if (test) {
				to_recipient = testRecipientsList;
				debug("getToRecipientFromUserName", "Test environment, using test recipients : " + testRecipientsList);
			}
						
		} catch(e) {
			exception("getToRecipientFromUserName", e);
			
		} finally {
			if (to_recipient.isEmpty()) {
				error("getToRecipientFromUserName", "Email not found for user [" + user_name + "], sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;
			}
		}
		
		return to_recipient;
	}

	public static String getToRecipientFromUserId(long user_id, boolean test, String defaultToRecipientWhenMissing, String testRecipientsList, APIAccessor apiAccessor) {
		def String to_recipient = "";
		
		try {
			debug("getToRecipientFromUserId", "Searching email for user id [" + user_id + "]");
			to_recipient = getEmailFromUserId(user_id, apiAccessor.getIdentityAPI());
			
			if ((to_recipient == null) || (to_recipient.isEmpty())) {
				error("getToRecipientFromUserId", "Email not found for user id [" + user_id + "], sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;
				
			} else if (test) {
				to_recipient = testRecipientsList;
				debug("getToRecipientFromUserId", "Test environment, using test recipients : " + testRecipientsList);
			}
			
		} catch(e) {
			exception("getToRecipientFromUserId", e);
			
		} finally {
			if (to_recipient.isEmpty()) {
				error("getToRecipientFromUserId", "Email not found for user id [" + user_id + "], sending to default recipient : [" + defaultToRecipientWhenMissing + "]");
				to_recipient = defaultToRecipientWhenMissing;
			}
		}
		
		return to_recipient;	
	}
	
	public static String getCcRecipientFromUserName(String user_name, boolean test, String testRecipientsList, APIAccessor apiAccessor) {
		def String cc_recipient = "";
		
		try {
			debug("getCcRecipientFromUserName", "Searching email for user [" + user_name + "]");
			if (test) {
				cc_recipient = testRecipientsList;
				debug("getCcRecipientFromUserName", "Test environment, using test recipients : " + testRecipientsList);
			} else {
				cc_recipient = getEmailFromUserName(user_name, apiAccessor);
			}
			
		} catch(e) {
			exception("getCcRecipientFromUserName", e);
			
		}
		
		return cc_recipient;
	}

	public static String getCcRecipientFromUserId(String user_id, boolean test, String testRecipientsList, APIAccessor apiAccessor) {
		def String cc_recipient = "";
		
		try {
			debug("getCcRecipientFromUserId", "Searching email for user [" + user_id + "]");
			if (test) {
				cc_recipient = testRecipientsList;
				debug("getCcRecipientFromUserId", "Test environment, using test recipients : " + testRecipientsList);
			} else {
				cc_recipient = getEmailFromUserId(user_id, apiAccessor.getIdentityAPI());
			}
			
		} catch(e) {
			exception("getCcRecipientFromUserId", e);
			
		}
		
		return cc_recipient;
	}

	public static String getEmailFromUserName(String user_name, APIAccessor apiAccessor) {
		def String user_email = "";
		
		try {
			// CFR 03/12/2020 - LOT2 Sprint 2 : trim username as Lyre sometimes sends spaces at the end...
			def String trimmed_user_name = user_name.trim();
			
			// Get user from username (SGID)
			IdentityAPI identity_api = apiAccessor.getIdentityAPI();
			User user = identity_api.getUserByUserName(trimmed_user_name);

			// Get user email
			user_email = getEmailFromUserId(user.id, identity_api);
			debug("getEmailFromUserName", "Found email [" + user_email + "] for user [" + user_name + "] trimmed as [" + trimmed_user_name + "] with id " + String.valueOf(user.id));
			
		} catch(org.bonitasoft.engine.identity.UserNotFoundException ex) {
			error("getEmailFromUserName", "User not found for user name [" + user_name + "]");
			
		} catch(e) {
			exception("getEmailFromUserName", e);
			
		}
		return user_email;
	}

	public static String getEmailFromUserId(long user_id, IdentityAPI identity_api) {
		def String user_email = "";
		
		try {
			// Get user email
			ContactData professional_data = identity_api.getUserContactData(user_id, false); // false : not personal data
			user_email = professional_data.getEmail();
			debug("getEmailFromUserId", "Found email [" + user_email + "] for user id [" + user_id + "] with id " + String.valueOf(user_id));
			
		} catch(org.bonitasoft.engine.identity.UserNotFoundException ex) {
			error("getEmailFromUserId", "User not found for user id [" + String.valueOf(user_id) + "]");
			
		} catch(e) {
			exception("getEmailFromUserId", e);
			
		}
		return user_email;
	}

	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("MailRecipient");
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
