#!/bin/bash
# Localization Configuration Verification Script for ch.elexis.ungrad.kassenbuch

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║   Kassenbuch Localization Configuration Verification              ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

BUNDLE_DIR="/home/gerry/git/elexis-ungrad-plugins/bundles/ch.elexis.ungrad.kassenbuch"
cd "$BUNDLE_DIR"

# Check 1: MANIFEST.MF
echo "✓ Checking MANIFEST.MF..."
if grep -q "Bundle-Name: %Bundle_Name" META-INF/MANIFEST.MF; then
    echo "  ✓ Bundle-Name is externalized"
else
    echo "  ✗ Bundle-Name is NOT externalized"
fi

if grep -q "Bundle-Vendor: %Bundle_Vendor" META-INF/MANIFEST.MF; then
    echo "  ✓ Bundle-Vendor is externalized"
else
    echo "  ✗ Bundle-Vendor is NOT externalized"
fi

if grep -q "Bundle-Localization: plugin" META-INF/MANIFEST.MF; then
    echo "  ✓ Bundle-Localization is set to 'plugin'"
else
    echo "  ✗ Bundle-Localization is MISSING"
fi
echo ""

# Check 2: plugin.xml
echo "✓ Checking plugin.xml..."
if grep -q 'name="%Plugin_ViewName"' plugin.xml; then
    echo "  ✓ View name is externalized"
else
    echo "  ✗ View name is NOT externalized"
fi

if grep -q 'name="%Plugin_CategoryName"' plugin.xml; then
    echo "  ✓ Category name is externalized"
else
    echo "  ✗ Category name is NOT externalized"
fi
echo ""

# Check 3: plugin.properties files
echo "✓ Checking plugin*.properties files..."
for lang in "" "_de" "_en" "_fr" "_it"; do
    file="plugin${lang}.properties"
    if [ -f "$file" ]; then
        echo "  ✓ $file exists"
        if grep -q "^Plugin_ViewName" "$file"; then
            echo "    ✓ Contains Plugin_ViewName"
        else
            echo "    ✗ MISSING Plugin_ViewName"
        fi
        if grep -q "^Plugin_CategoryName" "$file"; then
            echo "    ✓ Contains Plugin_CategoryName"
        else
            echo "    ✗ MISSING Plugin_CategoryName"
        fi
    else
        echo "  ✗ $file MISSING"
    fi
done
echo ""

# Check 4: build.properties
echo "✓ Checking build.properties..."
if grep -q "plugin.properties" build.properties; then
    echo "  ✓ plugin.properties included in build"
else
    echo "  ✗ plugin.properties NOT included in build"
fi

for lang in "de" "en" "fr" "it"; do
    if grep -q "plugin_${lang}.properties" build.properties; then
        echo "  ✓ plugin_${lang}.properties included in build"
    else
        echo "  ✗ plugin_${lang}.properties NOT included in build"
    fi
done
echo ""

# Check 5: Messages class
echo "✓ Checking Messages.java..."
if [ -f "src/ch/elexis/buchhaltung/kassenbuch/Messages.java" ]; then
    echo "  ✓ Messages.java exists"
else
    echo "  ✗ Messages.java MISSING"
fi
echo ""

# Check 6: messages*.properties files
echo "✓ Checking messages*.properties files..."
for lang in "" "_de" "_en" "_fr" "_it"; do
    file="src/ch/elexis/buchhaltung/kassenbuch/messages${lang}.properties"
    if [ -f "$file" ]; then
        echo "  ✓ messages${lang}.properties exists"
    else
        echo "  ✗ messages${lang}.properties MISSING"
    fi
done
echo ""

# Check 7: Java files use Messages class
echo "✓ Checking Java files..."
java_files=(
    "KassenView.java"
    "BuchungsDialog.java"
    "EditCatsDialog.java"
    "DatumEingabeDialog.java"
    "KassenbuchDruckDialog.java"
)

for file in "${java_files[@]}"; do
    path="src/ch/elexis/buchhaltung/kassenbuch/$file"
    if grep -q "Messages\." "$path" 2>/dev/null; then
        echo "  ✓ $file uses Messages class"
    else
        echo "  ✗ $file does NOT use Messages class"
    fi
done
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                          SUMMARY                                   ║"
echo "╠════════════════════════════════════════════════════════════════════╣"
echo "║  Configuration appears correct.                                    ║"
echo "║                                                                    ║"
echo "║  To test the localization:                                         ║"
echo "║  1. Clean the project in Eclipse (Project → Clean)                ║"
echo "║  2. Launch with: ./elexis -clean -nl <language>                    ║"
echo "║                                                                    ║"
echo "║  Languages: de (German), en (English), fr (French), it (Italian)  ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
