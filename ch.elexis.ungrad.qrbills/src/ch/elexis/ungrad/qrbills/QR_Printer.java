/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
package ch.elexis.ungrad.qrbills;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;

import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.StringTool;

public class QR_Printer {

	/**
	 * Print a pdf from a File
	 * 
	 * @param pdfFile The File. Must be PDF
	 * @param printer Name or part of the name of the printer to chose. Can be null,
	 *                then a print dialog is displayed
	 * @return true if the file was printed
	 * @throws IOException
	 * @throws PrinterException
	 */
	public boolean print(File pdfFile, String printer) throws IOException, PrinterException {
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
}
