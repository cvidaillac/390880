package com.sgdbf.pop3p.rest.api

import java.util.Map;

class UtilityFiltering {
	private static final String CONFIG_PARAM_ENABLE_SEARCH_MODELE = "enableWSSearchOnModele";
	private static final String CONFIG_PARAM_ENABLE_SEARCH_MARQUE = "enableWSSearchOnMarque";
	private static final String CONFIG_PARAM_ENABLE_SEARCH_GENRE = "enableWSSearchOnGenre";
	private static final String CONFIG_PARAM_ENABLE_SEARCH_SOUS_GENRE = "enableWSSearchOnSousGenre";

		// CFR 18/05/2022 BPM-814: Add new oarameter to disable energy filtering on Lyre side
	private static final String CONFIG_PARAM_ENABLE_SEARCH_ENERGY = "enableWSSearchOnEnergy";
	
	public static final String FILTER_FIELD_MARQUE = "marque";
	public static final String FILTER_FIELD_MODELE = "modele";
	public static final String FILTER_FIELD_GENRE = "libelleGenre";
	public static final String FILTER_FIELD_SOUSGENRE = "libelleSousGenre";
	
	// CFR 18/05/2022 BPM-814: Add new filter for energy
	public static final String FILTER_FIELD_ENERGY = "codeEnergie";
	
	private static final String MULTIPLE_SELECT_SEPARATOR = "\\Q | \\E";	// \\Q...\\E to avoid special meaning of | in split regexp
	
	// For agency materials
	public static Map < String,	String[] > parsingFiltersParameters(String[]filtersProcess) {
		Map < String, String[] > mapFiltersProcess = new HashMap < String,	String[] > ();

		if (filtersProcess != null) {
			for (e in filtersProcess) {
				String[]parts = e.split("=");
				String key = parts[0];
				String[]values = new String[2];
				values[0] = parts[1];
				if (parts.size() > 2) {
					values[1] = parts[2];
				} else {
					values[1] = "E";
				}
				mapFiltersProcess.put(key, values);
			}
		}
		return mapFiltersProcess;
	}
	
	// For material eligible
	public static Map < String,
	String[] > parsingFiltersParameters(String[]filtersProcess, Map<String, Boolean> enableWSSearch) {
		Map < String, String[] > mapFiltersProcess = new HashMap < String, String[] > ();

		if (filtersProcess != null) {
			for (e in filtersProcess) {
				String[]parts = e.split("=");
				String key = parts[0];
				
				// Check if filtering is done on Web service side or not
				if (! isFilteredByWebService(key, enableWSSearch)) {
					Index.LOGGER.info("pop3pClientSoap-getEligibleMaterials: filtering on [" + key + "] is implemented within REST-API");
					
					String[]values = new String[2];
					values[0] = parts[1];
					if (parts.size() > 2) {
						values[1] = parts[2];
					} else {
						values[1] = "E";
					}
					mapFiltersProcess.put(key, values);
					
				} else {
					Index.LOGGER.info("pop3pClientSoap-getEligibleMaterials: filtering on [" + key + "] is done by Web service");
				}
			}
		}
		return mapFiltersProcess;
	}
	
	
	public static boolean isFilteredByWebService(String key, Map<String, Boolean> enableWSSearch) {
		boolean is_filtered_by_ws = true;
		
		Boolean search_enabled = enableWSSearch.get(key);
		Index.LOGGER.debug(String.format("pop3pClientSoap-getEligibleMaterials: search_enabled=[%s] for field [%s]", String.valueOf(search_enabled), key));
		if ((search_enabled != null) && (search_enabled == false)) {
			is_filtered_by_ws = false;
		}
		
		return is_filtered_by_ws;
	}
	
