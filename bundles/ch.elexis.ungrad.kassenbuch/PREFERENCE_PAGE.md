# Kassenbuch Preference Page Implementation

## Overview
A preference page has been added to the Kassenbuch plugin, allowing users to configure a custom HTML template for PDF generation.

## Files Created

### 1. PreferenceConstants.java
- Defines the preference key: `HTML_TEMPLATE = "kassenbuch/html_template"`
- Used to store the user's selected template file path

### 2. KassenbuchPreferencePage.java
- Preference page under category `ch.elexis.ungrad.common.prefs`
- Uses `FileFieldEditor` to allow file selection
- Stores preference in `CoreHub.localCfg`
- Properly flushes configuration on Apply and OK

## Files Modified

### plugin.xml
Added preference page extension:
```xml
<extension point="org.eclipse.ui.preferencePages">
   <page
      category="ch.elexis.ungrad.common.prefs"
      class="ch.elexis.buchhaltung.kassenbuch.KassenbuchPreferencePage"
      id="ch.elexis.buchhaltung.kassenbuch.preferences"
      name="%Plugin_PreferencesName">
   </page>
</extension>
```

### Messages.java
Added preference page message keys:
- `Plugin_PreferencesName` - Preference page name
- `PreferencePage_Description` - Page description
- `PreferencePage_HTMLTemplate` - Template field label

### messages*.properties (all 4 languages)
Added localized strings:
- **German**: "Kassenbuch Einstellungen" / "HTML Vorlage für PDF Export (leer = Standard)"
- **English**: "Cash Book Settings" / "HTML Template for PDF Export (empty = default)"
- **French**: "Paramètres du livre de caisse" / "Modèle HTML pour l'exportation PDF (vide = par défaut)"
- **Italian**: "Impostazioni libro di cassa" / "Modello HTML per l'esportazione PDF (vuoto = predefinito)"

### plugin*.properties (all 4 languages)
Added `Plugin_PreferencesName` entry for all languages

### PdfPrinter.java
Updated `readTemplate()` method to:
1. **Check user preference first**: Reads from `CoreHub.localCfg.get(PreferenceConstants.HTML_TEMPLATE, null)`
2. **Validate file**: Checks if file exists and is readable
3. **Read custom template**: If valid, reads from the user-specified file
4. **Fall back to default**: If not configured or invalid, uses `/rsc/summary.html`

## How It Works

### User Configuration
1. Navigate to: **Preferences → Elexis Ungrad → Kassenbuch**
2. Click "Browse" to select a custom HTML template file
3. Click "Apply" or "OK" to save

### Template Selection Logic
```java
// 1. Try user-configured template
String userTemplate = CoreHub.localCfg.get(PreferenceConstants.HTML_TEMPLATE, null);

// 2. If valid, use it
if (userTemplate != null && !userTemplate.trim().isEmpty()) {
    File templateFile = new File(userTemplate);
    if (templateFile.exists() && templateFile.canRead()) {
        // Read from user file
    }
}

// 3. Otherwise, fall back to default rsc/summary.html
InputStream is = PdfPrinter.class.getResourceAsStream("/rsc/summary.html");
```

### Template Requirements
Custom templates must include these placeholders:
- `[from]` - Start date
- `[until]` - End date
- `[rows]` - Transaction rows
- `[total_income]` - Total income
- `[total_expense]` - Total expenses
- `[final_balance]` - Final balance
- `[category_rows]` - Category summary rows

## Category Summary Table
The category summary table now includes the **Saldo** column:
- **Kategorie** - Category name
- **Einnahmen** - Income
- **Ausgaben** - Expenses
- **Saldo** - Balance (Income - Expenses)

Each category row displays:
```html
<td>Category</td>
<td>Income Amount</td>
<td>Expense Amount</td>
<td>Balance Amount</td>
```

## Testing
1. Leave preference empty → Uses default `rsc/summary.html`
2. Set invalid path → Falls back to default template
3. Set valid custom template → Uses custom template for PDF generation

## Benefits
- Users can customize PDF layout without modifying code
- Supports company branding and custom layouts
- Safe fallback to default template
- Fully localized in 4 languages
- Integrated with existing Ungrad preferences structure

## Location in Preferences
**Preferences → Elexis Ungrad → Kassenbuch**

The page appears as a sub-page under the "Elexis Ungrad" category, alongside other Ungrad common preferences.
