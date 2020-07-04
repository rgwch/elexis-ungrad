package ch.elexis.pdfBills;

//Java
import java.io.File;

/**
 * This class demonstrates the conversion of an XML file to PDF using JAXP (XSLT) and FOP (XSL-FO).
 */
public class ExampleXML2PDF {
	
	/**
	 * Main method.
	 * 
	 * @param args
	 *            command-line arguments
	 */
	public static void main(String[] args){
		try {
			// Setup input and output files
			File xmlfile = new File("1359.xml");
			File xsltfile = new File("section2.xsl");
			
			File pdffile = new File("1359Section2.pdf");
			
			System.out.println("Input: XML (" + xmlfile + ")");
			System.out.println("Stylesheet: " + xsltfile);
			System.out.println("Output: PDF (" + pdffile + ")");
			System.out.println();
			System.out.println("Transforming...");
			
			ElexisPDFGenerator pdfGenerator = new ElexisPDFGenerator("userconfig.xml");
			String constantValue = "P2";
			pdfGenerator.generateMainPDF(xmlfile, xsltfile, pdffile, constantValue);
			
			System.out.println("Success!");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}
}