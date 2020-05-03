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

import com.github.jferard.pgloaderutils.sniffer.csv.Line;

import java.io.IOException;
import java.io.InputStream;

/**
 * The StreamParser class parses a stream in lines without the encoding information.
 */
class StreamParser {
    private static final int CR = 0x0D;
    private static final int LF = 0x0A;
    private final InputStream is;

    private int size;
    private int lastEOLChar;
    private Line curLine;

    StreamParser(final InputStream is, final int defaultSize) {
        this.is = is;
        this.size = defaultSize;
        this.lastEOLChar = 0;
        this.curLine = new Line(this.size);
    }

    private boolean isNotSecondPartOfEOL(int c) {
        return !(c == LF && this.lastEOLChar == CR);
    }

    public Line getNextLine() throws IOException {
        Line curLine = new Line(this.size);
        int c = this.is.read();

        if (c == -1) {
            return null;
        }

        while (c != -1) {
            if (this.lastEOLChar == 0) {
                if (c == CR || c == LF) { // a new line
                    this.lastEOLChar = c;
                    return curLine;
                } else {
                    curLine.append((byte) c);
                }
            } else { // lastEOLChar = CR or LF
                if (this.isNotSecondPartOfEOL(c)) {
                    if (c == CR || c == LF) { // a new line
                        this.lastEOLChar = c;
                        return curLine;
                    } else {
                        curLine.append((byte) c);
                    }
                } // else forget it
                this.lastEOLChar = 0;
            }
            c = this.is.read();
        }
        return curLine;
    }
}
