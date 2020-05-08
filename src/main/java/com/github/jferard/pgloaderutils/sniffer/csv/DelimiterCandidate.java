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
import java.util.List;

/**
 * Represents a delimiter candidate: a byte with a score.
 */
public class DelimiterCandidate implements Comparable<DelimiterCandidate> {
    final byte b;
    final int score;
    public DelimiterCandidate(final byte b, final int score) {
        this.b = b;
        this.score = score;
    }

    /**
     * @param scores an array with byte scores
     * @return the sorted candidates, from highest score to lowest score
     */
    public static List<DelimiterCandidate> sorted(final int[] scores) {
        final List<DelimiterCandidate> candidates = new ArrayList<DelimiterCandidate>(scores.length);
        for (byte b = 0; b >= 0; b++) { // 0-127
            final DelimiterCandidate bws = new DelimiterCandidate(b, scores[b]);
            candidates.add(bws);
        }
        Collections.sort(candidates);
        return candidates;
    }

    public byte getByte() {
        return this.b;
    }

    public int getScore() {
        return this.score;
    }


    @Override
    public int compareTo(final DelimiterCandidate o) {
        return o.score - this.score;
    }

    @Override
    public String toString() {
        return "DelimiterCandidate[char='" + (char) this.b + "' ("+this.b + "), score=" + this.score + "]";
    }
}
