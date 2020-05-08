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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BasicEscapeComputer implements ByteComputer {
    private final byte finalDelimiter;
    private final byte finalQuote;
    private final byte[] allowedEscapes;
    private final List<Line> lines;
    private List<Byte> keptEscapes;

    public BasicEscapeComputer(final List<Line> lines, final byte finalDelimiter, final byte finalQuote,
                               final byte[] allowedEscapes) {
        this.finalDelimiter = finalDelimiter;
        this.finalQuote = finalQuote;
        this.allowedEscapes = allowedEscapes;
        this.lines = lines;
        keptEscapes = ScoreUtil.asNewList(allowedEscapes);
        keptEscapes.add(this.finalQuote);
    }

    @Override
    public byte compute() {
        final List<Part> parts = getParts();
        final int[] escapeScore = getEscapeScore(parts);
        return getBestEscape(escapeScore);
    }

    private List<Part> getParts() {
        final List<Part> parts = new ArrayList<Part>();
        for (final Line line : lines) {
            parts.addAll(split(line));
        }
        return parts;
    }

    private List<Part> split(final Line line) {
        final List<Part> parts = line.asParts(this.finalDelimiter);
        for (final Part part : parts) {
            part.trimSpaces();
            part.trimIfPossibleQuote(this.finalQuote);
        }
        return parts;
    }

    private int[] getEscapeScore(final List<Part> parts) {
        final int[] escapeScore = new int[CSVFormatSniffer.ASCII_BYTE_COUNT];
        for (final Part part : parts) {
            final int c = part.findCharBefore(this.finalQuote);
            if (c >= 0 && keptEscapes.contains(Byte.valueOf((byte) c))) {
                escapeScore[c]++;
            }
        }
        return escapeScore;
    }


    private byte getBestEscape(final int[] escapeScore) {
        return Collections.max(keptEscapes, new Comparator<Byte>() {
            @Override
            public int compare(final Byte e1, final Byte e2) {
                return escapeScore[e1] - escapeScore[e2];
            }
        });
    }
}
