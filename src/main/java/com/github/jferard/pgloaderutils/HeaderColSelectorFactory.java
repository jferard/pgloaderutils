/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018, 2020-2022 J. Férard <https://github.com/jferard>
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

package com.github.jferard.pgloaderutils;

import org.apache.commons.csv.CSVRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class HeaderColSelectorFactory implements ColSelectorFactory {
    private final Set<String> colNames;

    public HeaderColSelectorFactory(final List<String> colNames) {
        this.colNames = colNames.stream().map(String::trim).collect(Collectors.toSet());
    }

    @Override
    public ColSelector create(final List<CSVRecord> firstRows) {
        final int size = firstRows.size();
        assert size > 0;
        final CSVRecord lastRow = firstRows.get(size - 1);
        final Set<Integer> indices = new HashSet<>(this.colNames.size());
        for (int i = 0; i<lastRow.size(); i++) {
            if (this.colNames.contains(lastRow.get(i).trim())) {
                indices.add(i);
            }
        }
        return new SetColSelector(indices);
    }
}
