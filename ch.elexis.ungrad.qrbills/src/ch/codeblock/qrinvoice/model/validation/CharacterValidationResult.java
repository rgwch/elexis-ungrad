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
package ch.codeblock.qrinvoice.model.validation;

import ch.codeblock.qrinvoice.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CharacterValidationResult implements Serializable {
    private static final long serialVersionUID = 8210114959570284979L;
    
    private final String input;
    private List<InvalidCharacterSequence> invalidCharacterSequences;

    public CharacterValidationResult(final String input) {
        this.input = input;
    }

    public void addInvalidCharacterSequence(final InvalidCharacterSequence invalidCharacterSequence) {
        // do not accept duplicates
        if (!getInvalidCharacterSequences().contains(invalidCharacterSequence)) {
            getInvalidCharacterSequences().add(invalidCharacterSequence);
        }
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(invalidCharacterSequences);
    }

    public boolean isValid() {
        return isEmpty();
    }

    public boolean hasInvalidCharacters() {
        return !isEmpty();
    }

    public String getInput() {
        return input;
    }

    public List<InvalidCharacterSequence> getInvalidCharacterSequences() {
        if (invalidCharacterSequences == null) {
            invalidCharacterSequences = new ArrayList<>();
        }
        return invalidCharacterSequences;
    }


    public String getSummary() {
        if (hasInvalidCharacters()) {
            final StringBuilder sb = new StringBuilder();
            for (final InvalidCharacterSequence invalidCharacterSequence : getInvalidCharacterSequences()) {
                if(sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(invalidCharacterSequence.getErrorSummary());
            }

            return sb.toString();
        } else {
            return "No invalid characters found";
        }
    }

}
