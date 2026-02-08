# Encoding Fix for Properties Files

## Issue
The properties files contained UTF-8 encoded characters (like German umlauts: ä, ö, ü) which were displayed as garbage characters (e.g., "fÃ¼r" instead of "für").

## Root Cause
Java properties files **must** use ISO-8859-1 (Latin-1) encoding. Any characters outside this encoding must be represented using Unicode escape sequences (e.g., `\u00fc` for "ü").

## Solution
All special characters in the properties files have been converted to Unicode escape sequences:

### German Characters
- `ä` → `\u00e4`
- `ö` → `\u00f6`
- `ü` → `\u00fc`
- `Ä` → `\u00c4`
- `Ö` → `\u00d6`
- `Ü` → `\u00dc`
- `ß` → `\u00df`

### French Characters
- `é` → `\u00e9`
- `è` → `\u00e8`
- `à` → `\u00e0`
- `ê` → `\u00ea`
- `ç` → `\u00e7`

### Italian Characters
- `à` → `\u00e0`
- `è` → `\u00e8`
- `é` → `\u00e9`
- `ì` → `\u00ec`
- `ò` → `\u00f2`
- `ù` → `\u00f9`

## Verification
All properties files are now ASCII-encoded:
```
messages.properties:    text/plain; charset=us-ascii
messages_de.properties: text/plain; charset=us-ascii
messages_fr.properties: text/plain; charset=us-ascii
messages_it.properties: text/plain; charset=us-ascii
```

## How Java Handles This
When the Java ResourceBundle loads these files, it automatically converts the Unicode escape sequences back to the actual characters. So:
- The file contains: `Zielverzeichnis f\u00fcr PDFs`
- Java displays: `Zielverzeichnis für PDFs`

## Best Practices for Adding New Translations

### Option 1: Use Unicode Escapes (Recommended)
When adding new German text with umlauts, use escape sequences:
```properties
MyNewKey=M\u00fcnchen liegt in S\u00fcddeutschland
```

### Option 2: Use native2ascii Tool
1. Write the text with actual characters in a temporary file
2. Convert using native2ascii:
```bash
native2ascii input.txt output.properties
```

### Option 3: Use Online Converters
Use online Unicode escape converters:
- https://www.rapidtables.com/convert/number/ascii-to-unicode.html
- Or search for "unicode escape converter"

## Common Unicode Escapes Reference

### German
```
ä = \u00e4    Ä = \u00c4
ö = \u00f6    Ö = \u00d6
ü = \u00fc    Ü = \u00dc
ß = \u00df
```

### French
```
à = \u00e0    À = \u00c0
â = \u00e2    Â = \u00c2
é = \u00e9    É = \u00c9
è = \u00e8    È = \u00c8
ê = \u00ea    Ê = \u00ca
ë = \u00eb    Ë = \u00cb
î = \u00ee    Î = \u00ce
ï = \u00ef    Ï = \u00cf
ô = \u00f4    Ô = \u00d4
ù = \u00f9    Ù = \u00d9
û = \u00fb    Û = \u00db
ü = \u00fc    Ü = \u00dc
ç = \u00e7    Ç = \u00c7
```

### Italian
```
à = \u00e0    À = \u00c0
è = \u00e8    È = \u00c8
é = \u00e9    É = \u00c9
ì = \u00ec    Ì = \u00cc
ò = \u00f2    Ò = \u00d2
ù = \u00f9    Ù = \u00d9
```

## Testing
To verify the encoding is correct:
1. Clean and rebuild the project
2. Start Eclipse with the desired locale (e.g., `-nl de`)
3. Check that all special characters display correctly
4. No garbage characters should appear

## Why This Matters
- **Portability:** ASCII files work on all systems
- **No Encoding Issues:** No confusion between UTF-8, ISO-8859-1, etc.
- **Standard Practice:** This is the official Java Properties file format
- **Tool Compatibility:** All Java tools expect this format

## Status
✅ All properties files have been fixed and are now using proper Unicode escapes.
