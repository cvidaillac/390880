/**
 * 
 */
package org.mycompany.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class GeneratePDFImpl extends AbstractGeneratePDFImpl {

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//Get access to the connector input parameters

		//TODO execute your business logic here 

		//WARNING : Set the output of the connector execution. If outputs are not set, connector fails
		//setResultPDF(resultPDF);

		// Create a new empty document
		PDDocument pdfDoc = new PDDocument();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			// Create a new blank page and add it to the document
			PDPage firstPage = new PDPage();


			PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, firstPage);

			//Begin the Content stream 
			contentStream.beginText(); 

			//Setting the font to the Content stream  
			contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);

			//Setting the position for the line 
			contentStream.newLineAtOffset(25, 500);

			String text = "This is the sample document and we are adding content to it.";

			//Adding text in the form of string 
			contentStream.showText(text);      

			//Ending the content stream
			contentStream.endText();

			//Closing the content stream
			contentStream.close();
			pdfDoc.addPage( firstPage );



			// finally make sure that the document is properly
			// closed.

			pdfDoc.save(byteArrayOutputStream);
			pdfDoc.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

		DocumentValue resultPDF=  new DocumentValue(byteArrayOutputStream.toByteArray(), "application/pdf", "PremierPdf.pdf"); 
		setResultPDF(resultPDF);

	}

	@Override
	public void connect() throws ConnectorException{
		//[Optional] Open a connection to remote server

	}

	@Override
	public void disconnect() throws ConnectorException{
		//[Optional] Close connection to remote server

	}

}
