package com.sgdbf.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sgdbf.rest.api.Index;
import com.vega_systems.ws.locprotns.PPREV;
import com.vega_systems.ws.locprotns.SYNTHESE;

import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonDeserialize(builder = Result.ResultBuilder.class)
public class Result {
	private String errorMsg;	// Error message in case of failure, mepty if OK
	private String errorNum;	// Error number in case of failure
	private String id;
	private SYNTHESE[] syntheses;
	// BPM-865 : Use a map to convert numeric values passed as String
	// WAS : private PPREV[] details;
	private HashMap<String, Object>[] details;
	
    public Result(String error_msg, String error_num, String plan_id, SYNTHESE[] list_syntheses) {
    	errorMsg = error_msg;
    	errorNum = error_num;
    	id = plan_id;
    	syntheses = list_syntheses;
    	details = null;
    }
    
    public Result(String error_msg, String error_num, HashMap<String, Object>[] list_details) {
    	errorMsg = error_msg;
    	errorNum = error_num;
    	id = null;
    	syntheses = null;
    	details = list_details;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
    
    public String getErrorNum() {
        return errorNum;
    }
    
    public String getId() {
        return id;
    }
    
    public SYNTHESE[] getSyntheses() {
        return syntheses;
    }
    
    public HashMap<String, Object>[] getDetails() {
        return details;
    }

    @JsonIgnore
    private final LocalDate currentDate = LocalDate.now();

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getName());

    	private String errorMsg;	// Error message in case of failure, mepty if OK
    	private String errorNum;	// Error number in case of failure
    	private String id;
    	private SYNTHESE[] syntheses;
    	// BPM-865 : Use a map to convert numeric values passed as String
    	// WAS : private PPREV[] details;
    	private HashMap<String, Object>[] details;
    	private boolean isSynthesis;
    	
    	public ResultBuilder errorMsg(String error_msg) {
            this.errorMsg =  error_msg;
            return this;
        }
    	
    	public ResultBuilder errorNum(String error_num) {
            this.errorNum =  error_num;
            return this;
        }
    	
    	public ResultBuilder id(String plan_id) {
            this.id =  plan_id;
            return this;
        }
    	
    	public ResultBuilder setError(String error_msg, String error_num) {
            this.errorMsg =  error_msg;
            this.errorNum =  error_num;
            return this;
        }

        public ResultBuilder syntheses(SYNTHESE[] list_syntheses) {
            this.syntheses = list_syntheses;
            return this;
        }
        
        public ResultBuilder setSyntheses(String plan_id, List<SYNTHESE> list_syntheses) {
			int nb_syntheses = list_syntheses.size();
			
			this.isSynthesis = true;
			this.id = plan_id;
			if (nb_syntheses > 0) {
				this.syntheses = new SYNTHESE[nb_syntheses];
				for (int i=0; i<nb_syntheses; i++) {
					SYNTHESE one_synthesis = list_syntheses.get(i);
					this.syntheses[i] = one_synthesis;
				}
			}
            return this;
        }
        
        public ResultBuilder details(HashMap<String, Object>[] list_details) {
            this.details = list_details;
            return this;
        }
        
    	// Parse string as a Double with comma separator for decimal part
    	private Double getDecimalValue(String parameter_value) {
    		Double double_value = null;
    		
    		try {
    			if ((parameter_value != null) && (parameter_value.length() > 0)) {
    				NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    				Number num_value = format.parse(parameter_value);
    				double_value = num_value.doubleValue();
    			}
    		} catch (Exception e) {
    			LOGGER.error("getDecimalValue exception : ", e.toString());
    		}
    		return double_value;
    	}
    	
        private HashMap<String, Object> formatDetail(PPREV one_detail, java.lang.reflect.Field[] fields, int i) throws IllegalArgumentException, IllegalAccessException {
        	HashMap<String, Object> formatted_detail = new HashMap<String, Object>();
        	
            for (java.lang.reflect.Field field : fields) {
            	String field_name = field.getName();
            	field.setAccessible(true);
            	
            	// Ignore fields that are not public or protected
            	int field_modifiers = field.getModifiers();
            	if ((Modifier.isPublic(field_modifiers) || Modifier.isProtected(field_modifiers)) 
            			&& (! Modifier.isStatic(field_modifiers)) ) {
	            	Object field_value = field.get(one_detail);
	            	
	            	// Check if field_name is one of the numeric values
	            	if ( field_name.equalsIgnoreCase("valeurAchat")
	            		|| field_name.equalsIgnoreCase("coutsSAVTotal")
	            		|| field_name.equalsIgnoreCase("engagementReprise")
	            		|| field_name.equalsIgnoreCase("loyerFinancementMensuel")) {
	            		// Convert string to numeric value
	            		Double decimal_value = getDecimalValue(String.valueOf(field_value));
	            		if (decimal_value != null) {
	            			// Conversion OK
	            			formatted_detail.put(field_name, decimal_value);
	            		} else {
	            			// Conversion failed
	            			LOGGER.warn("formatDetail: failed to convert value for field [" + field_name + "]");
	            			formatted_detail.put(field_name, field_value);
	            		}
	            		
	            	} else {
	            		// No conversion required
	            		formatted_detail.put(field_name, field_value);
	            	}
            	} else {
            		if (i==0) {
            			LOGGER.debug("formatDetail: Ignoring non public field [" + field_name + "]");
            		}
            	}
            }        	
        	
        	return formatted_detail;
        }

        public ResultBuilder setDetails(List<PPREV> list_details) throws IllegalArgumentException, IllegalAccessException {
			int nb_details = list_details.size();
			
			this.isSynthesis = false;
			if (nb_details > 0) {
				// Get the list of fields received from Lyre
	        	java.lang.reflect.Field[] fields = PPREV.class.getDeclaredFields();
	        	LOGGER.debug("setDetails: PPREV has " + fields.length + " fields");
	        	
	        	// Create array of result Map
				this.details = (HashMap<String, Object>[]) new HashMap[nb_details];
				
				// Loop through each record
				for (int i=0; i<nb_details; i++) {
					PPREV one_detail = list_details.get(i);
					this.details[i] = formatDetail(one_detail, fields, i);
				}
			}
            return this;
        }
        
        public Result build() {
        	if (isSynthesis) {
        		return new Result(errorMsg, errorNum, id, syntheses);
        	} else {
        		return new Result(errorMsg, errorNum, details);
        	}
        }
    }

    public static ResultBuilder builder() {
        return new ResultBuilder();
    }
}

