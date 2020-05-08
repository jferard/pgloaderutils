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

/**
 * The main constraint is that delimiter, quote and escape can't be a letter or
 * a digit. More precisely, char allowed are [0, 2F] u [3A, 40] u (5B, 60] u
 * [7B, 7F]. Only ascii char are allowed.
 */
public class ScoreCSVConstraints {
    private static final int ASCII_CHAR_COUNT = 128;

    public static ScoreCSVConstraintsBuilder builder() {
        return new ScoreCSVConstraintsBuilder();
    }

    public static ScoreCSVConstraintsBuilder basicBuilder() {
        return new ScoreCSVConstraintsBuilder().allowedDelimiters(new byte[]{',', ';', '\t', '|'})
                .allowedQuotes(new byte[]{'"', '\''}).allowedEscapes(new byte[]{'\\'}).minFields(2);
    }

    private final int minFields;
    private final int[] delimiterScores;
    private final int[] quoteScores;
    private final int[] escapeScores;

    public ScoreCSVConstraints(final int[] delimiterScores, final int[] quoteScores, final int[] escapeScores, final int minFields) {
        this.delimiterScores = delimiterScores;
        this.quoteScores = quoteScores;
        this.escapeScores = escapeScores;
        this.minFields = minFields;
    }

    public int[] getDelimiterScores() {
        return getScoresCopy(this.delimiterScores);
    }

    public int[] getQuoteScores() {
        return getScoresCopy(this.quoteScores);
    }

    public int[] getEscapeScores() {
        return getScoresCopy(this.escapeScores);
    }

    static int[] getScoresCopy(final int[] scores) {
        final int[] ret = new int[ASCII_CHAR_COUNT];
        System.arraycopy(scores, 0, ret, 0, ASCII_CHAR_COUNT);
        return ret;
    }

    public int quoteScore(final int c) {
        return this.quoteScores[c];
    }
    public int escapeScore(final int c) {
        return this.escapeScores[c];
    }
    public int getMinFields() {
        return this.minFields;
    }
}
