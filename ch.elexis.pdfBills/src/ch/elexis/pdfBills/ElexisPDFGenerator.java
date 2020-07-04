package ch.elexis.pdfBills;

/**
 * 
 */

/**
 * @author sramakri
 *
 */
import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class ElexisPDFGenerator {
	private String configfile;
	FopFactory fopFactory;
	
	public String leftMargin = "1.5cm";
	public String rightMargin = "0.7cm";
	public String topMargin = "1cm";
	public String bottomMargin = "0.7mm";
	public String besrMarginVertical = "0.75cm";
	public String besrMarginHorizontal = "0.75cm";
	
	public void setMarginData(String _left, String _right, String _top, String _bottom,
		String _besrV, String _besrH)
	{
		leftMargin = _left;
		rightMargin =_right;
		topMargin = _top;
		bottomMargin = _bottom;
		besrMarginVertical = _besrV;
		besrMarginHorizontal = _besrH;
	}
	public ElexisPDFGenerator(String configfile){
		this.configfile = configfile;
	}
	
	public void generateMainPDF(File input, File styleSheet, File output, String constantValue)
		throws Exception{
		OutputStream out = null;
		try {
			// configure fopFactory as desired
			fopFactory = FopFactory.newInstance(new File(configfile));
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			// configure foUserAgent as desired
			// Setup output
			out = new java.io.FileOutputStream(output);
			out = new java.io.BufferedOutputStream(out);
			
			// fopFactory.setFontBaseURL("fonts");
			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
			
			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(styleSheet));
			
			// Set the value of a <param> in the stylesheet
			transformer.setParameter("versionParam", "2.0");
        	transformer.setParameter("leftMargin",leftMargin);
        	transformer.setParameter("rightMargin",rightMargin);
        	transformer.setParameter("topMargin",topMargin);
        	transformer.setParameter("bottomMargin",bottomMargin);
			transformer.setParameter("besrMarginVertical", besrMarginVertical);
			transformer.setParameter("besrMarginHorizontal", besrMarginHorizontal);
			
			// Setup input for XSLT transformation
			Source src = new StreamSource(input);
			
			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());
			
			// Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			out.close();
		}
	}
}
