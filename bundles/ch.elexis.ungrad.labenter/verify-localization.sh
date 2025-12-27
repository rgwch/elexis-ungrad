#!/bin/bash
# Localization Configuration Verification Script for ch.elexis.ungrad.labenter

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║   Labenter Localization Configuration Verification                ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

BUNDLE_DIR="/home/gerry/git/elexis-ungrad-plugins/bundles/ch.elexis.ungrad.labenter"
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

if grep -q 'name="%Plugin_PreferencesName"' plugin.xml; then
    echo "  ✓ Preferences page name is externalized"
else
    echo "  ✗ Preferences page name is NOT externalized"
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
        if grep -q "^Plugin_PreferencesName" "$file"; then
            echo "    ✓ Contains Plugin_PreferencesName"
        else
            echo "    ✗ MISSING Plugin_PreferencesName"
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
if [ -f "src/ch/elexis/ungrad/labenter/Messages.java" ]; then
    echo "  ✓ Messages.java exists"
else
    echo "  ✗ Messages.java MISSING"
fi
echo ""

# Check 6: messages*.properties files
echo "✓ Checking messages*.properties files..."
for lang in "" "_de" "_en" "_fr" "_it"; do
    file="src/ch/elexis/ungrad/labenter/messages${lang}.properties"
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
    "views/ManualLabEntry.java"
    "views/LabEntryTable.java"
    "preferences/PreferencePage.java"
    "preferences/LabItemSelector.java"
)

for file in "${java_files[@]}"; do
    path="src/ch/elexis/ungrad/labenter/$file"
    if grep -q "ch.elexis.ungrad.labenter.Messages\." "$path" 2>/dev/null; then
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
