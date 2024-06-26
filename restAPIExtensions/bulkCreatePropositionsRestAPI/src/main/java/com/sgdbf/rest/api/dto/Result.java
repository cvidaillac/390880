package com.sgdbf.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(builder = Result.ResultBuilder.class)
public class Result {
    private final Long[] caseIds;
    private String[] errors;
    private String message;
    
    @JsonIgnore
    private final LocalDate currentDate = LocalDate.now();
    
    public Result(Long[] caseIds, String[] errors) {
        this.caseIds = caseIds;
        this.errors = errors;
        this.message = ((errors == null) || (errors.length==0)) ? "" : errors.toString();
    }

    public Long[] getCaseIds() {
        return caseIds;
    }
    
    public String[] getErrors() {
    	return errors;
    }
    
    public String getMessage() {
    	return message;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultBuilder {
        private Long[] caseIds;
        private String[] errors;
        
        public ResultBuilder setCasesFromList(List<Long> list_cases) {
            this.caseIds =  list_cases.toArray(new Long[0] );
            return this;
        }
        
        public ResultBuilder setErrorsFromList(List<String> list_errors) {
            this.errors =  list_errors.toArray(new String[0] );
            return this;
        }

        public Result build() {
            return new Result(caseIds, errors);
        }
    }

    public static ResultBuilder builder() {
        return new ResultBuilder();
    }
}

