package org.mycompany.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public abstract class AbstractAlfresco_SGDBFImpl extends AbstractConnector {

	protected final static String INPUT1_INPUT_PARAMETER = "Input1";
	protected final static String INPUT2_INPUT_PARAMETER = "Input2";
	protected final static String INPUT3_INPUT_PARAMETER = "Input3";
	protected final static String INPUT4_INPUT_PARAMETER = "Input4";
	protected final static String INPUT5_INPUT_PARAMETER = "Input5";

	protected final java.lang.String getInput1() {
		return (java.lang.String) getInputParameter(INPUT1_INPUT_PARAMETER);
	}

	protected final java.lang.String getInput2() {
		return (java.lang.String) getInputParameter(INPUT2_INPUT_PARAMETER);
	}

	protected final java.lang.String getInput3() {
		return (java.lang.String) getInputParameter(INPUT3_INPUT_PARAMETER);
	}

	protected final java.lang.String getInput4() {
		return (java.lang.String) getInputParameter(INPUT4_INPUT_PARAMETER);
	}

	protected final java.lang.String getInput5() {
		return (java.lang.String) getInputParameter(INPUT5_INPUT_PARAMETER);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getInput1();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("Input1 type is invalid");
		}
		try {
			getInput2();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("Input2 type is invalid");
		}
		try {
			getInput3();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("Input3 type is invalid");
		}
		try {
			getInput4();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("Input4 type is invalid");
		}
		try {
			getInput5();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("Input5 type is invalid");
		}

	}

}
