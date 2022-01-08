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

package com.github.jferard.pgloaderutils.loader;

import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import com.github.jferard.pgloaderutils.sql.Table;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class CSVRegularLoader {
    private static final Logger logger = Logger.getLogger(CSVRegularLoader.class.getName());

    /**
     * the CSV file data
     */
    private final CSVParser parser;

    /**
     * The table
     */
    private final Table destTable;

    /** The normalizer */
    private final Normalizer normalizer;

    /**
     * Additional values: not in the file and common to all records, typically the souce name.
     */
    private final List<Object> commonValues;

    /**
     * Index of the first row of data. Typically 1 if the header has one line
     */
    private final int firstRow;

    public CSVRegularLoader(final CSVParser parser, final List<Object> commonValues, final int firstRow,
                            final Table destTable, final Normalizer normalizer) {
        this.parser = parser;
        this.commonValues = commonValues;
        this.firstRow = firstRow;
        this.destTable = destTable;
        this.normalizer = normalizer;
    }

    public void load(final Connection connection, final int batchSize)
            throws IOException, SQLException, ParseException {
        final Statement statement = connection.createStatement();
        statement.execute(this.destTable.disableAllIndicesQuery());
        final PreparedStatement preparedStatement =
                connection.prepareStatement(this.destTable.insertValuesQuery());
        int count = 0;
        final Iterator<CSVRecord> iterator = this.parser.iterator();
        for (int i = 0; i < this.firstRow; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                return;
            }
        }
        while (iterator.hasNext()) {
            final CSVRecord record = iterator.next();
            final int commonSize = this.commonValues.size();
            for (int i = 0; i < commonSize; i++) {
                preparedStatement.setObject(1 + i, this.commonValues.get(i),
                        this.destTable.getType(i).getSqlType());
            }
            for (int i = 0; i < record.size(); i++) {
                final int j = commonSize + i;
                final DataType type = this.destTable.getType(j);
                final Object value = this.normalizer.normalize(record.get(i), type);
                preparedStatement.setObject(1 + j, value, type.getSqlType());
            }
            preparedStatement.addBatch();
            count++;
            if (count != 0 && count % batchSize == 0) {
                CSVRegularLoader.logger.info(String.format("%s rows added", count));
                preparedStatement.executeBatch();
                connection.commit();
            }

        }
        statement.execute(this.destTable.enableAllIndicesQuery());
    }
}
