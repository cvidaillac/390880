/**
 * 
 */
package com.sgdbf.pop3p.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * This abstract class is generated and should not be modified.
 */
public abstract class AbstractPop3pGetMailCopyImpl extends AbstractConnector {

	protected final static String WSDLURL_INPUT_PARAMETER = "wsdlURL";
	protected final static String COOKIE_INPUT_PARAMETER = "cookie";
	protected final static String CODENOEUD_INPUT_PARAMETER = "codeNoeud";
	protected final static String CODEWORKFLOW_INPUT_PARAMETER = "codeWorkflow";
	protected final static String GENREMATERIEL_INPUT_PARAMETER = "genreMateriel";
	protected final String RESULT_OUTPUT_PARAMETER = "result";

	protected final java.lang.String getWsdlURL() {
		return (java.lang.String) getInputParameter(WSDLURL_INPUT_PARAMETER);
	}

	protected final java.lang.String getCookie() {
		return (java.lang.String) getInputParameter(COOKIE_INPUT_PARAMETER);
	}

	protected final java.lang.String getCodeNoeud() {
		return (java.lang.String) getInputParameter(CODENOEUD_INPUT_PARAMETER);
	}

	protected final java.lang.String getCodeWorkflow() {
		return (java.lang.String) getInputParameter(CODEWORKFLOW_INPUT_PARAMETER);
	}

	protected final java.lang.String getGenreMateriel() {
		return (java.lang.String) getInputParameter(GENREMATERIEL_INPUT_PARAMETER);
	}

	protected final void setResult(java.util.List result) {
		setOutputParameter(RESULT_OUTPUT_PARAMETER, result);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getWsdlURL();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("wsdlURL type is invalid");
		}
		try {
			getCookie();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("cookie type is invalid");
		}
		try {
			getCodeNoeud();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("codeNoeud type is invalid");
		}
		try {
			getCodeWorkflow();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("codeWorkflow type is invalid");
		}
		try {
			getGenreMateriel();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("genreMateriel type is invalid");
		}

	}

}
