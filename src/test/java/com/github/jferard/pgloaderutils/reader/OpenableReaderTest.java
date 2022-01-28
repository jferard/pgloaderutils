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

package com.github.jferard.pgloaderutils.reader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

public class OpenableReaderTest {
    @Test
    public void test() throws IOException {
        final OpenableReader reader = new OpenableReader() {
            private StringReader r;

            @Override
            public void open() {
                this.r = new StringReader("abc");
            }

            @Override
            public int read(final char[] cbuf, final int off, final int len) throws IOException {
                return this.r.read(cbuf, off, len);
            }

            @Override
            public void close() {
                this.r = null;
            }
        };
        reader.open();
        final char[] cbuf = new char[4];
        Assert.assertEquals(3, reader.read(cbuf, 1, 3));
        Assert.assertArrayEquals(new char[] {'\0', 'a', 'b', 'c'}, cbuf);
        final SQLException sqlException = new SQLException("sql");
        final IOException ioException = new IOException("io");
        reader.setException(sqlException);
        reader.setException(ioException);
        Assert.assertEquals(sqlException, reader.getSQLException());
        Assert.assertEquals(ioException, reader.getIOException());
    }

}