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

package com.github.jferard.pgloaderutils.sql;

import com.github.jferard.pgloaderutils.RecordsStore;
import com.github.jferard.pgloaderutils.TestHelper;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class QueryMapParserTest {
    @Test
    public void test() throws IOException {
        final StringReader r = new StringReader("\"\"\"query1\n" +
                "SELECT * FROM %s\n" +
                "\"\"\"\n" +
                "\n" +
                "\"\"\"text\n" +
                "Some\n" +
                "\"random\"\n" +
                "text\n" +
                "\"\"\"");
        Assert.assertEquals(ImmutableMap.of("text", "Some\n" +
                        "\"random\"\n" +
                        "text", "query1", "SELECT * FROM %s"),
                QueryMapParser.parse(r));
    }

    @Test
    public void testBlankLines() throws IOException {
        final StringReader r = new StringReader("\r\n\"\"\"query1\n" +
                "SELECT * FROM %s\n" +
                "\"\"\"\n" +
                "\n\n" +
                "\"\"\"text\n" +
                "Some\n" +
                "\"random\"\n" +
                "text\n" +
                "\"\"\"\n\n\n\n");
        Assert.assertEquals(ImmutableMap.of("text", "Some\n" +
                        "\"random\"\n" +
                        "text", "query1", "SELECT * FROM %s"),
                QueryMapParser.parse(r));
    }

    @Test
    public void testGarbage() throws IOException {
        final RecordsStore store = TestHelper.configLog(QueryMapParser.class);

        final StringReader reader = new StringReader("A\r\n\"\"\"query1\n" +
                "SELECT * FROM %s;\n" +
                "\"\"\"\n" +
                "\nB\n" +
                "\"\"\"text\n" +
                "Some\n" +
                "\"random\"\n" +
                "text;\n" +
                "\"\"\"C\n\n\n\nD");
        Assert.assertEquals(ImmutableMap.of("text", "Some\n" +
                        "\"random\"\n" +
                        "text", "query1", "SELECT * FROM %s"),
                QueryMapParser.parse(reader));

        Assert.assertEquals(
                Arrays.asList("Ignore garbage at line 1: A", "Ignore garbage at line 6: B",
                        "Ignore garbage at line 11: C", "Ignore garbage at line 15: D"),
                store.getRecords().stream().map(LogRecord::getMessage)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testWrongQuoteCount() throws IOException {
        final RecordsStore store = TestHelper.configLog(QueryMapParser.class);

        final StringReader reader = new StringReader("\"\"\"query1\n" +
                "SELECT * FROM %s;\n" +
                "\"\"\n" +
                "\"\"\"query2\n" +
                "SELECT * FROM %s;\n" +
                "\"\"\"");
        Assert.assertEquals(ImmutableMap.of("query1", "SELECT * FROM %s;\n\"\""),
                QueryMapParser.parse(reader));

        Assert.assertEquals(
                Arrays.asList("Query not closed at line 3: 2 quotes",
                        "Ignore garbage at line 4: query2",
                        "Ignore garbage at line 5: SELECT * FROM %s;"),
                store.getRecords().stream().map(LogRecord::getMessage)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testDontClose() throws IOException {
        final RecordsStore store = TestHelper.configLog(QueryMapParser.class);

        final StringReader reader = new StringReader("\"\"\"query1\n" +
                "SELECT * FROM %s;\n");
        Assert.assertEquals(ImmutableMap.of("query1", "SELECT * FROM %s"),
                QueryMapParser.parse(reader));

        Assert.assertEquals(
                Arrays.asList("Missing closing quotes at line 3"),
                store.getRecords().stream().map(LogRecord::getMessage)
                        .collect(Collectors.toList()));
    }
}