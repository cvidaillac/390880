package org.mycompany.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public abstract class AbstractGeneratePDFImpl extends AbstractConnector {

	protected final String RESULTPDF_OUTPUT_PARAMETER = "resultPDF";

	protected final void setResultPDF(
			org.bonitasoft.engine.bpm.document.DocumentValue resultPDF) {
		setOutputParameter(RESULTPDF_OUTPUT_PARAMETER, resultPDF);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {

	}

}
