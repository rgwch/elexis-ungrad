/*******************************************************************************
 * Copyright (c) 2022-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.pdf;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.printing.PDFPrintable;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Handle HTML-to-PDF conversion, access PDF form fields, and print PDF files
 * Uses https://github.com/danfickle/openhtmltopdf and https://pdfbox.apache.org/
 * @author gerry
 *
 */
public class Manager {
	private PDDocument pdfDoc;
	
	/**
	 * Fill out a PDF form. 
	 * @param formpath full path to the pdf file with an embedded form
	 * @param outputPath full lath for the resulting file with filled form
	 * @param fields fields to write. fields in the form not mentioned herein remain simply unchanged. Fields not found in the form are ignored.
	 * @return the path to the newly written file (which is the same as outputPath)
	 * @throws Exception
	 */
	public String fillForm(String formpath, String outputPath, Map<String, String> fields)
		throws Exception{
		try {
			if (pdfDoc == null) {
				InputStream resource = new FileInputStream(formpath);
				pdfDoc = PDDocument.load(resource);
			}
			PDDocumentCatalog docCatalog = pdfDoc.getDocumentCatalog();
			pdfDoc.getDocumentInformation().setCreator("Elexis Ungrad");
			pdfDoc.getDocumentInformation().setCustomMetadataValue("concern", "Forms");
			PDAcroForm acroForm = docCatalog.getAcroForm();
			for (Entry<String, String> e : fields.entrySet()) {
				PDField pdField = acroForm.getField(e.getKey());
				if (pdField != null) {
					try {
						String val = e.getValue();
						pdField.setValue(val);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			pdfDoc.save(outputPath);
			pdfDoc.close();
			pdfDoc = null;
			return outputPath;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return "";
		}
	}
	
	/**
	 * Retrieve the value of a field in a form embedded in a pdf file
	 * @param formPath full filepath to the PDF File with an embedded form
	 * @param fieldName Name of the field to retrieve
	 * @return the contents of the field or empty string if no such field was found.
	 * @throws Exception
	 */
	public String getFieldContents(String formPath, String fieldName) throws Exception{
		if (pdfDoc == null) {
			pdfDoc = PDDocument.load(new File(formPath));
		}
		PDDocumentCatalog docCatalog = pdfDoc.getDocumentCatalog();
		PDAcroForm acroForm = docCatalog.getAcroForm();
		PDField pdField = acroForm.getField(fieldName);
		if (pdField != null) {
			try {
				String val = pdField.getValueAsString();
				return val;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 * Create a PDF file from a HTML.
	 * @param inputHtml the file to read. If a style sheet is linked, it must be in the current dir or an absolute path.
	 * @param outputFile the PDF to write
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws PrinterException
	 */
	public void createPDF(File inputHtml, File outputFile)
		throws FileNotFoundException, IOException, PrinterException{
		FileOutputStream fout = new FileOutputStream(outputFile);
		PdfRendererBuilder builder = new PdfRendererBuilder().useFastMode().withFile(inputHtml)
			.withProducer("Elexis Ungrad");
		builder.toStream(fout);
		builder.run();
		
	}
	
	/**
	 * Print a PDF file
	 * @param pdfFile the file to print
	 * @param printer The printer to use. If it is null or the named printer is not found, a printer select dialog will open.
	 * @return true after print-
	 * @throws IOException
	 * @throws PrinterException
	 */
	public boolean printFromPDF(File pdfFile, String printer) throws IOException, PrinterException{
		PDDocument pdoc = PDDocument.load(pdfFile);
		PDFPrintable printable = new PDFPrintable(pdoc);
		PrinterJob job = PrinterJob.getPrinterJob();
		PrintServiceAttributeSet attributes;
		job.setPrintable(printable);
		boolean printed = false;
		if (!StringTool.isNothing(printer)) {
			PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
			int selectedService = 0;
			/* Scan found services to see if anyone suits our needs */
			for (int i = 0; i < services.length; i++) {
				if (services[i].getName().toLowerCase().contains(printer.toLowerCase())) {
					selectedService = i;
					attributes = services[i].getAttributes();
					break;
				}
			}
			job.setPrintService(services[selectedService]);
			HashPrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
			attrs.add(MediaSizeName.ISO_A4);
			job.print(attrs);
			printed = true;
			
		} else {
			if (job.printDialog()) {
				job.print();
				printed = true;
			}
			
		}
		pdoc.close();
		return printed;
		
	}
	
	public boolean printFromHTML(File htmlFle, String printer, boolean bKeepHtml, boolean bKeepPdf){
		return false;
	}
}
