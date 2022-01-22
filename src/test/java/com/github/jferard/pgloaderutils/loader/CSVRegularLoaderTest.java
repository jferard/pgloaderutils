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

import com.github.jferard.pgloaderutils.CSVData;
import com.github.jferard.pgloaderutils.HeaderColSelectorFactory;
import com.github.jferard.pgloaderutils.provider.CSVRowsProvider;
import com.github.jferard.pgloaderutils.provider.RowsProvider;
import com.github.jferard.pgloaderutils.sql.Column;
import com.github.jferard.pgloaderutils.sql.GeneralDataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import com.github.jferard.pgloaderutils.sql.Table;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CSVRegularLoaderTest {
    @Test
    public void test() throws IOException, SQLException, ParseException {
        final Table t =
                new Table("table",
                        Collections.singletonList(new Column("foo", GeneralDataType.INTEGER)));
        final Normalizer normalizer = (value, type) -> Integer.valueOf(value);
        final Iterator<CSVRecord> iterator =
                CSVFormat.DEFAULT.parse(new StringReader("foo\n1\n2")).iterator();
        iterator.next();
        final RowsProvider rp = new CSVRowsProvider(
                iterator,
                Collections.emptyList(),
                normalizer);
        final CSVRegularLoader rl = new CSVRegularLoader(rp, t);

        final Connection connection = PowerMock.createMock(Connection.class);
        final Statement statement = PowerMock.createMock(Statement.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        PowerMock.resetAll();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=false\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        EasyMock.expect(connection.prepareStatement("INSERT INTO \"table\" VALUES (\n" +
                "?\n" +
                ")")).andReturn(preparedStatement);
        preparedStatement.setObject(1, 1, Types.INTEGER);
        preparedStatement.addBatch();
        preparedStatement.setObject(1, 2, Types.INTEGER);
        preparedStatement.addBatch();
        EasyMock.expect(preparedStatement.executeBatch()).andReturn(new int[] {1, 1});
        connection.commit();
        connection.setAutoCommit(true);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=true\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);

        PowerMock.replayAll();
        final List<CSVRecord> recs = rl.load(connection, 10);

        PowerMock.verifyAll();
        Assert.assertTrue(recs.isEmpty());
    }

    @Test
    public void testBadRow() throws IOException, SQLException, ParseException {
        final Table t =
                new Table("table",
                        Collections.singletonList(new Column("foo", GeneralDataType.INTEGER)));
        final Normalizer normalizer = (value, type) -> Integer.valueOf(value);
        final Iterator<CSVRecord> iterator =
                CSVFormat.DEFAULT.parse(new StringReader("foo\n1\nA")).iterator();
        iterator.next();
        final RowsProvider rp = new CSVRowsProvider(
                iterator,
                Collections.emptyList(),
                normalizer);
        final CSVRegularLoader rl = new CSVRegularLoader(rp, t);

        final Connection connection = PowerMock.createMock(Connection.class);
        final Statement statement = PowerMock.createMock(Statement.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        PowerMock.resetAll();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=false\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        EasyMock.expect(connection.prepareStatement("INSERT INTO \"table\" VALUES (\n" +
                "?\n" +
                ")")).andReturn(preparedStatement);
        preparedStatement.setObject(1, 1, Types.INTEGER);
        preparedStatement.addBatch();
        EasyMock.expect(preparedStatement.executeBatch()).andReturn(new int[] {1, 1});
        connection.commit();
        connection.setAutoCommit(true);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=true\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);

        PowerMock.replayAll();
        final List<CSVRecord> recs = rl.load(connection, 10);

        PowerMock.verifyAll();
        Assert.assertEquals(1, recs.size());
        Assert.assertEquals(Collections.singletonList("A"), Lists.newArrayList(recs.get(0)));
    }

    @Test
    public void testToRegularLoader() throws IOException, SQLException {
        final Connection connection = PowerMock.createMock(Connection.class);
        final Statement statement = PowerMock.createMock(Statement.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        final CSVParser parser =
                new CSVParser(new StringReader("a,b,c\n1,2,3\n4,5,6\n7,8,9"), CSVFormat.DEFAULT);
        final CSVData csvData = new CSVData(parser, Collections.singletonList("foo"), 1,
                (value, type) -> (type == GeneralDataType.TEXT ? value + "*" :
                        Integer.valueOf(value)));
        final CSVRegularLoader reader =
                csvData.toRegularLoader(new Table("table", Arrays.asList(
                        new Column("foo", GeneralDataType.TEXT),
                        new Column("a", GeneralDataType.INTEGER),
                        new Column("b", GeneralDataType.TEXT),
                        new Column("c", GeneralDataType.TEXT)
                )));

        PowerMock.resetAll();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=false\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        EasyMock.expect(connection.prepareStatement("INSERT INTO \"table\" VALUES (\n" +
                "?, ?, ?, ?\n" +
                ")")).andReturn(preparedStatement);
        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 1, Types.INTEGER);
        preparedStatement.setObject(3, "2*", Types.VARCHAR);
        preparedStatement.setObject(4, "3*", Types.VARCHAR);
        preparedStatement.addBatch();

        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 4, Types.INTEGER);
        preparedStatement.setObject(3, "5*", Types.VARCHAR);
        preparedStatement.setObject(4, "6*", Types.VARCHAR);
        preparedStatement.addBatch();

        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 7, Types.INTEGER);
        preparedStatement.setObject(3, "8*", Types.VARCHAR);
        preparedStatement.setObject(4, "9*", Types.VARCHAR);
        preparedStatement.addBatch();
        EasyMock.expect(preparedStatement.executeBatch()).andReturn(new int[] {1, 1, 1});
        connection.commit();

        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=true\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        connection.setAutoCommit(true);

        PowerMock.replayAll();
        reader.load(connection, 10);

        PowerMock.verifyAll();
    }

    @Test
    public void testToRegularLoaderSelectCols() throws IOException, SQLException {
        final Connection connection = PowerMock.createMock(Connection.class);
        final Statement statement = PowerMock.createMock(Statement.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        final CSVParser parser =
                new CSVParser(new StringReader("a,b,c\n1,2,3\n4,5,6\n7,8,9"), CSVFormat.DEFAULT);
        final CSVData csvData = new CSVData(parser, Collections.singletonList("foo"), 1,
                (value, type) -> (type == GeneralDataType.TEXT ? value + "*" :
                        Integer.valueOf(value)));
        final CSVRegularLoader reader =
                csvData.toRegularLoader(new Table("table", Arrays.asList(
                        new Column("foo", GeneralDataType.TEXT),
                        new Column("a", GeneralDataType.INTEGER),
                        new Column("c", GeneralDataType.TEXT)
                )), new HeaderColSelectorFactory(Arrays.asList("a", "c")));

        PowerMock.resetAll();
        EasyMock.expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        EasyMock.expect(connection.createStatement()).andReturn(statement);
        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=false\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        EasyMock.expect(connection.prepareStatement("INSERT INTO \"table\" VALUES (\n" +
                "?, ?, ?\n" +
                ")")).andReturn(preparedStatement);
        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 1, Types.INTEGER);
        preparedStatement.setObject(3, "3*", Types.VARCHAR);
        preparedStatement.addBatch();

        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 4, Types.INTEGER);
        preparedStatement.setObject(3, "6*", Types.VARCHAR);
        preparedStatement.addBatch();
        EasyMock.expect(preparedStatement.executeBatch()).andReturn(new int[] {1, 1});
        connection.commit();

        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, 7, Types.INTEGER);
        preparedStatement.setObject(3, "9*", Types.VARCHAR);
        preparedStatement.addBatch();
        EasyMock.expect(preparedStatement.executeBatch()).andReturn(new int[] {1});
        connection.commit();

        EasyMock.expect(statement.execute("UPDATE pg_index\n" +
                "SET indisready=true\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")")).andReturn(true);
        connection.setAutoCommit(true);

        PowerMock.replayAll();
        reader.load(connection, 2);

        PowerMock.verifyAll();
    }
}
