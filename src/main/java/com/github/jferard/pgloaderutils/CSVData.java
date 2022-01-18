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

import com.github.jferard.pgloaderutils.loader.CSVRegularLoader;
import com.github.jferard.pgloaderutils.provider.CSVRowsProvider;
import com.github.jferard.pgloaderutils.provider.CSVRowsSelectedColsProvider;
import com.github.jferard.pgloaderutils.reader.CSVCleanerFileReader;
import com.github.jferard.pgloaderutils.reader.CSVRecordCleaner;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import com.github.jferard.pgloaderutils.sql.Table;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CSVData {
    /**
     * the CSV file data
     */
    private final CSVParser parser;

    /**
     * The normalizer
     */
    private final Normalizer normalizer;

    /**
     * Additional values: not in the file and common to all records, typically the souce name.
     */
    private final List<Object> commonValues;

    /**
     * Index of the first row of data. Typically 1 if the header has one line
     */
    private final int firstRow;

    public CSVData(final CSVParser parser, final List<Object> commonValues, final int firstRow,
                   final Normalizer normalizer) {
        this.parser = parser;
        this.commonValues = commonValues;
        this.firstRow = firstRow;
        this.normalizer = normalizer;
    }

    /**
     * @param destTable The table
     * @return a loader
     */
    public CSVRegularLoader toRegularLoader(final Table destTable) {
        this.skipFirstRows();
        return new CSVRegularLoader(
                new CSVRowsProvider(this.parser.iterator(), this.commonValues, this.normalizer),
                destTable);
    }

    /**
     * @param destTable The table
     * @param factory a factory to create the set of selected cols
     * @return a loader
     */
    public CSVRegularLoader toRegularLoader(final Table destTable,
                                            final ColSelectorFactory factory) {
        final List<CSVRecord> firstRows = this.skipFirstRows();
        final ColSelector selector = factory.create(firstRows);
        return new CSVRegularLoader(
                new CSVRowsSelectedColsProvider(this.parser.iterator(), this.commonValues,
                        this.normalizer, selector), destTable);
    }

    /**
     * @param destTable The table
     */
    public CSVCleanerFileReader asOpenableReader(final Table destTable) throws IOException {
        final List<Object> commonValues = this.commonValues;
        final Normalizer normalizer = this.normalizer;
        this.skipFirstRows();
        final List<DataType> types = destTable.getTypes();
        return new CSVCleanerFileReader(this.parser, new CSVRecordCleaner() {
            @Override
            public Iterable<String> cleanRecord(final CSVRecord record) {
                final int commonSize = commonValues.size();
                final List<String> ret = new ArrayList<>(commonSize + record.size());
                for (final Object commonValue : commonValues) {
                    ret.add(commonValue.toString());
                }
                for (int i = 0; i < record.size(); i++) {
                    final int j = commonSize + i;
                    final DataType type = types.get(j);
                    try {
                        final Object value = normalizer.normalize(record.get(i), type);
                        ret.add(value.toString());
                    } catch (final ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                return ret;
            }
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
