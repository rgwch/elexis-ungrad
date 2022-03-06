/*-
 * #%L
 * QR Invoice Solutions
 * %%
 * Copyright (C) 2017 - 2022 Codeblock GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * -
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * -
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * -
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses are available for this software. These replace the above
 * AGPLv3 terms and offer support, maintenance and allow the use in commercial /
 * proprietary products.
 * -
 * More information on commercial licenses are available at the following page:
 * https://www.qr-invoice.ch/licenses/
 * #L%
 */
package ch.codeblock.qrinvoice.fonts;

import ch.codeblock.qrinvoice.FontFamily;
import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ch.codeblock.qrinvoice.FontFamily.*;
import static java.util.Arrays.asList;

/**
 * FontManager assists in retrieving Fonts and may provide a fallback if System Font is not available
 */
public class FontManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FontManager.class);
    private static final Map<FontFamily, Boolean> SYSTEM_FONT_PRESENT = Collections.synchronizedMap(new EnumMap<>(FontFamily.class));
    private static final Map<FontFamily, FontFamilyInformation> FONT_FAMILY_INFORMATION_MAPPPING = Collections.synchronizedMap(new EnumMap<>(FontFamily.class));
    private static final Map<String, byte[]> EMBEDDED_FONT_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> FONT_PATHS = new LinkedHashSet<>(SystemFonts.initFontPaths());

    private static final Map<FontFamily, Map<FontStyle, String>> FONT_FAMILY_FILE_PATH_MAPPING = Collections.synchronizedMap(new EnumMap<>(FontFamily.class));

    static {
        registerFonts();
        registerEmbeddedFonts();
    }

    private static void registerFonts() {
        final String fontDir = "/ch/codeblock/qrinvoice/fonts/liberation_sans/";
        FONT_FAMILY_INFORMATION_MAPPPING.put(LIBERATION_SANS, new FontFamilyInformation("Liberation Sans", "Liberation Sans", "Liberation Sans Bold", fontDir + "LiberationSans-Regular.ttf", fontDir + "LiberationSans-Bold.ttf"));
        FONT_FAMILY_INFORMATION_MAPPPING.put(HELVETICA, new FontFamilyInformation("Helvetica", "Helvetica", "Helvetica-Bold", asList("Helvetica"), asList("Helvetica-Bold", "HelveticaBd", "Helvetica-Bold")));
        FONT_FAMILY_INFORMATION_MAPPPING.put(ARIAL, new FontFamilyInformation("Arial", "Arial", "Arial Bold", asList("Arial"), asList("Arial Bold", "ArialBd", "Arial-Bold")));
        FONT_FAMILY_INFORMATION_MAPPPING.put(FRUTIGER, new FontFamilyInformation("Frutiger LT Pro (45 Light|65 Bold)", "Frutiger Light", "Frutiger Bold", asList("FrutigerLTPro-Light"), asList("FrutigerLTPro-Bold")));
    }

    private static void registerEmbeddedFonts() {
        final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        FONT_FAMILY_INFORMATION_MAPPPING.values().stream().filter(FontFamilyInformation::isEmbedded).forEach(f -> {
            for (final FontStyle fontStyle : FontStyle.values()) {
                try {
                    g.registerFont(Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream(f.getResourcePath(fontStyle))));
                    LOGGER.info("Embedded Font={} style={} registered", f.getBaseNamePattern().pattern(), fontStyle);
                } catch (FontFormatException | IOException e) {
                    throw new TechnicalException(String.format("Unable to register embedded Font %s at resource path %s", f.getBaseNamePattern().pattern(), f.getResourcePath(fontStyle)), e);

                }
            }
        });
    }

    public static void eagerInitialization() {
        final FontFamily[] fontFamilies = FontFamily.values();
        for (final FontFamily fontFamily : fontFamilies) {
            checkFontFamily(fontFamily);
            for (final FontStyle fontStyle : FontStyle.values()) {
                try {
                    getFontPath(fontFamily, fontStyle);
                } catch (TechnicalException e) {
                    if (!isEmbedded(fontFamily)) {
                        LOGGER.info("FontFamily={} style={} - {}", fontFamily, fontStyle, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Set font paths for a given font family
     *
     * @param fontFamily
     * @param pathToRegularTtfFile Full path to the TTF file containing the regular / plain variant
     * @param pathToBoldTtfFile    Full path to the TTF file containing the bold variant
     */
    public static void setFontPath(FontFamily fontFamily, String pathToRegularTtfFile, String pathToBoldTtfFile) {
        FontFamilyInformation fontFamilyInformation = FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily);

        fontFamilyInformation.setExplicitFilePaths(pathToRegularTtfFile, pathToBoldTtfFile);

        // Remove cached file path information
        FONT_FAMILY_FILE_PATH_MAPPING.remove(fontFamily);

        LOGGER.trace("FontFamily={} has manual file paths set, re-registering embedded fonts", fontFamily);
    }

    /**
     * Used for testing only
     */
    /* default */
    static void reset() {
        SYSTEM_FONT_PRESENT.clear();
        FONT_FAMILY_INFORMATION_MAPPPING.clear();
        FONT_FAMILY_FILE_PATH_MAPPING.clear();
        FONT_PATHS.clear();
        FONT_PATHS.addAll(SystemFonts.initFontPaths());
        registerFonts();
    }

    /**
     * @param fontFamily The Font Family
     * @param fontStyle  The Font Style
     * @return The name of the font
     */
    public static String getFontName(FontFamily fontFamily, FontStyle fontStyle) {
        return FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily).getFontName(fontStyle);
    }

    private static boolean checkFontFamily(final FontFamily fontFamily) {
        final String fontName = getFontName(fontFamily, FontStyle.REGULAR);
        final Font resolvedFont = new Font(fontName, Font.PLAIN, 1);
        if (resolvedFont.getFamily().equalsIgnoreCase(fontName)) {
            LOGGER.trace("FontFamily={} is installed", fontFamily);
            return true;
        } else {
            LOGGER.warn("FontFamily={} is not installed", fontFamily);
            LOGGER.warn("FontFamily={} resolved to '{}'", fontFamily, resolvedFont.getFamily());
            if (LOGGER.isTraceEnabled()) {
                final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
                final String[] fonts = g.getAvailableFontFamilyNames();
                LOGGER.trace("Installed font families:");
                for (final String f : fonts) {
                    LOGGER.trace("- {}", f);
                }
            }
            return false;
        }
    }

    public static boolean isEmbedded(final FontFamily fontFamily) {
        return FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily).isEmbedded();
    }

    public static String getEmbeddedTtfFileName(final FontFamily fontFamily, final FontStyle fontStyle) {
        final String resourcePath = getEmbeddedTtfFilePath(fontFamily, fontStyle);

        // "/ch/codeblock/qrinvoice/fonts/liberation_sans/LiberationSans-Regular.ttf" -> "LiberationSans-Regular.ttf"
        return resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
    }

    public static String getEmbeddedTtfFilePath(final FontFamily fontFamily, final FontStyle fontStyle) {
        final FontFamilyInformation info = FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily);
        if (!info.isEmbedded()) {
            throw new TechnicalException(String.format("Font %s is not embedded", fontFamily));
        }

        return info.getResourcePath(fontStyle);
    }

    /**
     * Returns the byte[] of an embedded ttf class path resource - uses caching - byte[] is constructed only once
     *
     * @param fontFamily
     * @param fontStyle
     * @return the ttf file as byte[]
     */
    public static byte[] getEmbeddedTtf(final FontFamily fontFamily, final FontStyle fontStyle) {
        final FontFamilyInformation info = FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily);
        if (!info.isEmbedded()) {
            throw new TechnicalException(String.format("Font %s is not embedded", fontFamily));
        }

        final String resourcePath = info.getResourcePath(fontStyle);
        return EMBEDDED_FONT_CACHE.computeIfAbsent(resourcePath, FontManager::loadEmbeddedTtf);
    }

    private static byte[] loadEmbeddedTtf(final String resourcePath) {
        try (InputStream is = FontManager.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new TechnicalException(String.format("Unable to read embedded Font from resource path %s", resourcePath), e);
        }
    }

    public static Font getFont(FontFamily fontFamily, FontStyle fontStyle) {
        final boolean systemFontPresent = SYSTEM_FONT_PRESENT.computeIfAbsent(fontFamily, FontManager::checkFontFamily);
        if (systemFontPresent && !SystemFonts.systemFontsIgnored()) {
            return getJavaFontByName(fontFamily, fontStyle);
        } else {
            return getJavaFontByScannedPaths(fontFamily, fontStyle);
        }
    }

    public static String getFontPath(FontFamily fontFamily, FontStyle fontStyle) {
        return FONT_FAMILY_FILE_PATH_MAPPING.computeIfAbsent(fontFamily, f -> Collections.synchronizedMap(new EnumMap<>(FontStyle.class))).computeIfAbsent(fontStyle, f -> findFontPath(fontFamily, fontStyle));
    }

    private static String findFontPath(FontFamily fontFamily, FontStyle fontStyle) {
        // First check if absolute paths have been given manually
        FontFamilyInformation fontFamilyInformation = FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily);

        if (fontFamilyInformation.hasExplicitFilePaths()) {
            String manualFilePath = fontFamilyInformation.getExplicitFilePath(fontStyle);

            // Try creating the font
            try {
                final Font font = Font.createFont(Font.TRUETYPE_FONT, new File(manualFilePath));
                LOGGER.info("FontFamily={} style={} was manually set to TTF file={})", fontFamily, fontStyle, manualFilePath);
            } catch (FontFormatException | IOException e) {
                LOGGER.warn("Unable to create Java Font from TTF file={}", manualFilePath);
                throw fontNotFoundException(fontFamily, fontStyle, e);
            }
            return manualFilePath;
        }

        // Match font automatically
        final Stream<Path> pathStream = FONT_PATHS.stream().map(Paths::get).filter(Files::exists);
        final Stream<Path> ttfStream = pathStream.map(FontManager::toTtfStreamMapper).filter(Objects::nonNull).flatMap(p -> p);

        return ttfStream
                .filter(filterTtfFileNames(FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily).getFileNames(fontStyle)))
                .filter(tryCreateFontFilter(fontFamily, fontStyle))
                .findFirst()
                .orElseThrow(() -> fontNotFoundException(fontFamily, fontStyle, null))
                .toFile().getAbsolutePath();
    }

    private static Predicate<Path> filterTtfFileNames(final Collection<String> fontFileNames) {
        return ttfFile -> {
            final boolean ttfMatches = fontFileNames != null && fontFileNames.stream().anyMatch(fontFileName -> ttfFile.toFile().getName().equalsIgnoreCase(fontFileName + ".ttf"));
            LOGGER.trace("TTF filename of '{}' matches: {}", ttfFile, ttfMatches);
            return ttfMatches;
        };
    }

    private static Stream<Path> toTtfStreamMapper(Path path) {
        try {
            return Files.find(path, 5, (filePath, attr) -> attr.isRegularFile() && filePath.getFileName().toString().toLowerCase().endsWith(".ttf"));
        } catch (IOException e) {
            LOGGER.warn("Unexpected Exception", e);
            return null;
        }
    }

    private static Predicate<Path> tryCreateFontFilter(final FontFamily fontFamily, final FontStyle fontStyle) {
        return ttfFile -> {
            try {
                final Font font = Font.createFont(Font.TRUETYPE_FONT, ttfFile.toFile());
                final Pattern baseName = FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily).getBaseNamePattern();
                if (baseName.matcher(font.getFamily()).matches()) {
                    LOGGER.info("FontFamily={} style={} was matched in TTF file={} (expected baseName={} equals {})", fontFamily, fontStyle, ttfFile, baseName, font.getFamily());
                    return true;
                } else {
                    LOGGER.info("FontFamily={} style={} did not match in TTF file={} (expected baseName={} equals {})", fontFamily, fontStyle, ttfFile, baseName, font.getFamily());
                    return false;
                }
            } catch (FontFormatException | IOException e) {
                LOGGER.warn("Unable to create Java Font from TTF file={}", ttfFile);
                return false;
            }
        };
    }

    private static Font getJavaFontByName(final FontFamily fontFamily, final FontStyle fontStyle) {
        final String fontName = getFontBaseName(fontFamily);
        switch (fontStyle) {
            case BOLD:
                return new Font(fontName, Font.BOLD, 1);
            case REGULAR:
            default:
                return new Font(fontName, Font.PLAIN, 1);
        }
    }

    private static String getFontBaseName(final FontFamily fontFamily) {
        return FONT_FAMILY_INFORMATION_MAPPPING.get(fontFamily).getBaseNamePattern().pattern();
    }

    private static Font getJavaFontByScannedPaths(final FontFamily fontFamily, final FontStyle fontStyle) {
        try {
            final String ttfFile = getFontPath(fontFamily, fontStyle);
            if (ttfFile != null) {
                final File fontFile = new File(ttfFile);
                final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                return font.deriveFont(fontStyle == FontStyle.REGULAR ? Font.PLAIN : Font.BOLD);
            } else {
                throw fontNotFoundException(fontFamily, fontStyle, null);
            }
        } catch (FontFormatException | IOException e) {
            throw fontNotFoundException(fontFamily, fontStyle, e);
        }
    }

    private static TechnicalException fontNotFoundException(final FontFamily fontFamily, final FontStyle fontStyle, final Exception e) {
        return new TechnicalException(String.format("Font %s with fontStyle %s could not be found on the system", fontFamily, fontStyle), e);
    }

    private FontManager() {
    }
}
