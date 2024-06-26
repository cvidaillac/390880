/**
 * 
 */
package com.sgdbf.pop3p.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * This abstract class is generated and should not be modified.
 */
public abstract class AbstractPop3pGetHierarchyImpl extends AbstractConnector {

	protected final static String WSDLURL_INPUT_PARAMETER = "wsdlURL";
	protected final static String COOKIE_INPUT_PARAMETER = "cookie";
	protected final static String CODEWORKFLOW_INPUT_PARAMETER = "codeWorkflow";
	protected final static String FAMILLEMAT_INPUT_PARAMETER = "familleMat";
	protected final static String CODENOEUDMAT_INPUT_PARAMETER = "codeNoeudMat";
	protected final static String MONTANTPROPOSITION_INPUT_PARAMETER = "montantProposition";
	protected final String RESULT_OUTPUT_PARAMETER = "result";

	protected final java.lang.String getWsdlURL() {
		return (java.lang.String) getInputParameter(WSDLURL_INPUT_PARAMETER);
	}

	protected final java.lang.String getCookie() {
		return (java.lang.String) getInputParameter(COOKIE_INPUT_PARAMETER);
	}

	protected final java.lang.String getCodeWorkflow() {
		return (java.lang.String) getInputParameter(CODEWORKFLOW_INPUT_PARAMETER);
	}

	protected final java.lang.String getFamilleMat() {
		return (java.lang.String) getInputParameter(FAMILLEMAT_INPUT_PARAMETER);
	}

	protected final java.lang.String getCodeNoeudMat() {
		return (java.lang.String) getInputParameter(CODENOEUDMAT_INPUT_PARAMETER);
	}

	protected final java.lang.String getMontantProposition() {
		return (java.lang.String) getInputParameter(MONTANTPROPOSITION_INPUT_PARAMETER);
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
			getCodeWorkflow();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("codeWorkflow type is invalid");
		}
		try {
			getFamilleMat();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("familleMat type is invalid");
		}
		try {
			getCodeNoeudMat();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("codeNoeudMat type is invalid");
		}
		try {
			getMontantProposition();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("montantProposition type is invalid");
		}

	}

}
