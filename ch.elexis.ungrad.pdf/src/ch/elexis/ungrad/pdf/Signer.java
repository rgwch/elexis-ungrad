package ch.elexis.ungrad.pdf;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.io.IOException;

public class Signer {

	/**
	 * Add Image to existing PDF and write resulting PDF to same directory as
	 * name_signed.pdf
	 * 
	 * @param pdfFile full path to existing pdf
	 * @param imgFile full path to image file
	 * @param x       placement horizontal
	 * @param y       placement vertical
	 * @throws IOException
	 */
	public void sign(String pdfName, String imgFile, int x, int y) throws IOException {
		File pdfFile = new File(pdfName);
		PDDocument doc = PDDocument.load(pdfFile);
		PDPage page = doc.getPage(0);

		PDImageXObject pdfimg = PDImageXObject.createFromFile(imgFile, doc);

		PDPageContentStream image = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);

		image.drawImage(pdfimg, x, y);

		image.close();

		int idx = pdfName.lastIndexOf(".");
		if (idx > -1) {
			pdfName = pdfName.substring(0, idx);
		}
		File out = new File(pdfName + "_signed.pdf");
		doc.save(out);
		doc.close();
	}
}
