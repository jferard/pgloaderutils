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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

public class SimpleFileReaderTest {
    @Test
    public void test() throws IOException {
        final StringReader sr = new StringReader("foo");
        final SimpleFileReader sfr = new SimpleFileReader(sr, Logger.getLogger("test"), 1);
        sfr.open();
        Assert.assertEquals('f', sfr.read());
        final char[] buffer = new char[3];
        final int read = sfr.read(buffer, 0, 2);
        Assert.assertEquals (2, read);
        Assert.assertArrayEquals(new char[] {'o', 'o', 0}, buffer);
        sfr.close();
    }
}