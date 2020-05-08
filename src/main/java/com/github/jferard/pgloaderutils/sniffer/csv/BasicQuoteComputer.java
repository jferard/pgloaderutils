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

public class BasicQuoteComputer implements ByteComputer {
    private final byte delimiter;
    private List<Line> lines;
    private final List<Byte> keptQuotes;

    public BasicQuoteComputer(final List<Line> lines, final byte delimiter, final byte[] allowedQuotes) {
        this.lines = lines;
        this.delimiter = delimiter;
        keptQuotes = ScoreUtil.asNewList(allowedQuotes);
    }

    @Override
    public byte compute() {
        final List<Part> parts = getParts();
        final int[] quoteScore = getQuoteScore(parts);
        final byte max = getBestQuote(quoteScore);
        // is the best quote enough?
        if (5 * quoteScore[max] >= parts.size()) {
            return max;
        } else {
            return '\0';
        }
    }

    private List<Part> getParts() {
        final List<Part> parts = new ArrayList<Part>();
        for (final Line line : lines) {
            parts.addAll(split(line));
        }
        return parts;
    }

    private List<Part> split(final Line line) {
        final List<Part> parts = line.asParts(this.delimiter);
        for (final Part part : parts) {
            part.trimSpaces();
        }
        return parts;
    }

    private int[] getQuoteScore(final List<Part> parts) {
        final int[] quoteScore = new int[CSVFormatSniffer.ASCII_BYTE_COUNT];
        for (final byte q : keptQuotes) {
            quoteScore[q] = getQuoteScoreForByte(parts, q);
        }
        return quoteScore;
    }

    private int getQuoteScoreForByte(final List<Part> parts, final byte q) {
        int quoteScore = 0;
        for (final Part part : parts) {
            final Part.QuoteType quoteType = part.quoteType(q);
            quoteScore += getBonus(quoteType);
        }
        return quoteScore;
    }

    private int getBonus(final Part.QuoteType quoteType) {
        final int bonus;
        switch (quoteType) {
            case LEFT:
            case RIGHT:
                bonus = 1;
                break;
            case BOTH:
                bonus = 5;
                break;
            default:
                bonus = 0;
                break;
        }
        return bonus;
    }

    private byte getBestQuote(final int[] quoteScore) {
        return Collections.max(keptQuotes, new Comparator<Byte>() {
            @Override
            public int compare(final Byte q1, final Byte q2) {
                return quoteScore[q1] - quoteScore[q2];
            }
        });
    }
}
