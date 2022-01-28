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

import com.github.jferard.pgloaderutils.loader.CSVRegularLoader;
import com.github.jferard.pgloaderutils.provider.CSVRowsProvider;
import com.github.jferard.pgloaderutils.reader.CSVProcessorFileReader;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Table;
import com.github.jferard.pgloaderutils.sql.ValueConverter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVData {
    /**
     * the CSV file data
     */
    private final CSVParser parser;

    /**
     * The value converter
     */
    private final ValueConverter converter;

    /**
     * Additional values: not in the file and common to all records, typically the source name.
     */
    private final List<Object> commonValues;

    /**
     * Index of the first row of data. Typically 1 if the header has one line
     */
    private final int firstRow;

    public CSVData(final CSVParser parser, final List<Object> commonValues, final int firstRow,
                   final ValueConverter converter) {
        this.parser = parser;
        this.commonValues = commonValues;
        this.firstRow = firstRow;
        this.converter = converter;
    }

    /**
     * @param destTable The table
     * @return a loader
     */
    public CSVRegularLoader toRegularLoader(final Table destTable) {
        this.skipFirstRows();
        return new CSVRegularLoader(
                CSVRowsProvider.create(this.parser.iterator(), this.commonValues, this.converter),
                destTable);
    }

    /**
     * @param destTable The table
     * @param factory   a factory to create the set of selected cols
     * @return a loader
     */
    public CSVRegularLoader toRegularLoader(final Table destTable,
                                            final ColSelectorFactory factory) {
        final List<CSVRecord> firstRows = this.skipFirstRows();
        final ColSelector selector = factory.create(firstRows);
        return new CSVRegularLoader(
                CSVRowsProvider.create(this.parser.iterator(), this.commonValues,
                        this.converter, selector), destTable);
    }

    /**
     * @param destTable       The table
     * @param recordProcessor a record processor
     * @return a loader
     */
    public CSVRegularLoader toRegularLoader(final Table destTable,
                                            final CSVRecordProcessor recordProcessor) {
        this.skipFirstRows();
        return new CSVRegularLoader(
                new CSVRowsProvider(this.parser.iterator(), this.commonValues,
                        this.converter, recordProcessor), destTable);
    }

    /**
     * @param destTable The table
     * @return an openable reader for bulk loading
     * @throws IOException if an I/O error occurs
     */
    public CSVProcessorFileReader asOpenableReader(final Table destTable) throws IOException {
        final List<Object> commonValues = this.commonValues;
        final ValueConverter converter = this.converter;
        this.skipFirstRows();
        final List<DataType> types = destTable.getTypes();
        return new CSVProcessorFileReader(this.parser, record -> {
            final int commonSize = commonValues.size();
            final List<String> ret = new ArrayList<>(commonSize + record.size());
            for (final Object commonValue : commonValues) {
                ret.add(Util.toPGString(commonValue));
            }
            for (int i = 0; i < record.size(); i++) {
                final int j = commonSize + i;
                final DataType type = types.get(j);
                final Object value = converter.toJavaObject(record.get(i), type);
                ret.add(Util.toPGString(value));
            }
            return ret;
        });
    }

    /**
     * @return the header rows
     */
    private List<CSVRecord> skipFirstRows() {
        final ArrayList<CSVRecord> ret = new ArrayList<>(this.firstRow);
        final Iterator<CSVRecord> iterator = this.parser.iterator();
        for (int i = 0; i < this.firstRow; i++) {
            if (iterator.hasNext()) {
                ret.add(iterator.next());
            } else {
                break;
            }
        }
        return ret;
    }
}
