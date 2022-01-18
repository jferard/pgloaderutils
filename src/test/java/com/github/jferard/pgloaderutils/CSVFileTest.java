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

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@RunWith(PowerMockRunner.class)
@PrepareForTest(CSVFile.class)
public class CSVFileTest {
    @Test
    public void test() throws IOException {
        PowerMock.mockStatic(Files.class);
        final Path path = PowerMock.createMock(Path.class);
        final CSVFile file = new CSVFile(path, StandardCharsets.UTF_8, CSVFormat.DEFAULT);

        PowerMock.resetAll();
        EasyMock.expect(Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .andReturn(new BufferedReader(new StringReader("a,b,c\r\n1,2,3")));

        PowerMock.replayAll();
        final CSVParser parser = file.newCSVParser();
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(Arrays.asList("a", "b", "c"), Lists.newArrayList(records.get(0)));
        Assert.assertEquals(Arrays.asList("1", "2", "3"), Lists.newArrayList(records.get(1)));

        PowerMock.verifyAll();
    }

    @Test
    public void testBOM() throws IOException {
        PowerMock.mockStatic(Files.class);
        final Path path = PowerMock.createMock(Path.class);
        final CSVFile file = new CSVFile(path, StandardCharsets.UTF_8, CSVFormat.DEFAULT);

        PowerMock.resetAll();
        EasyMock.expect(Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .andReturn(new BufferedReader(new StringReader("\uFEFFa,b,c\r\n1,2,3")));

        PowerMock.replayAll();
        final CSVParser parser = file.newCSVParser();
        final List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(Arrays.asList("a", "b", "c"), Lists.newArrayList(records.get(0)));
        Assert.assertEquals(Arrays.asList("1", "2", "3"), Lists.newArrayList(records.get(1)));

        PowerMock.verifyAll();
    }
}