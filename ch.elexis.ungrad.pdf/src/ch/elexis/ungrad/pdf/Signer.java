package ch.elexis.ungrad.pdf;

//Adding Image in Existing PDF using Java

//Importing openCV libraries
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import ch.rgw.io.FileTool;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.io.IOException;

public class Signer {

  public void sign(String pdfFile, String imgFile, int x, int y) throws IOException {

    // Loading an already existing pdf document
    PDDocument doc = PDDocument.load(new File(pdfFile));

    // Retrieve the page
    PDPage page = doc.getPage(0);

    // Creating Object of PDImageXObject for selecting
    // Image and provide the path of file in argument
    PDImageXObject pdfimg = PDImageXObject.createFromFile(imgFile, doc);

    // Creating the PDPageContentStream Object
    // for Inserting Image
    PDPageContentStream image = new PDPageContentStream(doc, page);

    // set the Image inside the Page
    image.drawImage(pdfimg, x, y);
    System.out.println("Image Inserted");

    // Closing the page of PDF by closing
    // PDPageContentStream Object
    // && Saving the Document
    image.close();
    String path = FileTool.getFilepath(pdfFile);
    String base = FileTool.getNakedFilename(pdfFile);
    File out = new File(path, base + "_signed.pdf");
    doc.save(out);

    // Closing the Document
    doc.close();
  }
}
