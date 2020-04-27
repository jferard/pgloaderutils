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

import java.nio.charset.Charset;

class Part {
    public static final int MULTIPLE = -2;
    public static final int NONE = -1;
    private byte[] array;
    private int from;
    private int to;

    public Part(byte[] array, int from, int to) {
        if (array.length == 0 || from >= array.length || to <= 0 || from >= to) throw new IllegalArgumentException();

        this.array = array;
        this.from = from;
        this.to = to;
    }

    public void trim() {
        while (this.array[this.from] == ' ') this.from++;

//		if (this.to == 0)
//			return;

        while (this.array[this.to - 1] == ' ') this.to--;
    }

    public boolean trimOne(byte c) {
        int result = this.quoteValue(c);
        if (result > 1) {
            this.from++;
            this.to--;
        }
        return result > 1;
    }

    public int quoteValue(byte quote) {
        if (this.from == this.to - 1) // one char
            return 0;
        if (this.array[this.from] == quote) {
            if (this.array[this.to - 1] == quote) return 5;
            else return 1;
        } else {
            if (this.array[this.to - 1] == quote) return 1;
            else return 0;
        }
    }

    /**
     * Check if a char cannot be a quote char.
     *
     * @param maybeQuote the quote char that is tested
     * @return true if the maybeQuote char is at one extremity of the part, but not at the other
     */
    public boolean cannotHaveQuote(byte maybeQuote) {
        return this.array[this.from] == maybeQuote && this.array[this.to - 1] != maybeQuote || this.array[this.from]
                != maybeQuote && this.array[this.to - 1] == maybeQuote;
    }

    /**
     * Check if a char can be a quote char.
     *
     * @param maybeQuote the quote char that is tested
     * @return true if the maybeQuote char is at one extremity of the part and at the other, or
     * not a any extremity
     */
    public boolean canHaveQuote(byte maybeQuote) {
        return this.array[this.from] == maybeQuote && this.array[this.to - 1] == maybeQuote || this.array[this.from]
                != maybeQuote && this.array[this.to - 1] != maybeQuote;
    }

    public byte getFirstChar() {
        return this.array[this.from];
    }

    public byte getLastChar() {
        return this.array[this.to - 1];
    }

    public int findCharBefore(int quote) {
        int c = NONE;
        int j = this.to - 1;
        while (j >= this.from + 1) {
            if (this.array[j] == quote) {
                j--;
                if (c == NONE) c = this.array[j];
                else if (c != this.array[j]) return MULTIPLE;
            }
            j--;
        }
        return c;
    }

    @Override
    public String toString() {
        return new String(this.array, this.from, this.to - this.from, Charset.forName("ASCII"));
    }
}
