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

package com.github.jferard.pgloaderutils.provider;

import com.github.jferard.pgloaderutils.ColSelector;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import org.apache.commons.csv.CSVRecord;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CSVRowsSelectedColsProvider implements RowsProvider {
    /**
     * The normalizer
     */
    private final Normalizer normalizer;
    private final ColSelector selector;

    /**
     * Additional values: not in the file and common to all records, typically the souce name.
     */
    private final List<Object> commonValues;

    private final Iterator<CSVRecord> iterator;
    private CSVRecord curRecord;

    public CSVRowsSelectedColsProvider(final Iterator<CSVRecord> iterator, final List<Object> commonValues,
                                       final Normalizer normalizer, final ColSelector selector) {

        this.iterator = iterator;
        this.commonValues = commonValues;
        this.normalizer = normalizer;
        this.selector = selector;
        this.curRecord = null;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public void setStatementParameters(final PreparedStatement preparedStatement,
                                       final List<DataType> types)
            throws ParseException, SQLException {
        final int commonSize = this.commonValues.size();
        final CSVRecord record = this.iterator.next();
        this.curRecord = record;
        for (int i = 0; i < commonSize; i++) {
            final DataType dataType = types.get(i);
            final Object value = this.commonValues.get(i);
            preparedStatement.setObject(1 + i, value, dataType.getSqlType());
        }
        final int recordSize = record.size();
        int i = commonSize; // column index
        final int colsCount = types.size();
        for (int j = commonSize; j < commonSize + recordSize; j++) { // record index
            final DataType type = types.get(i);
            if (this.selector.select(j)) {
                final Object value = this.normalizer.normalize(record.get(j), type);
                preparedStatement.setObject(1 + i, value, type.getSqlType());
                i++;
                if (i >= colsCount) {
                    return;
                }
            }
        }
        for (int k = i; k < colsCount; k++) { // short record
            final DataType type = types.get(k);
            preparedStatement.setNull(1 + k, type.getSqlType());
        }
    }

    @Override
    public CSVRecord getCurRecord() {
        return this.curRecord;
    }
}
