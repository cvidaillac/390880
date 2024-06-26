package com.sgdbf.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@JsonDeserialize(builder = Result.ResultBuilder.class)
public class Result {
	@JsonDeserialize(builder = Result.BudgetValueBuilder.class)
    public static class BudgetValue{
    	public String description;
    	public Long amount;
    	public String label;	// Concatenation of description : amount €
    	
    	public BudgetValue(String desc, String amount_string) {
    		description = desc;
    		amount = (amount_string == null) ? 0L : Long.valueOf(amount_string);
    		label = desc + " : " + String.format(Locale.FRANCE, "%,d", amount) + " €";
    	}
    };
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class BudgetValueBuilder {
    	private String description;
    	private String amount;
    	private String label;
    	
        public BudgetValueBuilder description(String description) {
            this.description =  description;
            return this;
        }
        
        public BudgetValueBuilder amount(String amount) {
            this.amount =  amount;
            return this;
        }
        
        public BudgetValueBuilder label(String label) {
            this.label =  label;
            return this;
        }
        
        public BudgetValue build() {
            return new BudgetValue(description, amount);
        }
    	
    };

	private String errorMsg;	// Error message in case of failure, mepty if OK
	private int nbValues;		// Number of suggested values
	private BudgetValue[] suggestedValues;
	
    @JsonIgnore
    private final LocalDate currentDate = LocalDate.now();
    
    public Result(String error_msg, BudgetValue[] suggested_values) {
    	errorMsg = error_msg;
    	nbValues = (suggested_values == null) ? 0 : suggested_values.length;
    	suggestedValues = suggested_values;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getNbValues() {
        return nbValues;
    }

    public BudgetValue[] getSuggestedValues() {
        return suggestedValues;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultBuilder {
    	private String errorMsg;	// Error message in case of failure, mepty if OK
    	private BudgetValue[] suggestedValues;
    	private int nbValues;

        public ResultBuilder errorMsg(String errorMsg) {
            this.errorMsg =  errorMsg;
            return this;
        }
        
        public ResultBuilder nbValues(int nbValues) {
        	this.nbValues = nbValues;
        	return this;
        }

        public ResultBuilder suggestedValues(BudgetValue[] suggestedValues) {
            this.suggestedValues = suggestedValues;
            return this;
        }
        
        public void setSuggestedValuesFromList(List<BudgetValue> list_values) {
        	this.suggestedValues = list_values.toArray(new BudgetValue[0] );
        }

        public Result build() {
        	return new Result(errorMsg, suggestedValues);
        }
    }

    public static ResultBuilder builder() {
        return new ResultBuilder();
    }
}

