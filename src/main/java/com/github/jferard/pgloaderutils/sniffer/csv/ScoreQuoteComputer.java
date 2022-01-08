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

import java.util.ArrayList;
import java.util.List;

public class ScoreQuoteComputer implements ByteComputer {
    private final List<Line> lines;
    private final byte delimiter;
    private final int[] quoteScore;

    public ScoreQuoteComputer(final List<Line> lines, final byte delimiter, final int[] quoteScore) {
        this.lines = lines;
        this.delimiter = delimiter;
        this.quoteScore = quoteScore;
    }

    @Override
    public byte compute() {
        final List<Part> parts = getParts();
        updateQuoteScore(parts);
        final byte max = ScoreUtil.getBest(this.quoteScore);
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

    private void updateQuoteScore(final List<Part> parts) {
        for (byte q=0; q>=0; q++) {
            this.quoteScore[q] += getQuoteScoreForByte(parts, q);
        }
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

}
