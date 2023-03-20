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

    PDDocument doc = PDDocument.load(new File(pdfFile));
    PDPage page = doc.getPage(0);

    PDImageXObject pdfimg = PDImageXObject.createFromFile(imgFile, doc);

    PDPageContentStream image = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);

    image.drawImage(pdfimg, x, y);
  
    image.close();
    String path = FileTool.getFilepath(pdfFile);
    String base = FileTool.getNakedFilename(pdfFile);
    File out = new File(path, base + "_signed.pdf");
    doc.save(out);

    // Closing the Document
    doc.close();
  }
}
