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

import java.util.List;

/**
 * Computes a delimiter from lines. The algorithm is similar to BasicDelimiterComputer, but uses
 * scores.
 */
class ScoreDelimiterComputer implements ByteComputer {
    private final int[][] delimCountByLine;
    private final int minDelimiters;
    private final List<Line> lines;
    private final double[] variances;
    private final int[] roundedMeans;
    private final int[] delimiterScores;
    private final int[][] delimiterQuote;

    ScoreDelimiterComputer(final List<Line> lines, final int[] delimiterScores,
                           final int minDelimiters) {
        this.lines = lines;
        this.delimiterScores = delimiterScores;
        this.minDelimiters = minDelimiters;

        this.delimCountByLine = new int[CSVFormatSniffer.ASCII_BYTE_COUNT][lines.size()];
        this.variances = new double[CSVFormatSniffer.ASCII_BYTE_COUNT];
        this.roundedMeans = new int[CSVFormatSniffer.ASCII_BYTE_COUNT];
        this.delimiterQuote =
                new int[CSVFormatSniffer.ASCII_BYTE_COUNT][CSVFormatSniffer.ASCII_BYTE_COUNT];
    }

    public byte compute() {
        final List<DelimiterCandidate> delimiterCandidates = this.computeAll();
        while (this.delimiterQuote[delimiterCandidates.get(0).getByte()][delimiterCandidates.get(1)
                .getByte()] < 0) {
            delimiterCandidates.remove(0);
        }
        return delimiterCandidates.get(0).getByte();
    }

    private List<DelimiterCandidate> computeAll() {
        int l = 0;
        for (final Line line : this.lines) {
            for (byte delim = 0; delim >= 0; delim++) {
                this.delimCountByLine[delim][l] = line.getCount(delim); //  / line.getSize();
            }
            l++;
        }

        this.computeStatsAndScoreDelimiters();
        this.penalizeQuotes();
        final List<DelimiterCandidate> sorted = DelimiterCandidate.sorted(this.delimiterScores);
        return sorted;
    }

    // compute roundedMean and variance for each delim
    private void computeStatsAndScoreDelimiters() {
        for (byte delim = 0; delim >= 0; delim++) { // 0-127
            final StatisticsBasic statisticsBasic =
                    new StatisticsBasic(this.delimCountByLine[delim]);
            final double mean = statisticsBasic.getMean();
            if (mean < 1) {
                this.delimiterScores[delim] -= 500;
            } else {
                final double variance = statisticsBasic.getVariance();
                this.roundedMeans[delim] = (int) Math.round(mean);
                this.variances[delim] = variance;

                if (variance < 0.05) {
                    this.delimiterScores[delim] += 25 * (1 - variance);
                } else if (variance < 0.1) {
                    this.delimiterScores[delim] += 16 * (1 - variance);
                } else if (variance < 0.15) {
                    this.delimiterScores[delim] += 5 * (1 - variance);
                } else if (variance >= 0.25) {
                    this.delimiterScores[delim] -= 15 * variance;
                }
                this.delimiterScores[delim] += mean * 10;
                if (this.roundedMeans[delim] < this.minDelimiters) {
                    this.delimiterScores[delim] -= 10;
                }
            }
        }
    }

    private void penalizeQuotes() {
        // O(n^2)
        for (byte maybeDelimiter = 0; maybeDelimiter >= 0; maybeDelimiter++) {
            for (final Line line : this.lines) {
                final List<Part> parts = line.asParts(maybeDelimiter);
                for (final Part part : parts) {
                    part.trimSpaces();
                    for (byte maybeQuote = 0; maybeQuote >= 0; maybeQuote++) {
                        if (part.canHaveQuote(maybeQuote)) {
                            final int delta = part.innerContains(maybeQuote) ? 1 : 2;
                            this.delimiterQuote[maybeDelimiter][maybeQuote] +=
                                    delta; // maybeQuote is a quote!
                            this.delimiterQuote[maybeQuote][maybeDelimiter] -=
                                    delta; // maybeQuote is a quote!
                        }
                    }
                }
            }
        }
    }
}