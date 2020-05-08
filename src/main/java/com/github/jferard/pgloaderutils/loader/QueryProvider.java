/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
 *
 * This file is part of pgLoader Utils.
 *
 * pgLoader Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pgLoader Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jferard.pgloaderutils.loader;

import java.util.Map;

/**
 * Replaces keys in a template query. The template has the format: "some text {key} some other text".
 * If valueByKey[key] == value, then the template is converted to "some text value some other text"
 *
 * @author Julien Férard
 */
public class QueryProvider {
    /**
     * @param template the template, keys are written between brackets.
     * @param valueByKey the map key->value
     * @return the query
     */
    public String newQuery(final String template, final Map<String, String> valueByKey) {
        final StringBuilder queryBuilder = new StringBuilder();

        int lastJ = 0;
        int i = template.indexOf('{');
        while (i >= 0) {
            final int j = template.indexOf('}', i);
            if (j == -1) {
                throw new IllegalStateException(template);
            }
            final String before = template.substring(lastJ, i);
            final String key = template.substring(i+1, j);
            queryBuilder.append(before);
            final String value = valueByKey.get(key);
            if (value == null) {
                throw new IllegalStateException(template+" % "+valueByKey+" missing "+key);
            }

            queryBuilder.append(value);

            lastJ = j+1;
            i = template.indexOf('{', lastJ);
        }
        queryBuilder.append(template.substring(lastJ));
        return queryBuilder.toString();
    }
}
