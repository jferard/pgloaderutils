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

import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import org.apache.commons.csv.CSVRecord;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

public class CSVRowsProvider implements RowsProvider {
    /**
     * The normalizer
     */
    private final Normalizer normalizer;

    /**
     * Additional values: not in the file and common to all records, typically the souce name.
     */
    private final List<Object> commonValues;

    private final Iterator<CSVRecord> iterator;
    private CSVRecord curRecord;

    public CSVRowsProvider(final Iterator<CSVRecord> iterator, final List<Object> commonValues,
                           final Normalizer normalizer) {

        this.iterator = iterator;
        this.commonValues = commonValues;
        this.normalizer = normalizer;
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
        final int remainingColumnsCount = types.size() - commonSize;
        if (recordSize < remainingColumnsCount) {
            for (int i = commonSize; i < commonSize + recordSize; i++) {
                final DataType type = types.get(i);
                final int j = i - commonSize; // record index
                final Object value = this.normalizer.normalize(record.get(j), type);
                preparedStatement.setObject(1 + i, value, type.getSqlType());
            }
            // short record: set last cols to null
            for (int i = commonSize + recordSize; i < commonSize + remainingColumnsCount; i++) {
                final DataType type = types.get(i);
                preparedStatement.setNull(1 + i, type.getSqlType());
            }
        } else {
            // long record: ignore last values
            for (int i = commonSize; i < commonSize + remainingColumnsCount; i++) {
                final DataType type = types.get(i);
                final int j = i - commonSize; // record index
                final Object value = this.normalizer.normalize(record.get(j), type);
                preparedStatement.setObject(1 + i, value, type.getSqlType());
            }
        }
    }

    @Override
    public CSVRecord getCurRecord() {
        return this.curRecord;
    }
}
