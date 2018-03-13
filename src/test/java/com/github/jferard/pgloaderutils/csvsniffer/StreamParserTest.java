/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
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
package com.github.jferard.pgloaderutils.csvsniffer;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class StreamParserTest {

    @Test
    public final void test() throws IOException {
        StreamParser streamParser = new StreamParser(
                new ByteArrayInputStream("line1\nline2\r\nline3\n\rline4".getBytes("ASCII")), 1024);

        Assert.assertEquals("line1", streamParser.getNextLine().toString());
        Assert.assertEquals("line2", streamParser.getNextLine().toString());
        Assert.assertEquals("line3", streamParser.getNextLine().toString());
        Assert.assertEquals("", streamParser.getNextLine().toString());
        Assert.assertEquals("line4", streamParser.getNextLine().toString());
        Assert.assertNull(streamParser.getNextLine());
    }

    @Test
    public final void test3() throws IOException {
        StreamParser streamParser = new StreamParser(
                Resources.getResource("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part" + ".csv").openStream(),
                1024);
    }

}
