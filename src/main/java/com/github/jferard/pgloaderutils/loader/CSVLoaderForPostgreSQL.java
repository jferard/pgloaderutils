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

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Format is (@see https://www.postgresql.org/docs/9.1/static/populate.html,
 * 14.4.2 & 14.4.8):
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
 * @see https://www.postgresql.org/docs/9.1/static/populate.html
 */
public class CSVLoaderForPostgreSQL {
    public static CSVLoaderForPostgreSQL toTable(final String tableName) {
        return new CSVLoaderForPostgreSQL("TRUNCATE " + tableName,
                "COPY " + tableName
                        + " FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')",
                "ANALYZE " + tableName);
    }

    public static CSVLoaderForPostgreSQL toTable(final String tableName, final String columns) {
        return new CSVLoaderForPostgreSQL("TRUNCATE " + tableName,
                "COPY " + tableName + " " + columns
                        + " FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')",
                "ANALYZE " + tableName);
    }

    public static CSVLoaderForPostgreSQL toTable(final String tableName, final char delimiter,
                                                 final char quote) {
        return new CSVLoaderForPostgreSQL("TRUNCATE " + tableName,
                "COPY " + tableName
                        + " FROM STDIN WITH (FORMAT csv, DELIMITER '" + delimiter + "', QUOTE '" +
                        quote + "')",
                "ANALYZE " + tableName);
    }

    public static CSVLoaderForPostgreSQL toTable(final String tableName, final String columns,
                                                 final char delimiter, final char quote) {
        return new CSVLoaderForPostgreSQL("TRUNCATE " + tableName,
                "COPY " + tableName + " " + columns
                        + " FROM STDIN WITH (FORMAT csv, DELIMITER '" + delimiter + "', QUOTE '" +
                        quote + "')",
                "ANALYZE " + tableName);
    }
    private final String truncateQuery;
    private final String copyQuery;
    private final String analyzeQuery;

    /**
     * @param truncateQuery
     * @param copyQuery
     * @param analyzeQuery
     */
    CSVLoaderForPostgreSQL(final String truncateQuery, final String copyQuery,
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
     * @throws IOException          if an I/O error occurs
     * @throws SQLException
     * @throws InterruptedException
     */
    public void populate(final Connection connection, final OpenableReader reader)
            throws IOException, SQLException, InterruptedException {
        final boolean storedAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        Statement statement = connection.createStatement();
        statement.executeUpdate(this.truncateQuery);
        statement.close();

        final String copyQuery = this.copyQuery;

		final BaseConnection baseConnection = (BaseConnection) connection;
		final CopyManager copyManager = new CopyManager(baseConnection);
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					copyManager.copyIn(copyQuery, reader);
				} catch (final SQLException e) {
					e.printStackTrace();
				} catch (final IOException e) {
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

        final IOException exception = reader.getException();
        if (exception != null) {
            throw exception;
        }

        statement = connection.createStatement();
        statement.executeUpdate(this.analyzeQuery);
        statement.close();
        connection.commit();
        connection.setAutoCommit(storedAutoCommit);
    }

}
