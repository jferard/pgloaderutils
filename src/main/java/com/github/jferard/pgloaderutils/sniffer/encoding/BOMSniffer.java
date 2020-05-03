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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A BOMSniffer creates a ByteArraysMatcher that tests the beginning of a
 * stream.
 */
public class BOMSniffer {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    public static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    public static final byte[] UTF_8_BOM;
    public static final byte[] UTF_16BE_BOM;
    public static final byte[] UTF_16LE_BOM;
    public static final byte[] UTF_32BE_BOM;
    public static final byte[] UTF_32LE_BOM;
    private static final byte FF;
    private static final byte FE;
    private static final byte BB;
    private static final byte BF;
    private static final byte EF;
    private static final byte ZERO;

    /**
     * Detect a Charset BOM in an InputStream. If a BOM is found, it is skipped.
     *
     * @param is the InputStream. Must support mark
     * @return the charset found from BOM, null if none
     * @throws IOException
     */
    public static Charset getCharset(InputStream is) throws IOException {
        if (!is.markSupported()) {
            throw new IllegalArgumentException();
        }

        BOMSniffer sniffer = BOMSniffer.create();
        ByteArraysMatcher matcher = sniffer.createMatcher(is);
        matcher.mark(is);
        byte[] bom = matcher.longestMatch();
        is.reset();
        if (bom != null) {
            is.skip(bom.length);
        }
        return sniffer.getCharset(bom);
    }

    public static BOMSniffer create() {
        final Map<byte[], Charset> charsetByBOM = new HashMap<byte[], Charset>();
        charsetByBOM.put(UTF_8_BOM, UTF_8);
        charsetByBOM.put(UTF_16BE_BOM, UTF_16BE);
        charsetByBOM.put(UTF_16LE_BOM, UTF_16LE);
        charsetByBOM.put(UTF_32BE_BOM, UTF_32BE);
        charsetByBOM.put(UTF_32LE_BOM, UTF_32LE);
        return new BOMSniffer(charsetByBOM);
    }

    static {
        FF = (byte) 0xFF;
        FE = (byte) 0xFE;
        BB = (byte) 0xBB;
        BF = (byte) 0xBF;
        EF = (byte) 0xEF;
        ZERO = (byte) 0x00;

        UTF_8_BOM = new byte[]{EF, BB, BF};
        UTF_16BE_BOM = new byte[]{FE, FF};
        UTF_16LE_BOM = new byte[]{FF, FE};
        UTF_32BE_BOM = new byte[]{ZERO, ZERO, FE, FF};
        UTF_32LE_BOM = new byte[]{FF, FE, ZERO, ZERO};
    }

    private final Map<byte[], Charset> charsetByBOM;

    BOMSniffer(Map<byte[], Charset> charsetByBOM) {
        this.charsetByBOM = charsetByBOM;
    }

    private ByteArraysMatcher createMatcher(InputStream is) {
        return new ByteArraysMatcher(charsetByBOM.keySet(), is);
    }

    private Charset getCharset(byte[] bom) {
        return this.charsetByBOM.get(bom);
    }
}