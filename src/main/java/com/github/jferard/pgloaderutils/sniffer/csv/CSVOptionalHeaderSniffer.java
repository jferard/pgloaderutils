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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The CSVOptionalHeaderSniffer class is a Sniffer that checks if the provided
 * stream contains a header. Here are the steps :
 *
 * <ol>
 * <li>if the first row contains at least on digits only value, then there is no
 * header in the stream</li>
 * <li>if the first row contains at least on digits only value, then there is no
 * header in the stream</li>
 * </ol>
 *
 * @author Julien Férard
 */
public class CSVOptionalHeaderSniffer implements OptionalHeaderSniffer {
    private final Charset charset;

    private final CSVFormat csvFormat;
    private final RowSignaturesAnalyzer rowSignaturesAnalyzer;
    private List<String> header;

    public CSVOptionalHeaderSniffer(final CSVFormat csvFormat,
                                    final Charset charset) {
        this.charset = charset;
        this.csvFormat = csvFormat;
        this.rowSignaturesAnalyzer = new RowSignaturesAnalyzer();
    }

    @Override
    public List<String> getHeader() {
        return this.header;
    }

    @Override
    public void sniff(final InputStream inputStream, final int size)
            throws IOException {
        final Reader streamReader = new InputStreamReader(inputStream,
                this.charset);

        final CSVParser parser = new CSVParser(streamReader, this.csvFormat);
        try {
            this.sniff(parser);
        } finally {
            parser.close();
        }
    }

    private void sniff(final CSVParser parser) {
        final Iterator<CSVRecord> iterator = parser.iterator();

        if (!iterator.hasNext()) {
            this.header = null;
            return;
        }

        final CSVRecord firstRowRecord = iterator.next();
        final int firstRowSize = firstRowRecord.size();

        final char[] firstRowSignature = this.rowSignaturesAnalyzer
                .getSignature(firstRowRecord, firstRowSize);

        if (this.containsAtLeastOneOnlyDigitsValue(firstRowSignature)) {
            this.header = null;
        } else {
            final char[] remainingRowsSignature = this.rowSignaturesAnalyzer
                    .getRemainingRowsSignature(iterator, firstRowSize);
            if (this.containsAtLeastOneColumnWithLetterHeaderAndDigitValues(
                    firstRowSignature, remainingRowsSignature,
                    firstRowSize)) {
                // copy firstRow in header
                for (final String s : firstRowRecord) {
                    this.header.add(s);
                }
            }
        }
    }

    private boolean containsAtLeastOneColumnWithLetterHeaderAndDigitValues(
            final char[] firstRowSignature, final char[] remainingRowsSignature,
            final int firstRowSize) {
        for (int col = 0; col < firstRowSize; col++) {
            // at least one column with non digit first cell and digit next
            // cells
            if (firstRowSignature[col] == '?'
                    && remainingRowsSignature[col] != '?') {
                this.header = new ArrayList<String>(firstRowSize);
                return true;
            }
        }
        return false;
    }

    private boolean containsAtLeastOneOnlyDigitsValue(
            final char[] firstRowSignature) {
        for (final char c : firstRowSignature) {
            if (c == 'D') {
                return true;
            }
        }
        return false;
    }
}
