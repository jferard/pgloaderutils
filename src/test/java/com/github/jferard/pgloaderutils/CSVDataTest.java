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

import com.github.jferard.pgloaderutils.reader.CSVProcessorFileReader;
import com.github.jferard.pgloaderutils.sql.Column;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.GeneralDataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import com.github.jferard.pgloaderutils.sql.Table;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

public class CSVDataTest {
    @Test
    public void testAsOpenableReader() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("a,b,c\n1,2,3"), CSVFormat.DEFAULT);
        final CSVData csvData = new CSVData(parser, Collections.singletonList("foo"), 1, new Normalizer() {
            @Override
            public Object normalize(final String value, final DataType type) throws ParseException {
                return value + "*";
            }
        });
        final CSVProcessorFileReader reader =
                csvData.asOpenableReader(new Table("table", Arrays.asList(
                        new Column("foo", GeneralDataType.TEXT),
                        new Column("a", GeneralDataType.TEXT),
                        new Column("b", GeneralDataType.TEXT),
                        new Column("c", GeneralDataType.TEXT)
                )));
        reader.open();
        Assert.assertEquals("foo,1*,2*,3*\r\n", TestHelper.readAll(reader));
        reader.close();
    }

    @Test
    public void testErr() throws IOException {
        final CSVParser parser = new CSVParser(new StringReader("a,b,c\n1,2,3\n4,5,C\n7,8,9"), CSVFormat.DEFAULT);
        final CSVData csvData = new CSVData(parser, Collections.emptyList(), 1,
                (value, type) -> Integer.parseInt(value));
        final CSVProcessorFileReader reader =
                csvData.asOpenableReader(new Table("table", Arrays.asList(
                        new Column("a", GeneralDataType.INTEGER),
                        new Column("b", GeneralDataType.INTEGER),
                        new Column("c", GeneralDataType.INTEGER)
                )));
        reader.open();
        Assert.assertEquals("1,2,3\r\n7,8,9\r\n", TestHelper.readAll(reader));
        reader.close();
        Assert.assertEquals(1, reader.getIgnoredRecords().size());
        Assert.assertEquals(Arrays.asList("4", "5", "C"), Lists.newArrayList(reader.getIgnoredRecords().get(0)));
    }
}