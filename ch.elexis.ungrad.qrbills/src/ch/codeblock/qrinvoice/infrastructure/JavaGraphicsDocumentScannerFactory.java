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
package ch.codeblock.qrinvoice.infrastructure;

import ch.codeblock.qrinvoice.MimeType;
import ch.codeblock.qrinvoice.document.JavaGraphicsDocumentScanner;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaGraphicsDocumentScannerFactory implements IDocumentScannerFactory {
    private final Set<MimeType> supportedMimeTypes = EnumSet.complementOf(EnumSet.of(MimeType.PDF));
    private final Set<String> supportedMimeTypeStrings = supportedMimeTypes.stream().map(MimeType::getMimeType).collect(Collectors.toSet());

    @Override
    public IDocumentScanner create() {
        return new JavaGraphicsDocumentScanner();
    }

    @Override
    public String getShortName() {
        return getClass().getSimpleName().replace("DocumentScannerFactory", "");
    }

    @Override
    public boolean supports(final Object obj) {
        if (obj instanceof MimeType) {
            return supportedMimeTypes.contains(obj);
        } else if (obj instanceof String) {
            return supportedMimeTypeStrings.contains(obj);
        }
        return false;
    }
}
