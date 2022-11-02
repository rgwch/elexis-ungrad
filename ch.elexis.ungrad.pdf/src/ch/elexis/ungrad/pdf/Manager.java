/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.text.PlainDocument;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.printing.PDFPrintable;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class Manager {

  public String fillForm(String formpath, String outputPath, Map<String, String> fields) throws Exception {
    PDDocument pdfDocument;
    try{
      InputStream resource = new FileInputStream(formpath);
      pdfDocument = PDDocument.load(resource);
      PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
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
      pdfDocument.save(outputPath);
      pdfDocument.close();
      return outputPath;
    }catch(Exception ex){
      ExHandler.handle(ex);
      return "";
    }
  }

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

  public boolean printFromHTML(File htmlFle, String printer, boolean bKeepHtml, boolean bKeepPdf) {
    return false;
  }
}
