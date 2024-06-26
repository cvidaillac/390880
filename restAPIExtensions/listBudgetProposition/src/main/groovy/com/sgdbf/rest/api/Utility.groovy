package com.sgdbf.rest.api


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.List;


class Utility {
	
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft");
	
	
	
	public static void filter (List<String> filters, String complement) {
		LOGGER.info("TEST : IN");
		
		
		
		if(filters != null) {
			LOGGER.info("SIZE : " + filters.size());
		}
		

	}
		
	public static String addFilterToSql(List<String> filters, String complement) {
		LOGGER.info("TEST : IN");
		
		
		if(filters != null) {
			LOGGER.info("SIZE : " + filters.size());
		}
		
		/*
		
				
		
		try {
			if (filters  != null) {
				for(e in filters){
					LOGGER.error("E : " + e);
					String[] parts = e.split("=");
					String key = parts[0];
					String value = parts[1];
					if (parts.size() > 2){
						String operator = parts[2];
						switch (operator) {
							case 'L':
								strSQLWhere.append(" AND p."+key+" LIKE '%").append(value).append("%'");
								break;
							case 'E':
								strSQLWhere.append(" AND p."+key+"='").append(value).append("'");
								break;
							default :
								strSQLWhere.append(" AND p."+key+" "+ operator + "'").append(value).append("'");
								break;
						}
					} else {
						strSQLWhere.append(" AND p."+key+"='").append(value).append("'");
					}
				}
			}
		} catch (Exception e) {

		}	
		*/
		
		//LOGGER.info("TEST : OUT : " + strSQLWhere.length());
		
		return "TETS"; //myStringBuilder.toString();
	}
			
}

