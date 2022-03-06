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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * File path and resource path information for fonts and their styles
 */
public class FontFamilyInformation {
    /**
     * Regular expression pattern for the font name (Taken from the font metadata)
     */
    private final Pattern baseNamePattern;

    /**
     * Collection of font names per style
     */
    private final Map<FontStyle, String> fontNames = new EnumMap<>(FontStyle.class);

    /**
     * Collection of possible file names for the TTF files per FontStyle without file extension
     */
    private final Map<FontStyle, Collection<String>> fileNames = new EnumMap<>(FontStyle.class);

    /**
     * Font is embedded in the core library, thus is a classpath resource
     */
    private boolean embedded;

    /**
     * Collection of possible file names for the TTF files per FontStyle without file extension
     */
    private final Map<FontStyle, String> resourcePaths = new EnumMap<>(FontStyle.class);

    /**
     * Collection of explicitly given absolute file paths to ttf files with extension
     */
    private final Map<FontStyle, String> explicitFileNames = new EnumMap<>(FontStyle.class);


    public FontFamilyInformation(String baseNamePattern, String plainName, String boldName, String plainResourcePath, String boldResourcePath) {
        this.baseNamePattern = Pattern.compile(baseNamePattern, Pattern.CASE_INSENSITIVE);
        this.embedded = true;
        fontNames.put(FontStyle.REGULAR, plainName);
        fontNames.put(FontStyle.BOLD, boldName);
        resourcePaths.put(FontStyle.REGULAR, plainResourcePath);
        resourcePaths.put(FontStyle.BOLD, boldResourcePath);
    }

    public FontFamilyInformation(String baseNamePattern, String plainName, String boldName, Collection<String> plainFileNames, Collection<String> boldFileNames) {
        this.baseNamePattern = Pattern.compile(baseNamePattern, Pattern.CASE_INSENSITIVE);
        this.embedded = false;
        fontNames.put(FontStyle.REGULAR, plainName);
        fontNames.put(FontStyle.BOLD, boldName);
        fileNames.put(FontStyle.REGULAR, plainFileNames);
        fileNames.put(FontStyle.BOLD, boldFileNames);
    }

    public void setExplicitFilePaths(String pathToRegularTtfFile, String pathToBoldTtfFile) {
        explicitFileNames.put(FontStyle.REGULAR, pathToRegularTtfFile);
        explicitFileNames.put(FontStyle.BOLD, pathToBoldTtfFile);

        embedded = false;
    }

    public Pattern getBaseNamePattern() {
        return baseNamePattern;
    }

    public String getFontName(FontStyle fontStyle) {
        return fontNames.get(fontStyle);
    }

    public Collection<String> getFileNames(FontStyle fontStyle) {
        return fileNames.get(fontStyle);
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean hasExplicitFilePaths() {
        return !explicitFileNames.isEmpty();
    }

    public String getResourcePath(FontStyle fontStyle) {
        return resourcePaths.get(fontStyle);
    }

    public String getExplicitFilePath(FontStyle fontStyle) {
        return explicitFileNames.get(fontStyle);
    }
}
