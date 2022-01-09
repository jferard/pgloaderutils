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

import com.github.jferard.pgloaderutils.provider.RowsProvider;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.Table;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.List;
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

    public void load(final Connection connection, final int batchSize)
            throws IOException, SQLException, ParseException {
        connection.setAutoCommit(false);
        final Statement statement = connection.createStatement();
        statement.execute(this.destTable.disableAllIndicesQuery());
        final PreparedStatement preparedStatement =
                connection.prepareStatement(this.destTable.insertValuesQuery());
        final List<DataType> types = this.destTable.getTypes();
        int count = 0;
        while (this.rowsProvider.hasNext()) {
            this.rowsProvider.setStatementParameters(preparedStatement, types);
            preparedStatement.addBatch();
            count++;
            if (count != 0 && count % batchSize == 0) {
                CSVRegularLoader.logger.info(String.format("%s rows added", count));
                preparedStatement.executeBatch();
                connection.commit();
            }
        }
        CSVRegularLoader.logger.info(String.format("%s rows added", count));
        preparedStatement.executeBatch();
        connection.commit();
        statement.execute(this.destTable.enableAllIndicesQuery()); // + REINDEX "name";
        connection.setAutoCommit(true);
    }

}
