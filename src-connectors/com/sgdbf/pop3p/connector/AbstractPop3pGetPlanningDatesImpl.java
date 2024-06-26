package com.sgdbf.pop3p.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public abstract class AbstractPop3pGetPlanningDatesImpl extends
		AbstractConnector {

	protected final static String WSDLURL_INPUT_PARAMETER = "wsdlUrl";
	protected final static String COOKIE_INPUT_PARAMETER = "cookie";
	protected final static String ANNEEBUDGET_INPUT_PARAMETER = "anneeBudget";
	protected final static String CODEAGENCE_INPUT_PARAMETER = "codeAgence";
	protected final String RESULT_OUTPUT_PARAMETER = "result";

	protected final java.lang.String getWsdlUrl() {
		return (java.lang.String) getInputParameter(WSDLURL_INPUT_PARAMETER);
	}

	protected final java.lang.String getCookie() {
		return (java.lang.String) getInputParameter(COOKIE_INPUT_PARAMETER);
	}

	protected final java.lang.String getAnneeBudget() {
		return (java.lang.String) getInputParameter(ANNEEBUDGET_INPUT_PARAMETER);
	}

	protected final java.lang.String getCodeAgence() {
		return (java.lang.String) getInputParameter(CODEAGENCE_INPUT_PARAMETER);
	}

	protected final void setResult(java.util.List result) {
		setOutputParameter(RESULT_OUTPUT_PARAMETER, result);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getWsdlUrl();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("wsdlUrl type is invalid");
		}
		try {
			getCookie();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("cookie type is invalid");
		}
		try {
			getAnneeBudget();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException(
					"anneeBudget type is invalid");
		}
		try {
			getCodeAgence();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("codeAgence type is invalid");
		}

	}

}
