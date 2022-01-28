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
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

public class HeaderColSelectorFactoryTest {
    @Test
    public void testErr() {
        final HeaderColSelectorFactory factory =
                new HeaderColSelectorFactory(Arrays.asList("foo", "bar", "baz"));
        Assert.assertThrows(AssertionError.class, () -> factory.create(Collections.emptyList()));
    }

    @Test
    public void testOk() throws IOException {
        final HeaderColSelectorFactory factory =
                new HeaderColSelectorFactory(Arrays.asList("foo", "bar", "baz"));
        final CSVParser parser = new CSVParser(new StringReader("ignore me\r\nbar,bat,baz"),
                CSVFormat.DEFAULT);
        final ColSelector selector = factory.create(parser.getRecords());
        Assert.assertTrue(selector.select(0));
        Assert.assertFalse(selector.select(1));
        Assert.assertTrue(selector.select(2));
        for (int i = 3; i<10; i++) {
            Assert.assertFalse(selector.select(i));
        }
    }
}