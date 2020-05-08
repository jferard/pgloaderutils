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
 * The main constraints are : - delimiter, quote and escape can't be a letter or
 * a digit. - quote will be added to escape chars.
 */
public class ScoreCSVConstraintsBuilder {
    private static final int ASCII_CHAR_COUNT = 128;
    private static final int NEVER = -100;
    private static final int POSSIBLE = 0;
    private static int[] all;

    private static int[] getAllScores() {
        if (ScoreCSVConstraintsBuilder.all == null) {
            all = new int[ASCII_CHAR_COUNT];
            for (int b = 0; b < ASCII_CHAR_COUNT; b++) {
                if (isContentOrNewLine(b)) {
                    all[b] = NEVER;
                } else {
                    all[b] = POSSIBLE;
                }
            }
        }
        return ScoreCSVConstraints.getScoresCopy(all);
    }

    private static boolean isContentOrNewLine(final int b) {
        return (b == '\r' || b == '\n') || (b >= '0' && b <= '9') || (b >= 'A' && b <= 'Z') ||
                (b >= 'a' && b <= 'z');
    }

    private int[] delimiterScores;
    private int[] quoteScores;
    private int minFields;

    private int[] escapeScores;

    ScoreCSVConstraintsBuilder() {
        this.delimiterScores = getAllScores();
        this.quoteScores = getAllScores();
        this.escapeScores = getAllScores();
        this.minFields = 2;
    }

    public ScoreCSVConstraintsBuilder allowedDelimiters(final byte[] allowedDelimiters) {
        this.allow(this.delimiterScores, allowedDelimiters);
        return this;
    }

    private void allow(final int[] scores, final byte[] alloweds) {
        for (int b = 0; b < ASCII_CHAR_COUNT; b++) {
            if (!this.contains(alloweds, b)) {
                scores[b] = NEVER;
            }
        }
    }

    private boolean contains(final byte[] bytes, final int b) {
        for (final byte b2 : bytes) {
            if (b == b2) {
                return true;
            }
        }
        return false;
    }

    public ScoreCSVConstraintsBuilder allowedQuotes(final byte[] allowedQuotes) {
        this.allow(this.quoteScores, allowedQuotes);
        return this;
    }

    public ScoreCSVConstraintsBuilder allowedEscapes(final byte[] allowedEscapes) {
        this.allow(this.escapeScores, allowedEscapes);
        return this;
    }

    public ScoreCSVConstraintsBuilder minFields(final int minFields) {
        this.minFields = minFields;
        return this;
    }

    public ScoreCSVConstraintsBuilder delimiterScores(final int[] delimiterScores) {
        this.delimiterScores = delimiterScores;
        return this;
    }

    public ScoreCSVConstraintsBuilder quoteScores(final int[] quoteScores) {
        this.quoteScores = quoteScores;
        return this;
    }

    public ScoreCSVConstraintsBuilder escapeScores(final int[] escapeScores) {
        this.escapeScores = escapeScores;
        return this;
    }

    public ScoreCSVConstraints build() {
        return new ScoreCSVConstraints(this.delimiterScores, this.quoteScores, this.escapeScores,
                this.minFields);
    }
}
