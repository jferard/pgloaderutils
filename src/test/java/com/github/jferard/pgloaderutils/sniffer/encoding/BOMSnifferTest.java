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

package com.github.jferard.pgloaderutils.sniffer.encoding;

import com.github.jferard.pgloaderutils.sniffer.encoding.BOMSniffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class BOMSnifferTest {
    @Test
    public void testUTF16LE() throws IOException {
        this.test_aux(BOMSniffer.UTF_16LE, BOMSniffer.UTF_16LE_BOM);
    }

    @Test
    public void testUTF32LE() throws IOException {
        this.test_aux(BOMSniffer.UTF_32LE, BOMSniffer.UTF_32LE_BOM);
    }

    @Test
    public void testUTF16BE() throws IOException {
        this.test_aux(BOMSniffer.UTF_16BE, BOMSniffer.UTF_16BE_BOM);
    }

    @Test
    public void testUTF8() throws IOException {
        this.test_aux(BOMSniffer.UTF_8, BOMSniffer.UTF_8_BOM);
    }

    private void test_aux(final Charset cs, final byte[] bom) throws IOException {
        final InputStream is = this.getInputStream("test", cs, bom);
        Assert.assertEquals(cs, BOMSniffer.getCharset(is));
        Assert.assertEquals("test", this.getString(is, cs));
    }

    /**
     * Takes a string and a charset and return the byte sequence with bom.
     *
     * @param s   the string
     * @param cs  a charset
     * @param bom a bom
     * @return the bytes: bom + encoded string in charset
     */
    InputStream getInputStream(final String s, final Charset cs, final byte[] bom) {
        final byte[] bytes = s.getBytes(cs);
        final int bomLen = bom.length;
        final int bytesLen = bytes.length;
        final byte[] newBytes = new byte[bomLen + bytesLen];
        System.arraycopy(bom, 0, newBytes, 0, bomLen);
        System.arraycopy(bytes, 0, newBytes, bomLen, bytesLen);
        return new ByteArrayInputStream(newBytes);
    }

    /**
     * Fill a string with the stream content
     * @param is the stream
     * @param cs the charset
     * @return the string
     * @throws IOException if an I/O error occurs
     */
    String getString(final InputStream is, final Charset cs) throws IOException {
        final Reader reader = new InputStreamReader(is, cs);
        final StringBuilder builder = new StringBuilder();
        final char[] chars = new char[100];
        while (true) {
            final int charsRead = reader.read(chars, 0, chars.length);
            if (charsRead == -1) {
                break;
            }
            builder.append(chars, 0, charsRead);
        }
        return builder.toString();
    }
}
