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

import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class ServiceProvider {
    private static final ServiceProvider INSTANCE = new ServiceProvider();

    public static ServiceProvider getInstance() {
        return INSTANCE;
    }

    private ServiceProvider() {
    }

    private final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    public <T extends Service<?>> List<T> getAll(final Class<T> interfaceType) {
        final List<T> allServiceProviders = new LinkedList<>();
        ServiceLoader.load(interfaceType).iterator().forEachRemaining(allServiceProviders::add);
        return allServiceProviders;
    }

    public <U, T extends Service<U>> T get(final Class<T> interfaceType, final U obj) {
        // caching might help here
        final List<T> allServices = getAll(interfaceType);
        if (allServices.isEmpty()) {
            throw new TechnicalException("Missing implementation of " + interfaceType.getName() + ". Likely there is a maven module missing");
        }

        final List<T> filteredServices = allServices.stream().filter(s -> s.supports(obj)).collect(Collectors.toList());

        T selectedService;
        if (filteredServices.isEmpty()) {
            throw new TechnicalException("No service implementation is able to support the given parameters. Make sure you have all required modules added.");
        } else if (filteredServices.size() == 1) {
            selectedService = filteredServices.get(0);
        } else {
            selectedService = resolvePreferred(interfaceType, filteredServices);
            if (selectedService == null) {
                selectedService = filteredServices.get(0);
                logger.warn("There is currently more than one implementation of {} available at runtime. The following has been chosen, as it was the first in list: {}", interfaceType.getName(), selectedService.getClass());
            }
        }

        return selectedService;
    }

    private <T extends Service<?>> T resolvePreferred(final Class<T> interfaceType, final List<T> allServices) {
        final String preferredImplementation = System.getProperty(interfaceType.getCanonicalName());
        if (StringUtils.isNotEmpty(preferredImplementation)) {
            for (final T serviceProvider : allServices) {
                if (serviceProvider.getClass().getCanonicalName().equals(preferredImplementation)) {
                    logger.info("Selected {} as the implementation for {} - preferred using System-Property", serviceProvider.getClass(), interfaceType);
                    return serviceProvider;
                }
            }
        }
        return null;
    }
}
