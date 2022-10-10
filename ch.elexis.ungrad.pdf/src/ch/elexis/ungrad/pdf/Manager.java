package ch.elexis.ungrad.pdf;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.rgw.tools.StringTool;

public class Manager {
	
	public void createPDF(File inputHtml, File outputFile)
			throws FileNotFoundException, IOException, PrinterException {
		FileOutputStream fout = new FileOutputStream(outputFile);
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.useFastMode();
		builder.withFile(inputHtml);
		builder.toStream(fout);
		builder.run();

	}

	public boolean printFromPDF(File pdfFile, String printer) throws IOException, PrinterException {
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
