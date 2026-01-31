package ch.elexis.ungrad.whisper;

import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.ungrad.whisper.WhisperService.ITextConsumer;

import org.eclipse.core.commands.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleDictationHandler extends AbstractHandler {

	private final WhisperService whisperService = new WhisperService();
	private boolean active = false;

	public ToggleDictationHandler() {
		whisperService.setTextConsumer(new ITextConsumer() {
			@Override
			public void onText(String text) {
				insertTextIntoFocusedField(text);
			}
		});
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!active) {
			String modelPath = "/pfad/zum/ggml-model.bin"; // TODO: aus Preferences lesen
			boolean ok = whisperService.start(modelPath);
			if (ok) {
				active = true;
			}
		} else {
			whisperService.stop();
			active = false;
		}
		return null;
	}

	private void insertTextIntoFocusedField(String textToInsert) {
		Display display = Display.getDefault();
		if (display == null || display.isDisposed()) {
			return;
		}

		Control focusControl = display.getFocusControl();
		if (!(focusControl instanceof EnhancedTextField)) {
			return;
		}

		EnhancedTextField etf = (EnhancedTextField) focusControl;
		// internen StyledText holen
		StyledText styled = (StyledText) etf.getControl();

		// aktuelle Selektion bestimmen
		Point sel = styled.getSelection();
		int start = sel.x;
		int end = sel.y;

		if (start < 0 || end < 0 || start > styled.getCharCount() || end > styled.getCharCount()) {
			// Fallback: ans Ende anhängen
			start = end = styled.getCharCount();
		}

		// ausgewählten Bereich durch Diktat-Text ersetzen
		styled.replaceTextRange(start, end - start, textToInsert);

		int newCaret = start + textToInsert.length();
		styled.setSelection(newCaret, newCaret);

		// Elexis-Logik nachziehen: Samdas-Record und Dirty-Flag aktualisieren
		// (entspricht dem, was z.B. insertXRef/updateXRef machen)
		// Achtung: Zugriff auf package-private Felder nur im selben Package.
		try {
			// wir sind im selben Bundle/Package wie EnhancedTextField
			etf.setDirty(true);
			// record/text aktualisieren, damit getContentsAsXML konsistent bleibt
			etf.getContents().getRecord().setText(styled.getText());
		} catch (Exception ignored) {
			// falls du das in einer separaten Hilfsklasse machst, kannst du
			// stattdessen eine öffentliche Hilfsmethode in EnhancedTextField ergänzen.
		}
	}

}
