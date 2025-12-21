# Localization Reference Table

| Key | German (DE) | English (EN) | French (FR) |
|-----|-------------|--------------|-------------|
| **TardocKonsView** |
| `TardocKonsView_ToggleAdditions_Tooltip` | Zusatzansicht umschalten | Toggle additional view | Basculer la vue supplémentaire |
| `TardocKonsView_SwitchToDiagnoses_Tooltip` | Zu Diagnosen wechseln | Switch to diagnoses | Passer aux diagnostics |
| `TardocKonsView_SwitchToBillingPositions_Tooltip` | Zu Abrechnungspositionen wechseln | Switch to billing positions | Passer aux positions de facturation |
| **TimerComposite** |
| `TimerComposite_StartTimer_Tooltip` | Timer starten | Start timer | Démarrer le minuteur |
| `TimerComposite_PauseTimer_Tooltip` | Timer pausieren | Pause timer | Mettre en pause le minuteur |
| `TimerComposite_ResetTimer_Tooltip` | Timer zurücksetzen | Reset timer | Réinitialiser le minuteur |
| **BillingPositionsComposite** |
| `BillingPositionsComposite_Search_Label` | Suche: | Search: | Recherche: |
| `BillingPositionsComposite_Search_Placeholder` | Suchbegriff... | Search term... | Terme de recherche... |
| **DiagnosesComposite** |
| `DiagnosesComposite_CodeSystem_Label` | Code System: | Code System: | Système de code: |
| `DiagnosesComposite_CodeSystem_All` | Alle | All | Tous |
| `DiagnosesComposite_CodeSystem_ICD10` | ICD-10 | ICD-10 | ICD-10 |
| `DiagnosesComposite_CodeSystem_TICode` | TI-Code | TI-Code | TI-Code |
| `DiagnosesComposite_Search_Label` | Suche: | Search: | Recherche: |
| `DiagnosesComposite_Search_Placeholder` | Diagnosecode oder Text eingeben... | Enter diagnosis code or text... | Entrer code de diagnostic ou texte... |
| **CasesComposite** |
| `CasesComposite_NoCase_Selected` | Kein Fall ausgewählt | No case selected | Aucun cas sélectionné |
| `CasesComposite_Closed_Suffix` | (geschlossen) | (closed) | (fermé) |

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
```
