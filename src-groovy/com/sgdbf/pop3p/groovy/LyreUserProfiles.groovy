package com.sgdbf.pop3p.groovy

import com.bonitasoft.engine.api.APIAccessor
import com.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.identity.Group
import org.bonitasoft.engine.identity.Role

class LyreUserProfiles implements java.io.Serializable {
	private static LogFile log_file = null;
	
	public final String smarteProfileName;
	public final String approverProfileName;
	public final String boProfileName;
	public final String lacProfileName;
	public final String savProfileName;
	public final String geemcoProfileName;
	public final String readOnlyProfileName;
	public Map<String, List<String>> mapProfileNames;
	public Map<String, Long> mapGroupNameToGroupId;
	public Map<String, Long> mapRoleNameToRoleId;
	public Map<String, Long> mapProfileNameToGroupId;
	public Map<String, Long> mapProfileNameToRoleId;
	public String errorMissingGroupOrRole;
	
	public LyreUserProfiles(String lyreProfileSmarte, String lyreProfileApprover, String lyreProfileBo, String lyreProfileLac, String lyreProfileSav, String lyreProfileGeemco, String lyreProfileReadOnly,
							String bonitaMembershipSmarte, String bonitaMembershipApprover, String bonitaMembershipBo, String bonitaMembershipLac, String bonitaMembershipSav, String bonitaMembershipGeemco, String bonitaMembershipReadOnly,
							APIAccessor apiAccessor) {
		smarteProfileName = lyreProfileSmarte;
		approverProfileName = lyreProfileApprover;
		boProfileName = lyreProfileBo;
		lacProfileName = lyreProfileLac;
		savProfileName = lyreProfileSav;
		geemcoProfileName = lyreProfileGeemco;
		readOnlyProfileName = lyreProfileReadOnly;
		
		// Create map for groups and 
		mapProfileNameToGroupId = new  HashMap<String, Long>();
		mapProfileNameToRoleId = new  HashMap<String, Long>();
		mapGroupNameToGroupId = new  HashMap<String, Long>();
		mapRoleNameToRoleId = new  HashMap<String, Long>();
		
		// Get identity API
		IdentityAPI identity_api = apiAccessor.getIdentityAPI();
		
		// Initialize error message on group or role configuration
		errorMissingGroupOrRole = "";
		
		// Build the array of active profiles
		mapProfileNames = new HashMap<String, List<String>>();
		addToProfiles(smarteProfileName, bonitaMembershipSmarte, identity_api);
		addToProfiles(approverProfileName, bonitaMembershipApprover, identity_api);
		addToProfiles(boProfileName, bonitaMembershipBo, identity_api);
		addToProfiles(lacProfileName, bonitaMembershipLac, identity_api);
		addToProfiles(savProfileName, bonitaMembershipSav, identity_api);
		addToProfiles(geemcoProfileName, bonitaMembershipGeemco, identity_api);
		addToProfiles(readOnlyProfileName, bonitaMembershipReadOnly, identity_api);
	}
	
	public String getErrorOnGroupOrRoleConfiguration() {
		return errorMissingGroupOrRole;
	}
	
	public boolean hasErrorOnGroupOrRoleConfiguration() {
		return ! errorMissingGroupOrRole.isEmpty();
	}
	
	public Long getGroupIdForProfile(String profile_name) {
		return mapProfileNameToGroupId.get(profile_name);
	}
	
	public Long getRoleIdForProfile(String profile_name) {
		return mapProfileNameToRoleId.get(profile_name);
	}
	
	public String getGroupNameForProfile(String profile_name) {
		String group_name = "";
		
		List<String> group_and_profile = mapProfileNames.get(profile_name);
		if ((group_and_profile != null) && (group_and_profile.size()>=1)) {
			group_name = group_and_profile.get(0);
		}
		
		return group_name;
	}

	public String getRoleNameForProfile(String profile_name) {
		String role_name = "";
		
		List<String> group_and_profile = mapProfileNames.get(profile_name);
		if ((group_and_profile != null) && (group_and_profile.size()>=2)) {
			role_name = group_and_profile.get(1);
		}
		
		return role_name;
	}

	private addToProfiles(String profile_name, String bonita_membership, IdentityAPI identity_api) {
		if ((profile_name != null) && (! profile_name.isEmpty())
			&& (bonita_membership != null) && (! bonita_membership.trim().isEmpty())) {
			String[] split_membership = bonita_membership.split(":");
			if (split_membership.size() >= 2) {
				// Get group and role name
				def String group_name = split_membership[0].trim();
				def String role_name = split_membership[1].trim();
				def List<String> group_and_role = new ArrayList();
				group_and_role.add(group_name);
				group_and_role.add(role_name);
				mapProfileNames.put(profile_name, group_and_role);
				
				// Get group id
				addGroupIdForProfile(profile_name, group_name, identity_api);
				
				// Get role id
				addRoleIdForProfile(profile_name, role_name, identity_api);
			}
		}
	}
	
	private void addGroupIdForProfile(String profile_name, String group_name, IdentityAPI identity_api) {
		try {
			def Long group_id = mapGroupNameToGroupId.get(group_name);
			if (group_id == null) {
				// Search for the group
				Group user_group = identity_api.getGroupByPath(group_name);
				group_id = user_group.getId();
				info("addGroupIdForProfile", "Found groupId " + String.valueOf(group_id) + " for group " + group_name + " associated to profile " + profile_name);
				
				// Add new group to map name->id
				mapGroupNameToGroupId.put(group_name, group_id);
			}
			
			// Add group id to map profile->groupId
			mapProfileNameToGroupId.putAt(profile_name, group_id);
			
		} catch(e) {
			exception("addGroupIdForProfile (profile=" + profile_name + ", group=" + group_name + ")", e);
			errorMissingGroupOrRole += "Group [" + group_name + "] configured for profile [" + profile_name + "] does not exist in Bonita. <BR>";
		}
	}
	
	private void addRoleIdForProfile(String profile_name, String role_name, IdentityAPI identity_api) {
		try {
			def Long role_id = mapRoleNameToRoleId.get(role_name);
			if (role_id == null) {
				// Search for the group
				Role user_role = identity_api.getRoleByName(role_name);
				role_id = user_role.getId();
				info("addRoleIdForProfile", "Found roleId " + String.valueOf(role_id) + " for role " + role_name + " associated to profile " + profile_name);
				
				// Add new group to map name->id
				mapRoleNameToRoleId.put(role_name, role_id);
			}
			
			// Add group id to map profile->groupId
			mapProfileNameToRoleId.putAt(profile_name, role_id);
			
		} catch(e) {
			exception("addRoleIdForProfile (profile=" + profile_name + ", group=" + role_name + ")", e);
			errorMissingGroupOrRole += "Role[" + role_name + "] configured for profile [" + profile_name + "] does not exist in Bonita. <BR>";
		}
	}
	
	public List<String> getListProfileNames() {
		return new ArrayList<String>(mapProfileNames.keySet());
	}
	
	public List<String> getGroupAndRoleForProfile(String profile_name) {
		return mapProfileNames.get(profile_name);
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("LyreUserProfiles");
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
	
	private static void info(String method, String message) {
		try {
			log_file = getLogFile();
			log_file.info(method, message);
			
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
