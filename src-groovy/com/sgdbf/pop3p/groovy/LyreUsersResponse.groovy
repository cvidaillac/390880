package com.sgdbf.pop3p.groovy
import com.sgdbf.pop3p.groovy.LogFile;
import com.sgdbf.pop3p.groovy.LyreUser;
import com.sgdbf.pop3p.groovy.LyreUserProfiles;

import org.w3c.dom.NodeList
import org.w3c.dom.Node

class LyreUsersResponse extends AnalyzeWebServiceResponse implements java.io.Serializable {
	private static LogFile log_file = null;
	
	// Error messages
	private final static String ERROR_NB_USERS = "Error : Lyre web service response soecifies a number of users that does not match the actual number of users in the list";
	
	// Expected XML nodes
	private final static String EXPECTED_RESPONSE_NODE = "WSBP_USERS_BUDGEPOPResult";
	private final static String NODE_NB_USERS = "nbUsers";
	private final static String NODE_USERS = "UTILISATEURS";
	private final static String NODE_PROFILS = "PROFILS";
	
	public String responseSource;
	public String errorMsg;
	public boolean isResponseOk;
	public int nbUsers;
	public List<LyreUser> users;
	private Map<String, List<LyreUser>> mapProfileToUsers;
	private Map<String, LyreUser> mapSgidToUser;
	private LyreUserProfiles lyreProfiles;
	
	public LyreUsersResponse(javax.xml.transform.Source sourceResponse, org.w3c.dom.Document responseDocumentBody, LyreUserProfiles lyre_profiles) {
		lyreProfiles = lyre_profiles;
		responseSource = sourceResponse.getSystemId();
		if (responseSource == null) {
			responseSource = "";
		}
		nbUsers = 0;
		users = new ArrayList<LyreUser>();
		mapProfileToUsers = new HashMap<String, List<LyreUser>>();
		mapSgidToUser= new HashMap<String, LyreUser>();
		
		errorMsg = analyzeResponse(responseDocumentBody,EXPECTED_RESPONSE_NODE);
		isResponseOk =  errorMsg.isEmpty();
	}
	
	public List<LyreUser> getUsersForProfile(String profile_name) {
		return mapProfileToUsers.get(profile_name);
	}
	
	public Map<String, Integer> getMapProfileToNbUsers() {
		def Map<String, Integer> map_profile_to_nbUsers = new HashMap<String, Integer>();
		
		try {
			for (String profile_name : mapProfileToUsers.keySet()) {
				List<LyreUser> list_users = mapProfileToUsers.get(profile_name);
				map_profile_to_nbUsers.put(profile_name, list_users.size());
			}
				
		} catch(e) {
			exception("getMapProfileToNbUsers", e);
		}
		return map_profile_to_nbUsers;
	}
	
	public List<LyreUser> getSmarteUsers() {
		return mapProfileToUsers.get(lyreProfiles.smarteProfileName);
	}
	
	public List<LyreUser> getBoUsers() {
		return mapProfileToUsers.get(lyreProfiles.boProfileName);
		
	}
	
	public List<LyreUser> getLacUsers() {
		return mapProfileToUsers.get(lyreProfiles.lacProfileName);
		
	}
	
	public List<LyreUser> getSavUsers() {
		return mapProfileToUsers.get(lyreProfiles.savProfileName);
	}
	
	public List<LyreUser> getGeemcoUsers() {
		return mapProfileToUsers.get(lyreProfiles.geemcoProfileName);
	}
	
	public List<LyreUser> getValidatorUsers() {
		return mapProfileToUsers.get(lyreProfiles.approverProfileName);
	}
	
	public List<LyreUser> getReadOnlyUsers() {
		return mapProfileToUsers.get(lyreProfiles.readOnlyProfileName);
	}
	
	public List<String> getListOfSgidsToProcess() {
		List<String> list_of_sgids = new ArrayList<String>(); 
		
		list_of_sgids.addAll(mapSgidToUser.keySet());
		return list_of_sgids;
	}
	
	public LyreUser getLyreUserFromSgid(String sgid) {
		return mapSgidToUser.get(sgid);
	}

