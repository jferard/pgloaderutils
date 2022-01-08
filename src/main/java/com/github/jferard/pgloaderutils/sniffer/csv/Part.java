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

import java.nio.charset.Charset;
import java.util.Arrays;

class Part {
    public static final int NONE = -1;
    public static final int MULTIPLE = -2;
    private final byte[] array;
    private int from;
    private int to;

    /**
     * @param array the underlying array
     * @param from the first index
     * @param to the last index+1
     */
    public Part(final byte[] array, final int from, final int to) {
        if (array.length == 0 || from >= to || to > array.length || to <= 0) {
            throw new IllegalArgumentException(
                    String.format("%s [%d-%d]", new String(array, Charset.forName("ASCII")), from, to));
        }

        this.array = array;
        this.from = from;
        this.to = to;
    }

    public void trimSpaces() {
        while (this.from < this.array.length && this.array[this.from] == ' ') {
            this.from++;
        }

//		if (this.to == 0)
//			return;

        while (this.to > 0 && this.array[this.to - 1] == ' ') {
            this.to--;
        }
    }

    public boolean trimIfPossibleQuote(final byte c) {
        final QuoteType result = this.quoteType(c);
        if (result == QuoteType.BOTH) {
            this.from++;
            this.to--;
        }
        return result == QuoteType.BOTH;
    }

    /**
     * @param quote a quote char
     * @return 0 if part matches [^q].*[^q], 1 if part matches [^q].*[q] or [q].*[^q], 5 if part
     * matches [q].*[q].
     */
    public QuoteType quoteType(final byte quote) {
        if (this.from == this.to - 1) // one char
        {
            return QuoteType.NONE;
        }
        if (this.array[this.from] == quote) {
            if (this.array[this.to - 1] == quote) {
                return QuoteType.BOTH;
            } else {
                return QuoteType.LEFT;
            }
        } else {
            if (this.array[this.to - 1] == quote) {
                return QuoteType.RIGHT;
            } else {
                return QuoteType.NONE;
            }
        }
    }

    /**
     * Check if a char cannot be a quote char.
     *
     * @param maybeQuote the quote char that is tested
     * @return true if the maybeQuote char is at one extremity of the part, but not at the other
     */
    public boolean cannotHaveQuote(final byte maybeQuote) {
        return this.array[this.from] == maybeQuote && this.array[this.to - 1] != maybeQuote ||
                this.array[this.from]
                        != maybeQuote && this.array[this.to - 1] == maybeQuote;
    }

    /**
     * Check if a char can be a quote char.
     *
     * @param maybeQuote the quote char that is tested
     * @return true if the maybeQuote char is at one extremity of the part and at the other, or
     * not a any extremity
     */
    public boolean canHaveQuote(final byte maybeQuote) {
        if (this.from >= this.to - 1) {
            return false; // penalize empty parts
        }
        return this.array[this.from] == maybeQuote && this.array[this.to - 1] == maybeQuote ||
                this.array[this.from] != maybeQuote && this.array[this.to - 1] != maybeQuote;
    }

    public byte getFirstChar() {
        return this.array[this.from];
    }

    public byte getLastChar() {
        return this.array[this.to - 1];
    }

    public boolean innerContains(final int c) {
        for (int i=this.from + 1; i<this.to - 1; i++) {
            if (this.array[i] == c) {
                return true;
            }
        }
        return false;
    }

    public int findCharBefore(final int quote) {
        int c = NONE;
        int j = this.to - 1;
        while (j >= this.from + 1) {
            if (this.array[j] == quote) {
                j--;
                if (c == NONE) {
                    c = this.array[j];
                } else if (c != this.array[j]) {
                    return MULTIPLE;
                }
            }
            j--;
        }
        return c;
    }

    @Override
    public String toString() {
        if (this.from > this.to) {
            return "";
        }
        return new String(this.array, this.from, this.to - this.from, Charset.forName("ASCII"));
    }

    public enum QuoteType {
        NONE, LEFT, RIGHT, BOTH;
    }
}
