package com.sgdbf.rest.api.dto;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;


@JsonDeserialize(builder = Result.ResultBuilder.class)
public class Result {
	@JsonDeserialize(builder = Result.RegionBuilder.class)
    public static class RegionValue{
    	public String regionId;
    	public String regionName;
    	
    	public RegionValue(String id, String name) {
    		regionId = id;
    		regionName = name;
    	}
    };
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class RegionBuilder {
    	private String regionId;
    	private String regionName;
    	
        public RegionBuilder regionId(String regionId) {
            this.regionId =  regionId;
            return this;
        }
        
        public RegionBuilder regionName(String regionName) {
            this.regionName =  regionName;
            return this;
        }        
        
        public RegionValue build() {
            return new RegionValue(regionId, regionName);
        }
    	
    };
	
    private String errorMsg;	// Error message in case of failure, mepty if OK
	private RegionValue[] regions;
	
    public Result(String error_msg, RegionValue[] regions_values) {
    	errorMsg = error_msg;
    	regions = regions_values;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public RegionValue[] getRegions() {
        return regions;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultBuilder {
    	private String errorMsg;
    	private RegionValue[] regions;

        public ResultBuilder errorMsg(String errorMsg) {
            this.errorMsg =  errorMsg;
            return this;
        }
        
        public ResultBuilder regions(RegionValue[] regions) {
            this.regions =  regions;
            return this;
        }

        public void setRegionsFromList(List<RegionValue> list_values) {
        	this.regions = list_values.toArray(new RegionValue[0] );
        }
        
        public Result build() {
            return new Result(errorMsg, regions);
        }
    }

    public static ResultBuilder builder() {
        return new ResultBuilder();
    }
}

