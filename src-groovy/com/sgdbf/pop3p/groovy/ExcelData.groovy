package com.sgdbf.pop3p.groovy
import java.text.*;

class ExcelData {
	def static LogFile log_file=null;
	
	def static Object getObjectFieldValue(List<Object> rowContent, String field_name,  Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def Object field_value=null;
		
		try {
			String field_label = fieldsMap.get(field_name);
			if (field_label == null) {
				logError("Field [" + field_name + "] not found in fields definition");
				
			} else {
				int field_index = fieldsList.indexOf(field_label);
				if (field_index<0) {
					logError("Field [" + field_name + "] with label [" + field_label + "] not found in fields definition");
					
				} else if (field_index >= rowContent.size()) {
					logDebug("No value for field [" + field_name + "] in import file");
					
				} else {
					field_value = rowContent.get(field_index);
				}
			}
		} catch(e) {
			logError("getObjectFieldValue-Exception: " + e.toString());
			throw e;
		}
		
		return field_value;
	
	}
	
	def static String getTextFieldValue(List<Object> rowContent, String field_name,  Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def String field_value=null;
		
		try {
			Object field_value_obj = getObjectFieldValue(rowContent, field_name, fieldsMap, fieldsList)
			if (field_value_obj != null) {
				field_value = field_value_obj.toString();
			}
			
		} catch(e) {
			logError("getTextFieldValue-Exception: " + e.toString());
			throw e;
		}
		
		return field_value;
	}
	
	
	def static Boolean getBooleanFieldValue(List<Object> rowContent, String field_name,  Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def Boolean field_value=null;
		
		try {
			String field_value_string = getUpperCaseFieldValue(rowContent, field_name, fieldsMap, fieldsList);
			if (field_value_string != null) {
				if ( field_value_string.equals("TRUE")
					|| field_value_string.equals("OUI")
					|| field_value_string.equals("YES")
					|| field_value_string.equals("O")
					|| field_value_string.equals("Y")) {
					field_value=true;
				} else {
					logDebug("Boolean value [" + field_value_string + "] for field [" + field_name + "] = false");
					field_value=false;
				}
			}
			
		} catch(e) {
			logError("getBooleanFieldValue-Exception: " + e.toString());
			throw e;
		}
		
		return field_value;
	}
	
	def static String getUpperCaseFieldValue(List<Object> rowContent, String field_name, Map<String, String> fieldsMap, List<String> fieldsList)
	{
		String field_value_string = null;
		
		try {
			field_value_string = getTextFieldValue(rowContent, field_name, fieldsMap, fieldsList);
			if (field_value_string != null) {
				field_value_string = field_value_string.toUpperCase();
			}
			
		} catch(e) {
			logError("getUpperCaseFieldValue-Exception: " + e.toString());
			throw e;
		}
		return field_value_string;
	}
	
	def static String getLovFieldValue(List<Object> rowContent, String field_name,  String lov_type, Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def String field_value="";
	
		try {
			field_value = getUpperCaseFieldValue(rowContent, field_name, fieldsMap, fieldsList);
			
		} catch(e) {
			logError("getLovFieldValue-Exception: " + e.toString());
			throw e;
		}
	
		return field_value;
	}
	
	def static String getLovFieldValue(List<Object> rowContent, String field_name,  Map<String,String> lovTypes, Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def String field_value="";
		def String lov_type="";
	
		try {
			lov_type = lovTypes.get(field_name);
			if (lov_type != null) {
				logDebug("Found LOV Type [" + lov_type + "] for field [" + field_name + "]");
			} else {
				logError("LOV type not found for field [" + field_name + "]");
			}
			field_value = getLovFieldValue(rowContent, field_name, lov_type, fieldsMap, fieldsList);
			
		} catch(e) {
			logError("getLovFieldValue-Exception: " + e.toString());
			throw e;
		}
	
		return field_value;
	}
	
	def static Date getDateFieldValue(List<Object> rowContent, String field_name, Map<String, String> fieldsMap, List<String> fieldsList)
	{
		def Date field_value=null;
		
		try {
			Object field_value_obj = getObjectFieldValue(rowContent, field_name, fieldsMap, fieldsList)
			if (field_value_obj != null) {
				if (field_value_obj instanceof Date) {
					field_value = (Date) field_value_obj;
				} else if (field_value_obj instanceof String) {
					if (field_value_obj.length() > 0) {
						logDebug("Trying to parse date field value [" + "] with format dd/MM/yyyy ...");
						DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						field_value = df.parse(field_value_obj);
					}
				} else {
					logError("Invalid date value [" + field_value_obj.toString() + "] with type " + field_value_obj.getClass().toString());
				}
			}
	
		} catch(e) {
			logError("getDateFieldValue-Exception: " + e.toString());
		}
		
		return field_value;
	}
	
	def static Number getNumericFieldValue(List<Object> rowContent, String field_name,  Map<String, String> fieldsMap, List<String> fieldsList) {
		Number numeric_value=null;
		
		try {
			Object field_value_obj = getObjectFieldValue(rowContent, field_name, fieldsMap, fieldsList)
			if (field_value_obj != null) {
				if (field_value_obj instanceof Date) {
					Date date_obj = (Date) field_value_obj;
					java.util.Calendar cal = Calendar.getInstance();
					cal.setTime(date_obj);
					numeric_value = new Integer(cal.get(cal.YEAR));
					logDebug("Extracted year value " + String.valueOf(numeric_value.intValue()) + "from date value " + date_obj.toString());
					
				} else {
					String field_value =  field_value_obj.toString();
					if (!isEmpty(field_value)) {
						if (field_value.isInteger()) {
							numeric_value = Integer.valueOf(field_value);
						} else if (field_value.isDouble()) {
							numeric_value = Double.valueOf(field_value);
						} else {
							throw new Exception("Invalid numeric value <" + field_value + ">");
						}
					}
				}
			}
			
		} catch(e) {
			logError("getNumericFieldValue-Exception: " + e.toString());
			throw e;
		}
		
		return numeric_value;
	}
	
	def static boolean isEmpty(Object field_value) {
		boolean is_empty=false;
		
		if (field_value == null) {
			is_empty = true;
			
		} else if (field_value instanceof String) {
			if (field_value.length()==0) {
				is_empty = true;
			}
		}
		
		return is_empty;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("ExcelData");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebug(String message) {
		getLogFile().debug("ExcelData", message);
	}
	
	def private static void logError(String message) {
		getLogFile().error("ExcelData", message);
	}
}