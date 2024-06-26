package com.sgdbf.pop3p.rest.api

import java.util.Map;

class UtilityFiltering {
	
	public static Map < String,
	String[] > parsingFiltersParameters(String[]filtersProcess) {
		Map < String,
				String[] > mapFiltersProcess = new HashMap < String,
				String[] > ();

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
	
	
	private static boolean isOkWithFiltering(Map < String, String[] > mapFiltersProcess, Map taskItem) {
		boolean isValide = true;
		for (Map.Entry < String, String[] > entry: mapFiltersProcess.entrySet()) {
			if (taskItem.containsKey(entry.getKey()) && isValide) {
				String filterValue = entry.getValue()[0];
				filterValue = filterValue.toLowerCase()
				String filterOperator = entry.getValue()[1];
				String taskValue = taskItem.get(entry.getKey()).toString();
				taskValue = taskValue.toLowerCase()

				if (filterOperator.equals("E")) {
					if (taskValue != filterValue) {
						isValide = false;
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

	
	
}
