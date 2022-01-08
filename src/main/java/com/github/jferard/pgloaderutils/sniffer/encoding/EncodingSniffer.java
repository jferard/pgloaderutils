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
package com.github.jferard.pgloaderutils.sniffer.encoding;

import com.github.jferard.pgloaderutils.Util;
import com.github.jferard.pgloaderutils.sniffer.Sniffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * An EncodingSniffer is a very basic csv, that checks if a stream is UTF-8, US ASCII or another stream.
 */
public class EncodingSniffer implements Sniffer {
    private Charset charset;

    /**
     * @param file the path of the file
     * @param size the size in expected codepoints to check
     * @return UTF-8, US-ASCII or null Charset. If null, the charset can be any
     * of the existing "1 byte per char" charsets.
     * @throws IOException
     */
    public void sniff(final File file, final int size) throws IOException {
        final InputStream stream = new FileInputStream(file);
        try {
            this.sniff(stream, size);
        } finally {
            stream.close();
        }
    }

    /**
     * @param stream the input stream
     * @param size   the size in expected codepoints to check
     * @return UTF-8, US-ASCII or null Charset. If null, the charset can be any
     * of the existing "1 byte per char" charsets.
     * @throws IOException
     */
    @Override
    public void sniff(final InputStream stream, final int size) throws IOException {
        this.charset = Util.US_ASCII;

        final UTF8Decoder decoder = new UTF8Decoder(stream);
        if (decoder.gobbleBOM()) {
            this.charset = Util.UTF_8;
        }

        try {
            for (int i = 0; i < size; i++) {
                final int c = decoder.readUnicodeValue();
                if (c == -1) {
                    return;
                } else if (c >= SnifferConstants.B10000000) {
                    this.charset = Util.UTF_8;
                }
            }
        } catch (final CharacterCodingException e) {
            this.charset = null;
        }
    }

    public Charset getCharset() {
        return this.charset;
    }
}