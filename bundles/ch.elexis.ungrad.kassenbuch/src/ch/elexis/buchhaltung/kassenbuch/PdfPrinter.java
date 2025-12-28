package ch.elexis.buchhaltung.kassenbuch;

import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class PdfPrinter {

	public static void createSummary(TimeTool von, TimeTool bis, File outputPath, String fileName) throws FileNotFoundException, IOException, PrinterException {
		Manager pdfManager = new Manager();
		Set<KassenbuchEintrag> liste = KassenbuchEintrag.getBookings(von, bis);
		File htmlFile = new File(System.getProperty("java.io.tmpdir"), "kassenbuch_summary.html");
		File output = new File(outputPath, fileName);
		
		// Read template
		String template = readTemplate();
		
		// Build rows HTML
		StringBuilder rowsHtml = new StringBuilder();
		Money totalIncome = new Money();
		Money totalExpense = new Money();
		Money finalBalance = new Money();
		Map<String, CategorySum> categoryMap = new HashMap<>();
		
		for (KassenbuchEintrag entry : liste) {
			Money amount = entry.getAmount();
			Money saldo = entry.getSaldo();
			String kategorie = entry.getKategorie();
			
			// Add to category sum
			if (StringTool.isNothing(kategorie)) {
				kategorie = "Sonstiges";
			}
			CategorySum catSum = categoryMap.get(kategorie);
			if (catSum == null) {
				catSum = new CategorySum();
				categoryMap.put(kategorie, catSum);
			}
			
			// Build row
			rowsHtml.append("\t\t\t<tr>\n");
			rowsHtml.append("\t\t\t\t<td>").append(entry.getDate()).append("</td>\n");
			rowsHtml.append("\t\t\t\t<td>").append(entry.getBelegNr()).append("</td>\n");
			rowsHtml.append("\t\t\t\t<td>").append(htmlEscape(entry.getText())).append("</td>\n");
			
			if (amount.isNegative()) {
				// Expense
				Money expense = new Money(amount).negate();
				rowsHtml.append("\t\t\t\t<td></td>\n");
				rowsHtml.append("\t\t\t\t<td>").append(expense.getAmountAsString()).append("</td>\n");
				totalExpense.addMoney(expense);
				catSum.expense.addMoney(expense);
			} else {
				// Income
				rowsHtml.append("\t\t\t\t<td>").append(amount.getAmountAsString()).append("</td>\n");
				rowsHtml.append("\t\t\t\t<td></td>\n");
				totalIncome.addMoney(amount);
				catSum.income.addMoney(amount);
			}
			
			rowsHtml.append("\t\t\t\t<td>").append(saldo.getAmountAsString()).append("</td>\n");
			rowsHtml.append("\t\t\t</tr>\n");
			
			finalBalance = saldo;
		}
		
		// Build category rows HTML
		StringBuilder categoryRowsHtml = new StringBuilder();
		for (Map.Entry<String, CategorySum> catEntry : categoryMap.entrySet()) {
			CategorySum catSum = catEntry.getValue();
			Money categorySaldo = new Money(catSum.income);
			categorySaldo.subtractMoney(catSum.expense);
			
			categoryRowsHtml.append("\t\t\t<tr>\n");
			categoryRowsHtml.append("\t\t\t\t<td>").append(htmlEscape(catEntry.getKey())).append("</td>\n");
			categoryRowsHtml.append("\t\t\t\t<td>").append(catSum.income.getAmountAsString()).append("</td>\n");
			categoryRowsHtml.append("\t\t\t\t<td>").append(catSum.expense.getAmountAsString()).append("</td>\n");
			categoryRowsHtml.append("\t\t\t\t<td>").append(categorySaldo.getAmountAsString()).append("</td>\n");
			categoryRowsHtml.append("\t\t\t</tr>\n");
		}
		
		// Replace placeholders in template
		String html = template;
		html = html.replace("[from]", von != null ? von.toString(TimeTool.DATE_GER) : "Anfang");
		html = html.replace("[until]", bis != null ? bis.toString(TimeTool.DATE_GER) : "Heute");
		html = html.replace("[rows]", rowsHtml.toString());
		html = html.replace("[total_income]", totalIncome.getAmountAsString());
		html = html.replace("[total_expense]", totalExpense.getAmountAsString());
		html = html.replace("[final_balance]", finalBalance.getAmountAsString());
		html = html.replace("[category_rows]", categoryRowsHtml.toString());
		
		// Write HTML file
		try (FileWriter writer = new FileWriter(htmlFile, StandardCharsets.UTF_8)) {
			writer.write(html);
		}
		
		// Create PDF
		pdfManager.createPDF(htmlFile, output);
	}
	
	private static String readTemplate() throws IOException {
		InputStream is = PdfPrinter.class.getResourceAsStream("/rsc/summary.html");
		if (is == null) {
			throw new FileNotFoundException("Template file rsc/summary.html not found");
		}
		
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		}
		return sb.toString();
	}
	
	private static String htmlEscape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				   .replace("<", "&lt;")
				   .replace(">", "&gt;")
				   .replace("\"", "&quot;")
				   .replace("'", "&#39;");
	}
	
	private static class CategorySum {
		Money income = new Money();
		Money expense = new Money();
	}
}
	