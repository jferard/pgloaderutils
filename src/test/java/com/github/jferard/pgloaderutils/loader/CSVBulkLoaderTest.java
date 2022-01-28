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

import com.github.jferard.pgloaderutils.reader.SimpleFileReader;
import com.github.jferard.pgloaderutils.sql.Column;
import com.github.jferard.pgloaderutils.sql.GeneralDataType;
import org.easymock.EasyMock;
import org.junit.Test;
import org.postgresql.copy.CopyIn;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.powermock.api.easymock.PowerMock;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CSVBulkLoaderTest {
    @Test
    public void test1() throws IOException, SQLException, InterruptedException {
        final CSVBulkLoader bl = CSVBulkLoader.toTable("table");
        this.aTest(bl, "COPY \"table\" FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')");
    }

    @Test
    public void test2() throws IOException, SQLException, InterruptedException {
        final CSVBulkLoader bl = CSVBulkLoader.toTable("table", ';', '"');
        this.aTest(bl, "COPY \"table\" FROM STDIN WITH (FORMAT csv, DELIMITER ';', QUOTE '\"')");
    }

    @Test
    public void test3() throws IOException, SQLException, InterruptedException {
        final List<Column> columns = Arrays.asList(new Column("foo",
                GeneralDataType.TEXT), new Column("bar", GeneralDataType.INTEGER));
        final CSVBulkLoader bl = CSVBulkLoader.toTable("table", columns);
        this.aTest(bl, "COPY \"table\" (\"foo\", \"bar\") FROM STDIN WITH (FORMAT csv, DELIMITER ',', QUOTE '\"')");
    }

    @Test
    public void test4() throws IOException, SQLException, InterruptedException {
        final List<Column> columns = Arrays.asList(new Column("foo",
                GeneralDataType.TEXT), new Column("bar", GeneralDataType.INTEGER));
        final CSVBulkLoader bl = CSVBulkLoader.toTable("table", columns, ';', '"');
        this.aTest(bl, "COPY \"table\" (\"foo\", \"bar\") FROM STDIN WITH (FORMAT csv, DELIMITER ';', QUOTE '\"')");
    }

    private void aTest(final CSVBulkLoader bl, final String copySQL)
            throws SQLException, IOException, InterruptedException {
        final BaseConnection connection = PowerMock.createMock(BaseConnection.class);
        final Statement statement1 = PowerMock.createMock(Statement.class);
        final Statement statement2 = PowerMock.createMock(Statement.class);
        final QueryExecutor queryExecutor = PowerMock.createMock(QueryExecutor.class);
        final CopyIn copyOp = PowerMock.createMock(CopyIn.class);

        PowerMock.resetAll();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement1);
        EasyMock.expect(statement1.executeUpdate("TRUNCATE \"table\"")).andReturn(1);
        statement1.close();
        EasyMock.expect(connection.getEncoding()).andReturn(Encoding.defaultEncoding());
        EasyMock.expect(connection.getQueryExecutor()).andReturn(queryExecutor);
        EasyMock.expect(connection.getAutoCommit()).andReturn(false);
        EasyMock.expect(queryExecutor.startCopy(
                        copySQL, false))
                .andReturn(copyOp);
        // might fail
        copyOp.writeToCopy(new byte[]{'a', ',', 'b', ',', 'c'}, 0, 5);
        EasyMock.expect(copyOp.endCopy()).andReturn(1l);
        EasyMock.expect(copyOp.isActive()).andReturn(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement2);
        EasyMock.expect(statement2.executeUpdate("ANALYZE \"table\"")).andReturn(1);
        statement2.close();
        connection.commit();
        connection.setAutoCommit(true);

        PowerMock.replayAll();
        bl.populate(connection,
                new SimpleFileReader(new StringReader("a,b,c"), Logger.getLogger("test"), 1), true);

        PowerMock.verifyAll();
    }

}