	protected String analyzeResponseNodes(NodeList nodes_list) {
		String error_msg = "";
		
		try {
			for (Node one_node : nodes_list) {
				String node_name = one_node.getNodeName();
				if (NODE_NB_USERS.equalsIgnoreCase(node_name)) {
					debug("analyzeResponse", "Found node " + node_name);
					String nb_users_value = getElementValue(one_node);
					if (nb_users_value != null) {
						nbUsers = Integer.valueOf(nb_users_value);
						debug("analyzeResponse", "Node " + node_name + " with value [" + String.valueOf(nbUsers) + "]");
					}
					
				} else if (NODE_USERS.equalsIgnoreCase(node_name)) {
					debug("analyzeResponse", "Found node " + node_name);
					def NodeList nodes_users = one_node.getChildNodes();
					for (Node one_user_node : nodes_users) {
						def String error_user = addUser(one_user_node);
						if (!error_user.isEmpty()) {
							error_msg += error_user + ". ";
						}
					}
				}
			}
					
			// Check the list and number of users are consistent
			def int list_users_size = users.size();
			if (nbUsers != list_users_size) {
				error_msg += ERROR_NB_USERS + "(" + String.valueOf(nbUsers) + " vs " + String.valueOf(list_users_size) + ")";
			}
			
		} catch(e) {
			exception("analyzeResponse", e);
			error_msg = AnalyzeWebServiceResponse.ERROR_EXCEPTION + " : " + e.getMessage();
		}
		info("analyzeResponse", "Returning result: [" + error_msg + "]");
		return error_msg;
	}

	private String addUser(Node one_user_node) {
		String error_msg = "";
		boolean user_has_one_expected_profile = false;
		
		try {
			def LyreUser new_user = new LyreUser();
			def NodeList user_fields = one_user_node.getChildNodes();
			for (Node one_field : user_fields) {
				def String field_name = one_field.getNodeName();
				// debug("addUser", "Found node [" + field_name + "]");
				if (NODE_PROFILS.equalsIgnoreCase(field_name)) {
					// Get the list of profiles : PROFILS -> PROFIL -> profil
					def NodeList user_profiles = one_field.getChildNodes();
					for (Node one_profile : user_profiles) {
						def Node node_profile_name = one_profile.getFirstChild();
						if (node_profile_name != null) {
							// Add the profile to the user
							def String profile_name = getElementValue(node_profile_name);
							if ((profile_name != null) && (! profile_name.isEmpty())) {
								// Add the profile to the user
								debug("addUser", "Found profile " + profile_name);
								new_user.addProfile(profile_name);
									
								// Check the profile is one we need to process
								if (! lyreProfiles.getListProfileNames().contains(profile_name) ){
									debug("addUser", "Ignoring profile [" + profile_name + "] for user [" + new_user.getSgid() + "]");
										
								} else {
									// This user will need to be created/updated
									user_has_one_expected_profile = true;
									
									// Add the user to the map : profile -> users list
									List<LyreUser> list_users_for_profile = mapProfileToUsers.get(profile_name);
									if (list_users_for_profile == null) {
										list_users_for_profile = new ArrayList<LyreUser>();
										mapProfileToUsers.put(profile_name, list_users_for_profile);
									}
									list_users_for_profile.add(new_user);
								}
							}
							
						}
					}
					
				} else {
					// Add user field
					def String field_value = getElementValue(one_field);
					debug("addUser", "Field [" + field_name + "] with value [" + field_value + "]");
					new_user.addField(field_name, field_value);
				}
			} // end for
			
			// Add user to the list of all users
			users.add(new_user);
			
			def String sgid = new_user.getSgid();
			debug("addUser", "Found user with sgid [" + sgid + "]");
			if (user_has_one_expected_profile) {
				if ((sgid != null) && (!sgid.isEmpty())) {
					// Add user to the Map of
					mapSgidToUser.put(sgid, new_user);
				}
			} else {
				if (sgid != null) {
					debug("addUser", "Ignoring user [" + sgid + "] as it does not have any of the expected profiles");
				}
			}
			
		} catch(e) {
			exception("addUser", e);
			error_msg = ERROR_EXCEPTION + " : " + e.getMessage();
		}
		if (error_msg.length() > 0) {
			error("addUser", "Returning error: [" + error_msg + "]");
		}
		return error_msg;
	}
	
	protected LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("LyreUsersResponse");
		}
		
		return log_file;
	}
	


}
