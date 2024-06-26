package com.sgdbf.pop3p.groovy

import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO

class BDMUtility {
	def static LogFile log_file=null;
	
	def static boolean updateObjectField(Object bdm_object, String field_name, Object field_value, List<String> upperCaseFieldNames) {
		def boolean is_updated=false;
		def java.lang.reflect.Method method;
		def Map result;
	
		try {
			
			// Get Method
			def String method_name = "set" + field_name.substring(0,1).toUpperCase() + field_name.substring(1);
			def Class object_class = bdm_object.getClass();
			logDebug("Looking for method [" + method_name + "] on object [" + object_class.getName() + "] for value of type [" +  field_value.getClass() + "]");
			result = getMethod(object_class, method_name, field_value);
			method = result["method"];
			field_value = result["field_value"];
	
			// Set uppercase if required
			if (upperCaseFieldNames.contains(field_name) && (field_value instanceof String)) {
				field_value = ((String) field_value).toUpperCase();
				logDebug("Setting uppercase value for field " + field_name);
			}
			
			// Invoke method
			logDebug("Invoking method [" + method_name + "]");
			method.invoke(bdm_object, field_value);
			is_updated=true;
					
		}  catch(e) {
			logError("Exception updateObjectField: " + e.toString());
		}
		
		return is_updated;
	}
	
	
	def static Object getObjectFieldValue(Object bdm_object, String field_name, boolean is_first_record) {
		def Object field_value=null;
		
		try {
			// Check if we need to query a sub-object
			if (field_name.contains("@")) {
				// NOT SUPPORTED YET
				logDebugOnCondition("Query on Sub-object: " + field_name, is_first_record);
				field_value="";
			
			// Check if value is within a sub-object
			} else if (field_name.contains(".")) {
				// Get child object
				def int pos_dot = field_name.indexOf(".");
				def String child_object_name = field_name.substring(0, pos_dot);
				// Check if we have multiple records in child object
				def String record_index="";
				if (child_object_name.contains("[")) {
					def pos_end_object_name = child_object_name.indexOf("[");
					record_index = child_object_name.substring(pos_end_object_name+1, child_object_name.length()-1);
					child_object_name = child_object_name.substring(0, pos_end_object_name);
				}
				def String child_field_name = field_name.substring(pos_dot+1);
				logDebugOnCondition("Getting child object [" + child_object_name + "]", is_first_record);
				def Object child_object_value = getObjectFieldValue(bdm_object, child_object_name, is_first_record);
				
				// get value on child object
				if (child_object_value != null) {
					if (record_index.isEmpty()) {
						logDebugOnCondition("Getting field [" + child_field_name + "] from child object [" + child_object_name + "]", is_first_record);
						field_value = getObjectFieldValue(child_object_value, child_field_name, is_first_record);
					} else {
						// Getting data from record_index record
						def List records_list = (List) child_object_value;
						def int index = Integer.parseInt(record_index) - 1;
						if (index < records_list.size()) {
							logDebugOnCondition("Getting field [" + child_field_name + "] from record " + record_index + " of child object [" + child_object_name + "]", is_first_record);
							def Object target_record = records_list.get(index);
							field_value = getObjectFieldValue(target_record, child_field_name, is_first_record);
						} else {
							logDebugOnCondition("No avaibable record " + record_index + " for child object [" + child_object_name + "]", is_first_record);
						}
					}
				}
	
			} else {
				// Get Method
				def String method_name ="";
				def java.lang.reflect.Method method = null;
				if (field_name.startsWith('is') && field_name.charAt(2).isUpperCase()) {
					// Getter for boolean starts with is
					method_name = field_name;
					method = getObjectFieldGetter(bdm_object, method_name, is_first_record);
				} 
				if (method == null) {
					// Assume get method
					method_name = "get" + field_name.substring(0,1).toUpperCase() + field_name.substring(1);
					method = getObjectFieldGetter(bdm_object, method_name, is_first_record);
				}
				// Did not find get method, try with is for boolean attributes
				if (method == null) {
					method_name = "is" + field_name.substring(0,1).toUpperCase() + field_name.substring(1);
					method = getObjectFieldGetter(bdm_object, method_name, is_first_record);
				}
				if (method != null) {
					// Invoke method
					logDebugOnCondition("Invoking method [" + method_name + "]", is_first_record);
					field_value = method.invoke(bdm_object);
					
				}
			}
			
		}  catch(e) {
			logError("Exception getObjectFieldValue: " + e.toString());
			logError("Exception cause: " + e.getCause().toString());
		}
	
		return field_value;
	}
	
