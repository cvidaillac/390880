package com.sgdf.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.vega_systems.ws.locprotns.ENERGIE;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(builder = Result.ResultBuilder.class)
public class Result {

    @JsonIgnore
    private final LocalDate currentDate = LocalDate.now();
	private String errorMsg;	// Error message in case of failure, mepty if OK
	private String errorNum;	// Error number in case of failure
	private Energy[] energies;
	
    public Result(String error_msg, String error_num, Energy[] list_energies) {
    	errorMsg = error_msg;
    	errorNum = error_num;
    	energies = list_energies;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
    
    public String getErrorNum() {
        return errorNum;
    }
    
    public Energy[] getEnergies() {
        return energies;
    }
    
    public LocalDate getCurrentDate() {
        return currentDate;
    }
    
    @JsonDeserialize(builder = Result.EnergyBuilder.class)
    public static class Energy {
    	public String codeEnergie;
    	public String libelleEnergie;
    	
    	public Energy(String code, String libelle) {
    		codeEnergie = code;
    		libelleEnergie = libelle;
    	}
    };
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class EnergyBuilder {
    	private String codeEnergie;
    	private String libelleEnergie;
    	
        public EnergyBuilder codeEnergie(String code) {
            this.codeEnergie =  code;
            return this;
        }
        
        public EnergyBuilder libelleEnergie(String libelle) {
            this.libelleEnergie =  libelle;
            return this;
        }
        
        public Energy build() {
            return new Energy(codeEnergie, libelleEnergie);
        }
    };
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultBuilder {
    	private String errorMsg;	// Error message in case of failure, mepty if OK
    	private String errorNum;	// Error number in case of failure
    	private Energy[] energies;
    	
    	public ResultBuilder errorMsg(String error_msg) {
            this.errorMsg =  error_msg;
            return this;
        }
    	
    	public ResultBuilder errorNum(String error_num) {
            this.errorNum =  error_num;
            return this;
        }
    	
    	public ResultBuilder setError(String error_msg, String error_num) {
            this.errorMsg =  error_msg;
            this.errorNum =  error_num;
            return this;
        }
    	
        public ResultBuilder energies(Energy[] list_energies) {
            this.energies = list_energies;
            return this;
        }
        
        public ResultBuilder setEnergies(List<ENERGIE> list_energies) {
			int nb_energies = list_energies.size();
			
			if (nb_energies > 0) {
				this.energies = new Energy[nb_energies];
				for (int i=0; i<nb_energies; i++) {
					ENERGIE one_energy = list_energies.get(i);
					this.energies[i] = new Energy(one_energy.getCodeEnergie(), one_energy.getLibelleEnergie());
				}
			}
            return this;
        }
    	
        public Result build() {
        	return new Result(errorMsg, errorNum, energies);

        }
    }

    public static ResultBuilder builder() {
        return new ResultBuilder();
    }
}

