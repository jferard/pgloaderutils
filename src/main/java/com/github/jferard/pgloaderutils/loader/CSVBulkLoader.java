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

import com.github.jferard.pgloaderutils.reader.OpenableReader;
import com.github.jferard.pgloaderutils.sql.Column;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Format is (@see https://www.postgresql.org/docs/9.1/static/populate.html,
 * 14.4.2 and 14.4.8):
 *
 * <pre>
 * {@code
 * TRUNCATE table
 * COPY FROM stdin
 * ANALYZE table
 * }
 * </pre>
 *
 * @author Julien Férard
 * @see <a href=https://www.postgresql.org/docs/9.1/static/populate.html>the doc</a>
 */
public class CSVBulkLoader {
    public static CSVBulkLoader toTable(final String tableName) {
        return new CSVBulkLoader(String.format("TRUNCATE \"%s\"", tableName),
                String.format("COPY \"%s\" FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')",
                        tableName),
                String.format("ANALYZE \"%s\"", tableName));
    }

    public static CSVBulkLoader toTable(final String tableName, final List<Column> columns) {
        return new CSVBulkLoader(String.format("TRUNCATE \"%s\"", tableName),
                String.format(
                        "COPY \"%s\" %s FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')",
                        tableName, columnsToString(columns)),
                String.format("ANALYZE \"%s\"", tableName));
    }

    public static CSVBulkLoader toTable(final String tableName, final char delimiter,
                                        final char quote) {
        return new CSVBulkLoader(String.format("TRUNCATE \"%s\"", tableName),
                String.format(
                        "COPY \"%s\" FROM STDIN WITH (FORMAT csv, DELIMITER '%s', QUOTE '%s')",
                        tableName, delimiter, quote),
                String.format("ANALYZE \"%s\"", tableName));
    }

    public static CSVBulkLoader toTable(final String tableName, final List<Column> columns,
                                        final char delimiter, final char quote) {
        return new CSVBulkLoader(String.format("TRUNCATE \"%s\"", tableName),
                String.format(
                        "COPY \"%s\" %s FROM STDIN WITH (FORMAT csv, DELIMITER '%s', QUOTE '%s')",
                        tableName, columnsToString(columns), delimiter, quote),
                String.format("ANALYZE \"%s\"", tableName));
    }

    private static String columnsToString(final List<Column> columns) {
        return "(\"" + columns.stream().map(Column::getName)
                .collect(Collectors.joining("\", \"")) + "\")";
    }

    private final String truncateQuery;
    private final String copyQuery;
    private final String analyzeQuery;

    /**
     * @param truncateQuery the query to truncate the table
     * @param copyQuery     the query to copy the table
     * @param analyzeQuery  the query to analyze the table
     */
    CSVBulkLoader(final String truncateQuery, final String copyQuery,
                  final String analyzeQuery) {
        this.truncateQuery = truncateQuery;
        this.copyQuery = copyQuery;
        this.analyzeQuery = analyzeQuery;
    }

    /**
     * Add the data from a CSV file to a PostgreSQL connection
     *
     * @param connection the PostgreSQL connection
     * @param reader     an OpenableReader for a CSV file
     * @param update     true to update an existing table
     * @throws IOException          if an I/O error occurs
     * @throws SQLException if a SQL exception occurs (un-parsable value for instance)
     * @throws InterruptedException if a thread is interrupted.
     */
    public void populate(final Connection connection, final OpenableReader reader,
                         final boolean update)
            throws IOException, SQLException, InterruptedException {
        final boolean storedAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        if (update) {
            final Statement statement = connection.createStatement();
            statement.executeUpdate(this.truncateQuery);
            statement.close();
        }

        final String copyQuery = this.copyQuery;

        final BaseConnection baseConnection = (BaseConnection) connection;
        final CopyManager copyManager = new CopyManager(baseConnection);
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    copyManager.copyIn(copyQuery, reader);
                } catch (final IOException e) {
                    reader.setException(e);
                } catch (final SQLException e) {
                    reader.setException(e);
                } finally {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        reader.setException(e);
                    }
                }
            }
        };
        // ready to copy, but the reader is still not opened
        thread.start();

        // open the floodgates
        reader.open();

        // wait for the end of flood
        thread.join();

        final IOException ioException = reader.getIOException();
        if (ioException != null) {
            throw ioException;
        }
        final SQLException sqlException = reader.getSQLException();
        if (sqlException != null) {
            throw sqlException;
        }

        final Statement statement = connection.createStatement();
        statement.executeUpdate(this.analyzeQuery);
        statement.close();
        connection.commit();
        connection.setAutoCommit(storedAutoCommit);
    }

}
