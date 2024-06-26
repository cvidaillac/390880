package com.sgdbf.pop3p.groovy

import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserNotFoundException

class LyreUser implements java.io.Serializable  {
	private final static String FIELD_SGID = "sgid";
	private final static String FIELD_EMAIL = "mail";
	private final static String FIELD_FIRSTNAME = "prenom";
	private final static String FIELD_LASTNAME = "nom";
	
	private Map<String, String> userFields;
	private Set<String> profiles;
	
	public LyreUser() {
		userFields = new HashMap<String, String>();
		profiles = new HashSet<String>();
	}
	
	public void addField(String field_name, String field_value) {
		userFields.put(field_name, field_value);
	}
	
	public void addProfile(String profile) {
		profiles.add(profile);
	}
	
	public List<String> getProfiles() {
		return new ArrayList(profiles);
	}
	
	public String getSgid() {
		return userFields.get(FIELD_SGID);
	}
	
	public String getEmail() {
		return userFields.get(FIELD_EMAIL);
	}
	
	public String getFirstname() {
		return userFields.get(FIELD_FIRSTNAME);
	}
	
	public String getLastname() {
		return userFields.get(FIELD_LASTNAME);
	}
	
	public String getFullname() {
		def String fullname = "";
		
		def String first_name = getFirstname();
		def String last_name = getLastname();
		if ((first_name != null) && (! first_name.isEmpty())) {
			fullname += first_name + " ";
		}
		if ((last_name != null) && (! last_name.isEmpty())) {
			fullname += last_name;
		}

		return fullname;
	}
	
	public boolean hasValidEmail() {
		def boolean is_valid = false;
		
		def String email = getEmail();
		if ((email != null) && (!email.isEmpty())) {
			is_valid = true;
		}
		
		return is_valid;
	}
	
	public org.bonitasoft.engine.identity.User searchBonitaUser(org.bonitasoft.engine.api.IdentityAPI identityAPI, LogFile log_file) {
		def String sgid = "";
		def User bonita_user = null;
		
		try {
			sgid = getSgid();
			if ((sgid != null) && (! sgid.isEmpty())) {
				bonita_user = identityAPI.getUserByUserName(sgid);
			} else {
				log_file.error("LyreUser.searchBonitaUser", "Lyre user with no SGID !");
			}
		} catch (UserNotFoundException ex) {
			log_file.info("LyreUser.searchBonitaUser", "Lyre user with SGID [" + sgid + "] not found in Bonita");

		} catch(e) {
			log_file.exception("LyreUser.searchBonitaUser", e);
		}
		
		return bonita_user;
	}
}
