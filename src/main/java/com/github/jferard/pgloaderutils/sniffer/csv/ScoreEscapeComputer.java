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

public class ScoreEscapeComputer implements ByteComputer {
    private final byte finalDelimiter;
    private final byte finalQuote;
    private final int[] escapeScore;
    private final List<Line> lines;

    public ScoreEscapeComputer(final List<Line> lines, final byte finalDelimiter, final byte finalQuote,
                               final int[] escapeScore) {
        this.finalDelimiter = finalDelimiter;
        this.finalQuote = finalQuote;
        this.escapeScore = escapeScore;
        this.lines = lines;
    }

    @Override
    public byte compute() {
        final List<Part> parts = this.getParts();
        this.updateEscapeScore(parts);
        return ScoreUtil.getBest(this.escapeScore);
    }

    private List<Part> getParts() {
        final List<Part> parts = new ArrayList<Part>();
        for (final Line line : this.lines) {
            parts.addAll(this.split(line));
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

    private void updateEscapeScore(final List<Part> parts) {
        for (final Part part : parts) {
            final int c = part.findCharBefore(this.finalQuote);
            if (c >= 0) {
                this.escapeScore[c]++;
            }
        }
    }
}
