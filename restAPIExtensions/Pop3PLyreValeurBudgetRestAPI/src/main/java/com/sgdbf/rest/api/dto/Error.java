package com.sgdbf.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Error.ErrorBuilder.class)
public class Error {
    @JsonIgnore
    private final String name = "error";
    private final String errorMsg;

    public Error(String message) {
        this.errorMsg = message;
    }

    public String getName() {
        return name;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ErrorBuilder {

        private String errorMsg;

        public ErrorBuilder errorMsg(String message) {
            this.errorMsg = message;
            return this;
        }

        public Error build() {
            return new Error(errorMsg);
        }

    }

    public static ErrorBuilder builder() {
        return new ErrorBuilder();
    }
}