	def static java.lang.reflect.Method getObjectFieldGetter(bdm_object, method_name, boolean is_first_record) {
		def java.lang.reflect.Method method = null;
		
		try {
			def Class object_class = bdm_object.getClass();
			logDebugOnCondition("Looking for method [" + method_name + "] on object [" + object_class.getName() + "]", is_first_record);
			method = object_class.getMethod(method_name);

		} catch(e) {
			logError("Exception getObjectFieldGetter: " + e.toString());
			logError("Exception cause: " + e.getCause().toString());
		}
		return method;
	}
	
	def static List<Object> getObjectRecordValues(Object bdm_object, List<String> fields_list, boolean is_first_record) {
		def List<Object> record_values = new ArrayList();
	
		try {
			def int nb_fields = fields_list.size();
			for (int i=0; i<nb_fields; i++) {
				def Object field_value = getObjectFieldValue(bdm_object, fields_list.get(i), is_first_record);
				record_values.add(field_value);
			}
			
		}  catch(e) {
			logError("Exception getObjectRecordValues: " + e.toString());
		}
	
		return record_values;
	}
	
	def private static Map getMethod(Class object_class, String method_name, Object field_value) {
		java.lang.reflect.Method method=null;
		
		try {
			method = object_class.getMethod(method_name, field_value.getClass());
			
		} catch(java.lang.NoSuchMethodException ex) {
			if ((field_value instanceof Number) || (field_value instanceof String)) {
			} else {
				throw ex;
			}
		}
		
		if (method == null) {
			try {
				Long long_field_value;
				if (field_value instanceof Number) {
					// Try converting Integer Value to Long
					logDebug("Converting Number field value to Long...");
					long_field_value = Long.valueOf(field_value.longValue());
				} else {
					// Try converting String Value to Long
					logDebug("Converting String field value to Long...");
					long_field_value = Long.valueOf(field_value);
				}
				method = object_class.getMethod(method_name, long_field_value.getClass());
				field_value = long_field_value;
				
			} catch(java.lang.NoSuchMethodException ex) {
				method = null;
			}
			
			if (method == null) {
				try {
					Double double_field_value;
					if (field_value instanceof Number) {
						// Try converting Number Value to Double
						logDebug("Converting Number field value to Double...");
						double_field_value = Double.valueOf(field_value.doubleValue());
					} else {
						// Try converting String Value to Double
						logDebug("Converting String field value to Double...");
						double_field_value = Double.valueOf(field_value);
					}
					method = object_class.getMethod(method_name, double_field_value.getClass());
					field_value = double_field_value;
				} catch(java.lang.NoSuchMethodException ex) {
					method=null;
				}
				
				if (method == null) {
					try {
						Float float_field_value;
						if (field_value instanceof Number) {
							// Try converting Number Value to Float
							logDebug("Converting Number field value to Float...");
							float_field_value = Float.valueOf(field_value.floatValue());
						} else {
							// Try converting String Value to Float
							logDebug("Converting String field value to Float...");
							float_field_value = Float.valueOf(field_value);
						}
						method = object_class.getMethod(method_name, float_field_value.getClass());
						field_value = float_field_value;
					} catch(java.lang.NoSuchMethodException ex) {
						logError("Failed to convert Number value, exception: " + ex.toString());
						throw ex;
					}
				}
			}
		}
	
		return ["method": method, "field_value": field_value];
	}
	
	def private static  getMethodFromName(Class object_class, String find_method_name) {
		def java.lang.reflect.Method find_method=null;
		
		try {
			java.lang.reflect.Method[] methods = object_class.getMethods();
			logDebug("Found " + methods.length + " methods");
			for (int i=0; i < methods.length; i++) {
				def java.lang.reflect.Method current_method = methods[i];
				def String current_method_name = current_method.getName();
				//logDebug("Found method [" + current_method_name + "] while searching for method [" + find_method_name + "]");
				if (current_method_name.equals(find_method_name)) {
					logDebug("Found method [" + current_method_name + "]");
					find_method = current_method;
					break;
				}
			}
			
		}  catch(e) {
			logError("Exception getMethodFromName: " + e.toString());
		}
	
		return find_method;
	}
	
