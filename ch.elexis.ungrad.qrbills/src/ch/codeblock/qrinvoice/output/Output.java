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
package ch.codeblock.qrinvoice.output;

import ch.codeblock.qrinvoice.OutputFormat;

public abstract class Output {
    private final OutputFormat outputFormat;
    private final byte[] data;
    private final Integer width;
    private final Integer height;

    protected Output(final OutputFormat outputFormat, final byte[] data, final Integer width, final Integer height) {
        this.outputFormat = outputFormat;
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * @return The size of the {@link #getData()} or -1 if data is null
     */
    public int getSize() {
        if (data == null) {
            return -1;
        }
        return data.length;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * @return The height of a rasterized graphic. If PDF is used as {@link OutputFormat} null is returned
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @return The width of a rasterized graphic. If PDF is used as {@link OutputFormat} null is returned
     */
    public Integer getWidth() {
        return width;
    }
}
