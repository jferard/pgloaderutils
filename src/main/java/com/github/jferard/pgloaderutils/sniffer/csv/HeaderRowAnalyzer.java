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

import com.github.jferard.pgloaderutils.sniffer.StringUtils;
import org.apache.commons.csv.CSVFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HeaderRowAnalyzer {
    private char delimiter;
    private char escape;
    private char quote;
    private Counter<Character> delimiterCounter;
    private Counter<Character> escapeCounter;
    private Counter<Character> quoteCounter;

    public HeaderRowAnalyzer() {
        this(',', '\\', '"');
    }

    public HeaderRowAnalyzer(final char delimiter, final char escape, final char quote) {
        this.delimiter = delimiter;
        this.escape = escape;
        this.quote = quote;

        this.delimiterCounter = new Counter<Character>();
        this.escapeCounter = new Counter<Character>();
        this.quoteCounter = new Counter<Character>();
    }

    public CSVFormat analyze(final List<String> expectedHeaderStart, final String firstReadLine) throws IOException {
        if (expectedHeaderStart.size() < 2) {
            throw new IllegalArgumentException();
        }

        final List<String> expectedFields = new ArrayList<String>(expectedHeaderStart.size());

        for (final String field : expectedHeaderStart) {
            expectedFields.add(StringUtils.normalize(field));
        }

        final String line = StringUtils.normalize(firstReadLine);
        int curFieldStartIndex = 0;

        final Iterator<String> iterator = expectedFields.iterator();
        assert iterator.hasNext();

        String curExpectedField = iterator.next();
        final char firstCharOfCurExpectedField = curExpectedField.charAt(0);
        int curFieldFirstLetterIndex = line.indexOf(firstCharOfCurExpectedField, curFieldStartIndex);

        if (curFieldFirstLetterIndex == -1) {
            throw new IOException("Can't find first letter:" + curExpectedField + " (" + line + ")");
        }

        while (iterator.hasNext()) {
            // get the index of the first char after cur field, ie the first
            // char of the delimiter block
            final int curFieldDelimiterBlockIndex = this
                    .getFieldDelimiterIndex(curExpectedField, line, curFieldStartIndex, curFieldFirstLetterIndex);

            final String nextExpectedField = iterator.next();
            final char firstCharOfNextExpectedField = nextExpectedField.charAt(0);

            // get the index of the first char of the next field
            final int nextFieldFirstLetterIndex = line.indexOf(firstCharOfNextExpectedField, curFieldDelimiterBlockIndex);
            if (nextFieldFirstLetterIndex == -1) {
                throw new IOException("Can't find first letter:" + nextExpectedField + " (" + line + ")");
            }
            // get nextIndex
            curFieldStartIndex = this.advanceCurFieldStartIndex(curExpectedField, line, curFieldDelimiterBlockIndex,
                    nextFieldFirstLetterIndex);

            curExpectedField = nextExpectedField;
            curFieldFirstLetterIndex = nextFieldFirstLetterIndex;
        }

        this.delimiter = this.delimiterCounter.maxElementOr(this.delimiter);
        this.escape = this.escapeCounter.maxElementOr(this.escape);
        this.quote = this.quoteCounter.maxElementOr(this.quote);

        return CSVFormat.RFC4180.withDelimiter(this.delimiter).withEscape(this.escape).withQuote(this.quote);

    }

    private int advanceCurFieldStartIndex(final String curExpectedField, final String line, final int curFieldDelimiterBlockIndex,
                                          final int nextFieldFirstLetterIndex) throws IOException {
        final char maybeDelimiter = line.charAt(curFieldDelimiterBlockIndex);
        final int nextFieldStartIndex;
        // just a delimiter
        if (nextFieldFirstLetterIndex == curFieldDelimiterBlockIndex + 1) {
            if (Character.isLetterOrDigit(maybeDelimiter)) {
                throw new IOException("Bad delimiter after field of field:" + curExpectedField + " (" + line + ")");
            }

            this.delimiter = maybeDelimiter;
            nextFieldStartIndex = nextFieldFirstLetterIndex;
        } else {
            // trim
            int i = curFieldDelimiterBlockIndex;
            while (Character.isSpaceChar(line.charAt(i))) {
                i++;
            }

            int j = nextFieldFirstLetterIndex - 1;
            while (Character.isSpaceChar(line.charAt(j))) {
                j--;
            }
            if (j > i) { // only space, tabs, ... chars
                if (Character.isLetterOrDigit(maybeDelimiter)) {
                    throw new IOException("Bad delimiter after field of line:" + curExpectedField + " (" + line + ")");
                }
                this.delimiterCounter.put(maybeDelimiter);
                nextFieldStartIndex = nextFieldFirstLetterIndex;
            } else if (i < j) {
                this.delimiterCounter.put(line.charAt(i));
                this.quoteCounter.put(line.charAt(j));
                nextFieldStartIndex = j;
            } else {
                this.delimiterCounter.put(line.charAt(i));
                nextFieldStartIndex = nextFieldFirstLetterIndex;
            }
        }
        return nextFieldStartIndex;
    }

    private int getFieldDelimiterIndex(final String expectedField, final String line, final int firstIndex,
                                       final int firstLetterIndex) throws IOException {
        int delimiterBlockIndex;
        if (firstLetterIndex == firstIndex) {
            delimiterBlockIndex = this
                    .getDelimiterBlockIndex(expectedField, line, expectedField.length(), firstLetterIndex);
        } else if (firstLetterIndex == firstIndex + 1) {
            final char maybeQuote = line.charAt(firstIndex);
            if (Character.isLetterOrDigit(maybeQuote)) {
                throw new IOException("Missing start of field:" + expectedField + " (" + line + ")");
            }

            final int len = this.getLen(expectedField, maybeQuote);
            delimiterBlockIndex = this.getDelimiterBlockIndex(expectedField, line, len, firstLetterIndex);

            for (int i = 0; i < len; i++) {
                if (line.charAt(firstLetterIndex + i) == maybeQuote) {
                    this.escapeCounter.put(line.charAt(firstLetterIndex + i - 1));
                }
            }

            if (line.charAt(delimiterBlockIndex) != maybeQuote) {
                throw new IOException("Missing quote:" + expectedField + " (" + line + ")");
            }
            this.quoteCounter.put(maybeQuote);
            delimiterBlockIndex++;
        } else {
            throw new IOException("Missing start of field:" + expectedField + " (" + line + ")");
        }
        return delimiterBlockIndex;
    }

    /**
     * @param field
     * @param quote
     * @return the lenght of the field, with two chars per quote
     */
    private int getLen(final String field, final char quote) {
        int len = field.length();
        for (int i = 0; i < field.length(); i++) {
            if (field.charAt(i) == quote) {
                len++;
            }
        }
        return len;
    }

    private int getDelimiterBlockIndex(final String expectedField, final String line, final int len,
                                       final int firstCharIndex) throws IOException {
        double bestDistance = 1.1;
        int bestI = 0;
        for (int i = -1; i <= 1; i++) {
            final String foundField = line.substring(firstCharIndex, firstCharIndex + len + i);
            final double distance = StringUtils
                    .getJaroWinklerDistance(expectedField, foundField);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestI = i;
            }
        }
        if (bestDistance > 0.1f) {
            throw new IOException("Field error. Expected: " + expectedField + ". Found: " + line
                    .substring(firstCharIndex, firstCharIndex + len + bestI));
        }

        return firstCharIndex + len + bestI;
    }

    public CSVFormat analyze(final List<String> expectedHeader, final char[] acceptedDelimiters, final String firstLine) {
        return null;
    }
}
