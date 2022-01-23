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

import com.github.jferard.pgloaderutils.sql.GeneralDataType;
import org.apache.commons.csv.CSVRecord;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CSVRecord.class)
public class CSVRowsSelectedColsProviderTest {
    @Test
    public void testShortRecord() throws SQLException, ParseException {
        final CSVRecord rec1 = PowerMock.createMock(CSVRecord.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        PowerMock.resetAll();
        EasyMock.expect(rec1.size()).andReturn(2).anyTimes();
        EasyMock.expect(rec1.get(0)).andReturn("foo");
        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setNull(2, Types.VARCHAR);

        PowerMock.replayAll();
        final CSVRowsSelectedColsProvider provider = new CSVRowsSelectedColsProvider(
                Collections.singletonList(rec1).iterator(), Collections.emptyList(),
                (value, type) -> value, i -> i % 2 == 0);
        provider.setStatementParameters(preparedStatement,
                Arrays.asList(GeneralDataType.TEXT, GeneralDataType.TEXT));
        PowerMock.verifyAll();
    }

    @Test
    public void testExpectedRecord() throws SQLException, ParseException {
        final CSVRecord rec1 = PowerMock.createMock(CSVRecord.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        PowerMock.resetAll();
        EasyMock.expect(rec1.size()).andReturn(3).anyTimes();
        EasyMock.expect(rec1.get(0)).andReturn("foo");
        EasyMock.expect(rec1.get(2)).andReturn("baz");
        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, "baz", Types.VARCHAR);

        PowerMock.replayAll();
        final CSVRowsSelectedColsProvider provider = new CSVRowsSelectedColsProvider(
                Collections.singletonList(rec1).iterator(), Collections.emptyList(),
                (value, type) -> value, i -> i % 2 == 0);
        provider.setStatementParameters(preparedStatement,
                Arrays.asList(GeneralDataType.TEXT, GeneralDataType.TEXT));
        PowerMock.verifyAll();
    }

    @Test
    public void testLongRecord() throws SQLException, ParseException {
        final CSVRecord rec1 = PowerMock.createMock(CSVRecord.class);
        final PreparedStatement preparedStatement = PowerMock.createMock(PreparedStatement.class);

        PowerMock.resetAll();
        EasyMock.expect(rec1.size()).andReturn(5).anyTimes();
        EasyMock.expect(rec1.get(0)).andReturn("foo");
        EasyMock.expect(rec1.get(2)).andReturn("baz");
        EasyMock.expect(rec1.get(4)).andReturn("x");
        preparedStatement.setObject(1, "foo", Types.VARCHAR);
        preparedStatement.setObject(2, "baz", Types.VARCHAR);

        PowerMock.replayAll();
        final CSVRowsSelectedColsProvider provider = new CSVRowsSelectedColsProvider(
                Collections.singletonList(rec1).iterator(), Collections.emptyList(),
                (value, type) -> value, i -> i % 2 == 0);
        provider.setStatementParameters(preparedStatement,
                Arrays.asList(GeneralDataType.TEXT, GeneralDataType.TEXT));
        PowerMock.verifyAll();
    }
}