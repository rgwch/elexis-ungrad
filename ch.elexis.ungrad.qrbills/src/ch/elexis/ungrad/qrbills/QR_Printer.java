package ch.elexis.ungrad.qrbills;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;

public class QR_Printer {
	File pdfFile;

	QR_Printer(File file) throws IOException {
		pdfFile = file;
	}

	void print(String printer) throws IOException {
		PDDocument pdoc = PDDocument.load(pdfFile);
		PDFPrintable printable = new PDFPrintable(pdoc);
		
	}
}
