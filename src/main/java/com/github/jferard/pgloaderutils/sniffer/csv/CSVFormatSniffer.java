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
package com.github.jferard.pgloaderutils.sniffer.csv;

import com.github.jferard.pgloaderutils.sniffer.Sniffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Three things to determine : finalDelimiter, finalQuotechar, finalEscapechar
 * <p>
 * Consider the following line : a\|a,"a|a","a|a","a|a"
 *
 * @author Julien Férard
 */
public class CSVFormatSniffer implements Sniffer {
    static final int ASCII_BYTE_COUNT = 128;
    private static final int BONUS_FOR_IRREGULAR_LINES = 5;
    private static final int DEFAULT_LINE_SIZE = 1024;

    public static CSVFormatSniffer createBasic(final BasicCSVConstraints csvParams) {
        return new CSVFormatSniffer(
                new BasicDelimiterComputerFactory(csvParams.getAllowedDelimiters(),
                        csvParams.getMinFields() - 1),
                new BasicQuoteComputerFactory(csvParams.getAllowedQuotes()),
                new BasicEscapeComputerFactory(csvParams.getAllowedEscapes()));
    }

    public static CSVFormatSniffer createScore(final ScoreCSVConstraints csvParams) {
        return new CSVFormatSniffer(
                new ScoreDelimiterComputerFactory(csvParams.getDelimiterScores(),
                        csvParams.getMinFields() - 1),
                new ScoreQuoteComputerFactory(csvParams.getQuoteScores()),
                new ScoreEscapeComputerFactory(csvParams.getEscapeScores()));
    }

    private final DelimiterComputerFactory delimiterComputerFactory;
    private final QuoteComputerFactory quoteComputerFactory;
    private final EscapeComputerFactory escapeComputerFactory;
    private byte finalDelimiter;
    private byte finalEscape;
    private byte finalQuote;

    public CSVFormatSniffer(final DelimiterComputerFactory delimiterComputerFactory,
                            final QuoteComputerFactory quoteComputerFactory,
                            final EscapeComputerFactory escapeComputerFactory) {
        this.delimiterComputerFactory = delimiterComputerFactory;
        this.quoteComputerFactory = quoteComputerFactory;
        this.escapeComputerFactory = escapeComputerFactory;
    }

    public byte getDelimiter() {
        return this.finalDelimiter;
    }

    public byte getEscape() {
        return this.finalEscape;
    }

    public byte getQuote() {
        return this.finalQuote;
    }

    @Override
    public void sniff(final InputStream inputStream, final int size)
            throws IOException, ParseException {

        // n fields -> n-1 delimiters
        final List<Line> lines = this.getLines(inputStream, size);
        final ByteComputer delimiterComputer = this.delimiterComputerFactory.create(lines);
        this.finalDelimiter = delimiterComputer.compute();
        final ByteComputer quoteComputer = this.quoteComputerFactory.create(lines, this.finalDelimiter);
        this.finalQuote = quoteComputer.compute();
        final ByteComputer escapeComputer =
                this.escapeComputerFactory.create(lines, this.finalDelimiter, this.finalQuote);
        this.finalEscape = escapeComputer.compute();
    }

    private List<Line> getLines(final InputStream inputStream, final int size) throws IOException {
        final StreamParser streamParser =
                new StreamParser(inputStream, CSVFormatSniffer.DEFAULT_LINE_SIZE);
        final List<Line> lines = new ArrayList<Line>();
        int i = 0;
        Line line = streamParser.getNextLine();
        while (line != null) {
            i += line.getSize();
            if (i >= size) {
                break;
            }

            lines.add(line);
            line = streamParser.getNextLine();
        }
        return lines;
    }

    public void sniff(final String path, final int size) throws IOException, ParseException {
        final InputStream stream = new FileInputStream(path);
        try {
            this.sniff(stream, size);
        } finally {
            stream.close();
        }
    }
}