	def static Map getFindQuery(String object_name,  String find_method_name, String query_criteria, Map<String, BusinessObjectDAO> dao_objects_map, List<Long> queryListIds) {
		def String error_message="";
		def List find_result=null;
		def BusinessObjectDAO dao_instance = null;
		def java.lang.reflect.Method find_method = null;
		def Object[] method_parameters=null;
		
		try {
			// Get the DAO object
			dao_instance = getBdmObjectDaoInstance(object_name, dao_objects_map);
			if (dao_instance == null) {
				error_message = "Impossible d'exporter un objet de type [" + object_name + "]";
				logError(error_message);
				
			} else {
				logDebug("getFindQuery: Trying to get method [" + find_method_name + "] for object [" + object_name + "]");
				find_method = getMethodFromName(dao_instance.getClass(), find_method_name);
				if (find_method == null) {
					error_message = "Impossible d'effectuer la recherche [" + find_method + "] sur l'object de type [" + object_name + "]";
					logError(error_message);
				} else {
					logDebug("getFindQuery: Calling method [" + find_method_name + "] for object [" + object_name + "]...");
					if (queryListIds.size() > 0) {
						logDebug("getFindQuery: Using the list of " + String.valueOf(queryListIds.size()) + " persistenceIds as method parameter for method [" + find_method_name + "]");
						def array_list_ids = queryListIds  as Long[];
						method_parameters = new java.lang.Object[1];
						def Class[] parameter_types = find_method.getParameterTypes();
						logDebug("getFindQuery: number of parameters = " + String.valueOf(parameter_types.size()));
						logDebug("getFindQuery: type of parameter 1 = " + parameter_types[0].getName());
						method_parameters[0] = array_list_ids;
					} else {
						def result = getFindMethodParameters(dao_instance, find_method, query_criteria);
						method_parameters = result['parameters'];
						error_message =  result['errorMessage'];
					}
				}
			}
	
		}  catch(e) {
			logError("Exception getFindQuery: " + e.toString());
		}
		
		return ['errorMessage' : error_message, "daoInstance": dao_instance, "findMethod": find_method, "parameters": method_parameters];
	}
	
	def private static java.lang.Object getParameter(Class param_type, int param_index, String query_criteria) {
		def java.lang.Object parameter_value = null;
		def String param_value_string="";
		
		try {
			// Get parameter value
			def String search_string = "&f=";
			def int pos_start=0;
			def current_index=0;
			while ((pos_start>=0) && (current_index<=param_index)) {
				pos_start = query_criteria.indexOf(search_string, pos_start);
				current_index++;
				if (pos_start>=0) {
					// Go after f=
					pos_start += search_string.length();
				}
			}
			if ((pos_start < 0) || (current_index==0)) {
				logError("Parameter [" + String.valueOf(param_index) + "] not found in query criteria [" + query_criteria + "]");
			} else {
				// Find beginning of value after param_name=...
				def int param_name_start = pos_start;
				pos_start = query_criteria.indexOf("=", pos_start);
				if (pos_start <0) {
					logError("Start of Parameter [" + String.valueOf(param_index) + "] value not found in query criteria [" + query_criteria + "]");
				} else {
					def String param_name=query_criteria.substring(param_name_start, pos_start);
					logDebug("Found parameter name [" + param_name + "] for query parameter " + String.valueOf(param_index) + " from query criteria [" + query_criteria + "]");
					pos_start += 1;
					def int pos_end = query_criteria.indexOf("&", pos_start);
					if (pos_end < 0) {
						param_value_string = query_criteria.substring(pos_start);
					} else {
						param_value_string = query_criteria.substring(pos_start, pos_end);
					}
					logDebug("Found parameter value [" + param_value_string + "] for parameter [" + param_name + "] with index " + String.valueOf(param_index));
					def String class_name = param_type.getName();
					switch(class_name) {
						case "java.lang.Integer":
							parameter_value = Integer.valueOf(param_value_string);
							break;
							
						case "java.lang.Long":
							parameter_value = Long.valueOf(param_value_string);
							break;
	
						case "java.lang.Boolean":
							parameter_value = Boolean.valueOf(param_value_string);
							break;
	
						case "java.lang.Double":
							parameter_value = Double.valueOf(param_value_string);
							break;
							
						case "java.util.Date":
							parameter_value = StringUtility.parseDate(param_value_string);
							break;
						
						default:
						parameter_value = param_value_string;
					}
				}
			}
			
		}  catch(e) {
			logError("Exception getParameter: " + e.toString());
		}
		
		return parameter_value;
	}
	
