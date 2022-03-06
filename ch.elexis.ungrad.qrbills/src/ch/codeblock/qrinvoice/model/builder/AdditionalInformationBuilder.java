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
package ch.codeblock.qrinvoice.model.builder;

import ch.codeblock.qrinvoice.model.AdditionalInformation;
import ch.codeblock.qrinvoice.model.billinformation.BillInformation;
import ch.codeblock.qrinvoice.model.parser.BillInformationParser;

public final class AdditionalInformationBuilder {
    private String unstructuredMessage;
    private String trailer;
    private BillInformation billInformation;

    private AdditionalInformationBuilder() {
    }

    public static AdditionalInformationBuilder create() {
        return new AdditionalInformationBuilder();
    }

    /**
     * @param unstructuredMessage Unstructured Message to set
     * @return AdditionalInformationBuilder instance
     * @see AdditionalInformation#getUnstructuredMessage()
     */
    public AdditionalInformationBuilder unstructuredMessage(String unstructuredMessage) {
        this.unstructuredMessage = unstructuredMessage;
        return this;
    }

    public String getUnstructuredMessage() {
        return unstructuredMessage;
    }

    /**
     * @param trailer Trailer to set
     * @return AdditionalInformationBuilder instance
     * @see AdditionalInformation#getTrailer()
     */
    protected AdditionalInformationBuilder trailer(String trailer) {
        this.trailer = trailer;
        return this;
    }

    /**
     * @param billInformation Bill Information to set
     * @return AdditionalInformationBuilder instance
     * @see AdditionalInformation#getBillInformation()
     */
    public AdditionalInformationBuilder billInformation(String billInformation) {
        this.billInformation = BillInformationParser.create().parseBillInformation(billInformation);
        return this;
    }

    /**
     * @param billInformation Bill Information to set
     * @return AdditionalInformationBuilder instance
     * @see AdditionalInformation#getBillInformation()
     */
    public AdditionalInformationBuilder billInformation(BillInformation billInformation) {
        this.billInformation = billInformation;
        return this;
    }

    /**
     * @return The created {@link AdditionalInformation} based on the data set to this builder
     */
    public AdditionalInformation build() {
        AdditionalInformation additionalInformation = new AdditionalInformation();
        additionalInformation.setUnstructuredMessage(unstructuredMessage);
        additionalInformation.setTrailer(trailer);
        if (billInformation != null) {
            additionalInformation.setBillInformation(billInformation.toBillInformationString());
            additionalInformation.setBillInformationObject(billInformation);
        }
        return additionalInformation;
    }
}