	public static boolean isOkWithFiltering(Map < String, String[] > mapFiltersProcess, Map taskItem) {
		boolean isValide = true;
		for (Map.Entry < String, String[] > entry: mapFiltersProcess.entrySet()) {
			String field_name = entry.getKey();
			if (taskItem.containsKey(field_name) && isValide) {
				// CFR 18/05/2022 BPM-814 : Decode the search value parameter
				String filterValue =  URLDecoder.decode(entry.getValue()[0], 'UTF-8');
				filterValue = filterValue.toLowerCase()
				String filterOperator = entry.getValue()[1];
				String taskValue = taskItem.get(field_name).toString();
				taskValue = taskValue.toLowerCase()

				if (filterOperator.equals("E")) {
					if (taskValue != filterValue) {
						isValide = false;
					}
				} else if (filterOperator.equals("M")) {
					// CFR 18/05/2022 BPM-814 : Handle multiple select operator for multiple energies selected
					// Split filterValue to get each selected energy
					String[] list_filters = filterValue.split(MULTIPLE_SELECT_SEPARATOR);
					int nb_filters = list_filters.length; 
					if (nb_filters > 0) {
						// Loop through each filter value and check if we get an exact match
						boolean one_value_found = false;
						for (int i=0; i<nb_filters; i++) {
							String one_value = list_filters[i];
							if (taskValue == one_value) {
								one_value_found = true;
								break;
							}
						}
						
						// Check if we got a match
						Index.LOGGER.debug(String.format("pop3pClientSoap-getEligibleMaterials: Filtering on multiple values for field [%s] with value = [%s], comparing with %d filter values = [%s] : matching result=%s", field_name, taskValue, nb_filters, filterValue, String.valueOf(one_value_found)));
						isValide = isValide &&  one_value_found;
					}
					
				} else {
					if (!taskValue.contains(filterValue)) {
						isValide = false;
					}
				}
				if (!isValide) {
					break;
				}

			}
		}
		return isValide;

	}

	public static Map<String, Boolean> getWSEnabledSearches(Properties props) {
		Map<String, Boolean> enabled_searches = new HashMap<String, Boolean>();
		
		enabled_searches.put(FILTER_FIELD_MODELE, isPropertyEnabled(CONFIG_PARAM_ENABLE_SEARCH_MODELE, props));
		enabled_searches.put(FILTER_FIELD_MARQUE, isPropertyEnabled(CONFIG_PARAM_ENABLE_SEARCH_MARQUE, props));
		enabled_searches.put(FILTER_FIELD_GENRE, isPropertyEnabled(CONFIG_PARAM_ENABLE_SEARCH_GENRE, props));
		enabled_searches.put(FILTER_FIELD_SOUSGENRE, isPropertyEnabled(CONFIG_PARAM_ENABLE_SEARCH_SOUS_GENRE, props));
		
		// CFR 18/05/2022 BPM-814: Add new filter for energy
		enabled_searches.put(FILTER_FIELD_ENERGY, isPropertyEnabled(CONFIG_PARAM_ENABLE_SEARCH_ENERGY, props));
		
		return enabled_searches;
	}
	
	public static String getWebServiceFilterValue(String field_name, Map<String, Boolean> enableWSSearch, Map<String, String> filterMap) {
		def String web_service_filter_value = isFilteredByWebService(field_name, enableWSSearch) ? filterMap.get(field_name) : null;
		Index.LOGGER.info("pop3pClientSoap-getEligibleMaterials: web service filter for [" + field_name + "] = [" + ((web_service_filter_value == null) ? "" : web_service_filter_value) + "]");  
		
		return web_service_filter_value;
		
	}

	private static boolean isPropertyEnabled(String property_name, Properties props) {
		boolean is_enabled = true;
		
		// CFR 19/05/2022 BPM-814 : Replace incorrect method getAt with getProperty
		//String property_value = props.getAt(property_name);
		String property_value = props.getProperty(property_name);
		if (property_value != null) {
			Index.LOGGER.debug(String.format("pop3pClientSoap-getEligibleMaterials: configuration property %s = [%s]", property_name, property_value));
			if (property_value.equalsIgnoreCase("false")) {
				is_enabled = false;
			}
		}
			
		return is_enabled;
	}
}
