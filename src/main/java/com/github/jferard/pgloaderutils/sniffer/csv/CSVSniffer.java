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
package com.github.jferard.pgloaderutils.sniffer.csv;

import com.github.jferard.pgloaderutils.sniffer.ParallelSniffer;
import com.github.jferard.pgloaderutils.sniffer.Sniffer;
import com.github.jferard.pgloaderutils.sniffer.encoding.EncodingSniffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class CSVSniffer implements Sniffer {
    public static CSVSniffer createBasic(final BasicCSVConstraints csvConstraints) {
        final CSVFormatSniffer csvSniffer = CSVFormatSniffer.createBasic(csvConstraints);
        final EncodingSniffer encodingSniffer = new EncodingSniffer();
        final CSVOptionalHeaderSnifferFactory factory = new CSVOptionalHeaderSnifferFactory();
        return new CSVSniffer(csvSniffer, encodingSniffer, factory);

    }

    public static CSVSniffer createScore(final ScoreCSVConstraints csvConstraints) {
        final CSVFormatSniffer csvSniffer = CSVFormatSniffer.createScore(csvConstraints);
        final EncodingSniffer encodingSniffer = new EncodingSniffer();
        final CSVOptionalHeaderSnifferFactory factory = new CSVOptionalHeaderSnifferFactory();
        return new CSVSniffer(csvSniffer, encodingSniffer, factory);

    }

    private final CSVFormatSniffer csvSniffer;
    private final EncodingSniffer encodingSniffer;
    private final OptionalHeaderSnifferFactory headerSnifferFactory;
    private OptionalHeaderSniffer headerSniffer;

    CSVSniffer(final CSVFormatSniffer csvSniffer,
               final EncodingSniffer encodingSniffer,
               final OptionalHeaderSnifferFactory factory) {
        this.csvSniffer = csvSniffer;
        this.encodingSniffer = encodingSniffer;
        this.headerSnifferFactory = factory;
    }

    @Override
    public void sniff(final InputStream inputStream, final int size) throws IOException {
        final ParallelSniffer parallelSniffer = new ParallelSniffer(this.csvSniffer,
                this.encodingSniffer);
        final byte[] bytes = this.getBytes(inputStream, size);
        InputStream stream = new ByteArrayInputStream(bytes);
        parallelSniffer.sniff(stream, size);
        this.headerSniffer = this.headerSnifferFactory.create(
                this.csvSniffer.getDelimiter(), this.csvSniffer.getQuote(),
                this.csvSniffer.getEscape(), this.encodingSniffer.getCharset());

        stream = new ByteArrayInputStream(bytes);
        this.headerSniffer.sniff(stream, size);
    }

    private byte[] getBytes(final InputStream inputStream, final int size) throws IOException {
        final byte[] bytes = new byte[size];

        int c = inputStream.read();
        int i = 0;
        while (c != -1 && i++ < size) {
            bytes[i] = (byte) c;
            c = inputStream.read();
        }
        return bytes;
    }

    public byte getDelimiter() {
        return this.csvSniffer.getDelimiter();
    }

    public byte getEscape() {
        return this.csvSniffer.getEscape();
    }

    public byte getQuote() {
        return this.csvSniffer.getQuote();
    }

    public Charset getCharset() {
        return this.encodingSniffer.getCharset();
    }

    public List<String> getHeader() {
        return this.headerSniffer.getHeader();
    }
}
