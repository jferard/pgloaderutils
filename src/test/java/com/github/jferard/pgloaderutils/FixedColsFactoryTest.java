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

package com.github.jferard.pgloaderutils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

public class FixedColsFactoryTest {
    @Test
    public void testOk() throws IOException {
        final FixedColsFactory factory =
                new FixedColsFactory(Arrays.asList("foo", "bar", "baz"));
        final CSVParser parser = new CSVParser(new StringReader("ignore me\r\nbar,bat,baz"),
                CSVFormat.DEFAULT);
        final FixedCols fixedCols = factory.create(parser.getRecords());
        Assert.assertNull(fixedCols.get(0));
        Assert.assertEquals((Integer) 0, fixedCols.get(1));
        Assert.assertEquals((Integer) 2, fixedCols.get(2));
    }
}