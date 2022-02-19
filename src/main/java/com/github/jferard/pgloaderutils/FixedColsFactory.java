/*
 * Some utilities for loading csv data into a PostgreSQL database:
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FixedColsFactory {
    private final List<String> colNames;

    public FixedColsFactory(final List<String> colNames) {
        this.colNames = colNames;
    }

    public FixedCols create(final List<CSVRecord> firstRows) {
        final int size = firstRows.size();
        assert size > 0;
        final CSVRecord lastRow = firstRows.get(size - 1);
        final Map<String, Integer>
                recordIndexByRecordFieldName = IntStream.range(0, lastRow.size()).boxed()
                .collect(Collectors.toMap(lastRow::get, Function.identity()));
        final Map<Integer, Integer> recordIndexByFixedIndex = new HashMap<>();
        for (int i = 0; i < this.colNames.size(); i++) {
            final String colName = this.colNames.get(i);
            final Integer j = recordIndexByRecordFieldName.get(colName);
            if (j != null) {
                recordIndexByFixedIndex.put(i, j);
            }
        }
        return new FixedCols(this.colNames.size(), recordIndexByFixedIndex);
    }
}