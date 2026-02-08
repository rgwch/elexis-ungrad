# Quick Reference: Adding New Localized Strings

If you need to add new localizable strings to the ch.elexis.ungrad.qr-outputter plugin, follow these steps:

## Step 1: Add Field to Messages.java

Edit: `src/ch/elexis/ungrad/QR_Outputter/Messages.java`

Add a new public static String field:
```java
public static String MyNewClass_MyNewLabel;
```

## Step 2: Add Translations to Properties Files

### English (messages.properties)
```properties
MyNewClass_MyNewLabel=My English Text
```

### German (messages_de.properties)
```properties
MyNewClass_MyNewLabel=Mein deutscher Text
```

### French (messages_fr.properties)
```properties
MyNewClass_MyNewLabel=Mon texte français
```

### Italian (messages_it.properties)
```properties
MyNewClass_MyNewLabel=Il mio testo italiano
```

## Step 3: Use in Java Code

In your Java class:
```java
label.setText(Messages.MyNewClass_MyNewLabel);
```

## Naming Convention

Follow this pattern for field names:
- **Format:** `ClassName_Description`
- **Examples:**
  - `BillPreferencePage_Title`
  - `QrRnOutputter_ErrorTitle`
  - `QR_SettingsControl_PrintQRPage`

## Message Parameters

For messages with parameters, use `{0}`, `{1}`, etc.:

### In properties file:
```properties
MyClass_ErrorMessage=Error processing file {0}: {1}
```

### In Java code:
```java
String message = Messages.MyClass_ErrorMessage
    .replace("{0}", filename)
    .replace("{1}", error);
```

Or use MessageFormat:
```java
String message = MessageFormat.format(
    Messages.MyClass_ErrorMessage, 
    filename, 
    error);
```

## Plugin.xml Localization

For plugin.xml strings:

1. Add to plugin properties files (plugin.properties, plugin_de.properties, etc.):
```properties
myNewExtensionName=My Extension Name
```

2. Reference in plugin.xml:
```xml
name="%myNewExtensionName"
```

## Testing

1. Save all files
2. Clean and rebuild the project
3. Test with different locale settings:
   - `-nl de` for German
   - `-nl en` for English
   - `-nl fr` for French
   - `-nl it` for Italian

## Common Issues

### Issue: String not showing localized text
**Solution:** 
- Check that the key in Messages.java matches the key in properties files
- Verify properties files are included in build.properties
- Clean and rebuild the project

### Issue: Missing translation
**Solution:**
- Add the missing key to all properties files
- English (messages.properties) is the fallback

### Issue: Compilation error "cannot be resolved"
**Solution:**
- Ensure Messages.java is in the same package
- Check that NLS.initializeMessages() is called
- Verify bundle name matches the package + "messages"