	def public static Map executeFindMethod(BusinessObjectDAO dao_instance, java.lang.reflect.Method find_method, java.lang.Object[] parameters, int page_count, int page_size) {
		def List records_found = new ArrayList();
		def String error_message = "";
		
		try {
			// Calculate start_index with page_count and page_size
			def int start_index = page_count * page_size;
			
			// Execute method
			def int nb_parameters = parameters.size();
			switch(nb_parameters) {
				case 0:
					records_found = find_method.invoke(dao_instance, start_index, page_size);
					break;
	
				case 1:
					records_found = find_method.invoke(dao_instance, parameters[0], start_index, page_size);
					break;
					
				case 2:
					records_found = find_method.invoke(dao_instance, parameters[0], parameters[1], start_index, page_size);
					break;
					
				case 3:
					records_found = find_method.invoke(dao_instance, parameters[0], parameters[1], parameters[2], start_index, page_size);
					break;
					
				case 4:
					records_found = find_method.invoke(dao_instance, parameters[0], parameters[1], parameters[2], parameters[3], start_index, page_size);
					break;
					
				case 5:
					records_found = find_method.invoke(dao_instance, parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], start_index, page_size);
					break;
					
				case 6:
					records_found = find_method.invoke(dao_instance, parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], start_index, page_size);
					break;
					
			}
		} catch(java.lang.reflect.InvocationTargetException e) {
			def String cause = e.getCause().toString();
			error_message = "Erreur lors du chargement des enregistrements: " + cause;
			logError("Exception executeFindMethod: " + e.toString() + " - cause: " + cause);
			
		}  catch(e) {
			error_message = "Erreur lors du chargement des enregistrements: " + e.toString();
			logError("Exception executeFindMethod: " + e.toString());
		}
		return ["records": records_found, "errorMessage": error_message];
	}
	
	def private static Map getFindMethodParameters(BusinessObjectDAO dao_instance, java.lang.reflect.Method find_method, String query_criteria) {
		def java.lang.Object[] parameters = null;
		def String error_message = "";
		
		try {
			// Get the method parameters
			def Class[] parameter_types = find_method.getParameterTypes();
			def int nb_parameters = parameter_types.size();
			logDebug("getFindMethodParameters: Found " + nb_parameters + " parameters for method");
			
			if (nb_parameters > 2) {
				def int nb_query_criteria_params = nb_parameters-2;			// The last two parameters are page count and page size
				parameters = new java.lang.Object[nb_query_criteria_params];
				
				for (int i=0; i<nb_query_criteria_params; i++) {
					def Class param_type = parameter_types[i];
					logDebug("getFindMethodParameters: parameter " + String.valueOf(i) + " of type [" + param_type.getName() + "]");
					parameters[i] = getParameter(param_type, i, query_criteria);
				}
			} else {
				logDebug("getFindMethodParameters: No Parameter found");
				parameters = [];
			}
			
		}  catch(e) {
			logError("Exception getFindMethodParameters: " + e.toString());
			error_message = "Erreur lors de la récupération des paramètres: " + e.toString();
		}
		return ['parameters': parameters, 'errorMessage': error_message];
	}
	
	def private static BusinessObjectDAO getBdmObjectDaoInstance(String object_name, Map<String, BusinessObjectDAO> dao_objects_map) {
		def BusinessObjectDAO dao_instance = null;
		
		try {
			logDebug("getBdmObjectDaoInstance: Getting DAO for object [" + object_name + "]");
			
			// Get last part of object name
			def int pos_start = object_name.lastIndexOf(".");
			if (pos_start>0) {
				object_name = object_name.substring(pos_start+1);
			}
			object_name = object_name.toLowerCase();
			
			dao_instance = dao_objects_map.get(object_name);
			
		}  catch(e) {
			logError("Exception getBdmObjectDaoInstance: " + e.toString());
		}
		
		return dao_instance;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("BDMUtility");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebugOnCondition(String message, boolean condition) {
		if (condition) {
			logDebug(message);
		}	
	}
	
	def private static void logDebug(String message) {
		getLogFile().debug("BDMUtility", message);
	}
	
	def private static void logError(String message) {
		getLogFile().error("BDMUtility", message);
	}
	
}
