/**
 * 
 */
package com.sgdbf.pop3p.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * This abstract class is generated and should not be modified.
 */
public abstract class AbstractOLD_SaveHistoryToLYREImpl extends AbstractConnector {

	protected final static String COOKIE_INPUT_PARAMETER = "cookie";
	protected final static String URLWSDL_INPUT_PARAMETER = "urlWSDL";
	protected final static String CURRENTPROPOSITION_INPUT_PARAMETER = "currentProposition";

	protected final java.lang.String getCookie() {
		return (java.lang.String) getInputParameter(COOKIE_INPUT_PARAMETER);
	}

	protected final java.lang.String getUrlWSDL() {
		return (java.lang.String) getInputParameter(URLWSDL_INPUT_PARAMETER);
	}

	protected final com.sgdbf.model.Proposition getCurrentProposition() {
		return (com.sgdbf.model.Proposition) getInputParameter(CURRENTPROPOSITION_INPUT_PARAMETER);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getCookie();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("cookie type is invalid");
		}
		try {
			getUrlWSDL();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("urlWSDL type is invalid");
		}
		try {
			getCurrentProposition();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("currentProposition type is invalid");
		}

	}

}
