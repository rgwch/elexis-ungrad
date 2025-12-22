# Localization Reference Table

| Key | German (DE) | English (EN) | French (FR) | Italian (IT) |
|-----|-------------|--------------|-------------|--------------|
| **TardocKonsView** |
| `TardocKonsView_ToggleAdditions_Tooltip` | Zusatzansicht umschalten | Toggle additional view | Basculer la vue supplémentaire | Attiva/disattiva vista aggiuntiva |
| `TardocKonsView_SwitchToDiagnoses_Tooltip` | Zu Diagnosen wechseln | Switch to diagnoses | Passer aux diagnostics | Passa alle diagnosi |
| `TardocKonsView_SwitchToBillingPositions_Tooltip` | Zu Abrechnungspositionen wechseln | Switch to billing positions | Passer aux positions de facturation | Passa alle posizioni di fatturazione |
| **TimerComposite** |
| `TimerComposite_StartTimer_Tooltip` | Timer starten | Start timer | Démarrer le minuteur | Avvia timer |
| `TimerComposite_PauseTimer_Tooltip` | Timer pausieren | Pause timer | Mettre en pause le minuteur | Metti in pausa timer |
| `TimerComposite_ResetTimer_Tooltip` | Timer zurücksetzen | Reset timer | Réinitialiser le minuteur | Reimposta timer |
| **BillingPositionsComposite** |
| `BillingPositionsComposite_Search_Label` | Suche: | Search: | Recherche: | Ricerca: |
| `BillingPositionsComposite_Search_Placeholder` | Suchbegriff... | Search term... | Terme de recherche... | Termine di ricerca... |
| **DiagnosesComposite** |
| `DiagnosesComposite_CodeSystem_Label` | Code System: | Code System: | Système de code: | Sistema di codifica: |
| `DiagnosesComposite_CodeSystem_All` | Alle | All | Tous | Tutti |
| `DiagnosesComposite_CodeSystem_ICD10` | ICD-10 | ICD-10 | ICD-10 | ICD-10 |
| `DiagnosesComposite_CodeSystem_TICode` | TI-Code | TI-Code | TI-Code | TI-Code |
| `DiagnosesComposite_Search_Label` | Suche: | Search: | Recherche: | Ricerca: |
| `DiagnosesComposite_Search_Placeholder` | Diagnosecode oder Text eingeben... | Enter diagnosis code or text... | Entrer code de diagnostic ou texte... | Inserire codice diagnostico o testo... |
| **CasesComposite** |
| `CasesComposite_NoCase_Selected` | Kein Fall ausgewählt | No case selected | Aucun cas sélectionné | Nessun caso selezionato |
| `CasesComposite_Closed_Suffix` | (geschlossen) | (closed) | (fermé) | (chiuso) |

## Usage in Code

To use these messages in your Java code:

```java
import ch.elexis.ungrad.tardoc.Messages;

// Example 1: Set a tooltip
button.setToolTipText(Messages.TimerComposite_StartTimer_Tooltip);

// Example 2: Set label text
label.setText(Messages.BillingPositionsComposite_Search_Label);

// Example 3: Set placeholder text
textField.setMessage(Messages.BillingPositionsComposite_Search_Placeholder);
```

## Adding New Translations

To add a new localizable string:

1. Add the field to `Messages.java`:
   ```java
   public static String MyComponent_MyText;
   ```

2. Add the translation to all three `.properties` files:
   - `messages.properties` (German)
   - `messages_en.properties` (English)
   - `messages_fr.properties` (French)

3. Use it in your code:
   ```java
   label.setText(Messages.MyComponent_MyText);
   ```

## Testing Different Languages

To test the plugin in different languages, start Eclipse with the `-nl` parameter:

```bash
# German (default)
eclipse -nl de

# English
eclipse -nl en

# French
eclipse -nl fr

# Italian
eclipse -nl it
```
