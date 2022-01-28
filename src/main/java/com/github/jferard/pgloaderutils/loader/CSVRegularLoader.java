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

package com.github.jferard.pgloaderutils.loader;

import com.github.jferard.pgloaderutils.Util;
import com.github.jferard.pgloaderutils.provider.RowsProvider;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Table;
import org.apache.commons.csv.CSVRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVRegularLoader {
    private static final Logger logger = Logger.getLogger(CSVRegularLoader.class.getName());

    private final RowsProvider rowsProvider;
    /**
     * The table
     */
    private final Table destTable;

    public CSVRegularLoader(final RowsProvider rowsProvider, final Table destTable) {
        this.rowsProvider = rowsProvider;
        this.destTable = destTable;
    }

    /**
     * Load the data
     * @param connection the connection
     * @param batchSize the batch size
     * @return the list of ignored records
     * @throws SQLException if an SQL error occurs (!)
     */
    public List<CSVRecord> load(final Connection connection, final int batchSize)
            throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }
        final PreparedStatement indexStatement = connection.prepareStatement(Table.indIsReadyQuery());
        this.setIndIsReady(indexStatement, false);

        final PreparedStatement insertStatement =
                connection.prepareStatement(this.destTable.insertValuesQuery());
        final List<DataType> types = this.destTable.getTypes();
        int count = 0;
        final List<CSVRecord> ignoredRecords = new ArrayList<>();
        while (this.rowsProvider.hasNext()) {
            try {
                this.rowsProvider.setStatementParameters(insertStatement, types);
                insertStatement.addBatch();
                count++;
                if (count != 0 && count % batchSize == 0) {
                    CSVRegularLoader.logger.info(String.format("%s rows added", count));
                    insertStatement.executeBatch();
                    connection.commit();
                }
            } catch (final RuntimeException | SQLException e) {
                final CSVRecord ignoredRecord = this.rowsProvider.getCurRecord();
                CSVRegularLoader.logger.log(Level.SEVERE,
                        String.format("Error when adding record %s", ignoredRecord), e);
                ignoredRecords.add(ignoredRecord);
            }
        }
        CSVRegularLoader.logger.info(String.format("%s rows added", count));
        insertStatement.executeBatch();
        connection.commit();
        this.setIndIsReady(indexStatement, true);
        final Statement statement = connection.createStatement();
        statement.execute("REINDEX TABLE "+ Util.pgEscapeIdentifier(this.destTable.getName()));
        connection.commit();
        if (autoCommit) {
            connection.setAutoCommit(true);
        }
        return ignoredRecords;
    }

    private void setIndIsReady(final PreparedStatement indexStatement, final boolean indIsReady)
            throws SQLException {
        indexStatement.setBoolean(1, indIsReady);
        indexStatement.setString(2, this.destTable.getName());
        indexStatement.execute();
    }

}
