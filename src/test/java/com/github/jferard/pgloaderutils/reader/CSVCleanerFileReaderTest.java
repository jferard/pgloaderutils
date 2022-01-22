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

package com.github.jferard.pgloaderutils.reader;

import com.github.jferard.pgloaderutils.TestHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVCleanerFileReaderTest {
    @Test
    public void test() throws IOException {
        final CSVProcessorFileReader r =
                CSVProcessorFileReader.fromReader(new StringReader("a;b;c\n1,0;2;3"),
                        CSVFormat.RFC4180.withDelimiter(';'), new CSVRecordProcessor() {
                            @Override
                            public Iterable<String> cleanRecord(final CSVRecord record) {
                                final List<String> ret = new ArrayList<>(record.size());
                                ret.add(0, record.get(0).replace(',', '.'));
                                ret.add(1, record.get(1));
                                ret.add(2, record.get(2));
                                return ret;
                            }
                        });
        r.open();
        Assert.assertEquals("a,b,c\r\n" +
                "1.0,2,3\r\n", TestHelper.readAll(r));
        r.close();
    }

    @Test
    public void testFromStream() throws IOException {
        final CSVProcessorFileReader r =
                CSVProcessorFileReader.fromStream(new ByteArrayInputStream("a;b;c\n1,0;2;3".getBytes(
                                StandardCharsets.UTF_8)), StandardCharsets.UTF_8,
                        CSVFormat.RFC4180.withDelimiter(';'), record -> {
                            final List<String> ret = new ArrayList<>(record.size());
                            ret.add(0, record.get(0).replace(',', '.'));
                            ret.add(1, record.get(1));
                            ret.add(2, record.get(2));
                            return ret;
                        });
        r.open();
        Assert.assertEquals("a,b,c\r\n" +
                "1.0,2,3\r\n", TestHelper.readAll(r));
        r.close();
    }
}