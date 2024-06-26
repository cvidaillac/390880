/**
 * 
 */
package org.sgdbf.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * This abstract class is generated and should not be modified.
 */
public abstract class AbstractUnassignBoLacTasksImpl extends AbstractConnector {

	protected final String COUNTBOTASKS_OUTPUT_PARAMETER = "countBOTasks";
	protected final String COUNTLACTASKS_OUTPUT_PARAMETER = "countLACTasks";

	protected final void setCountBOTasks(java.lang.Integer countBOTasks) {
		setOutputParameter(COUNTBOTASKS_OUTPUT_PARAMETER, countBOTasks);
	}

	protected final void setCountLACTasks(java.lang.Integer countLACTasks) {
		setOutputParameter(COUNTLACTASKS_OUTPUT_PARAMETER, countLACTasks);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {

	}

}
