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

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Computes a delimiter from lines. The algorithm is:
 *
 * 1. for each byte in `allowedDelimiters`, count the number of items in each line.
 * 2. remove the bad candidates (too few delimiters, too large variance)
 * 3. take the min variance and the max mean of number of delims
 * 4. remove the possible quotes.
 * 5. return the first element.
 */
class BasicDelimiterComputer implements ByteComputer {
    private final int[][] delimCountByLine;
    private final int minDelimiters;
    private final List<Line> lines;
    private final double[] variances;
    private final int[] roundedMeans;
    private final List<Byte> keptDelimiters;
    private byte[] allowedDelimiters;

    BasicDelimiterComputer(final List<Line> lines, final byte[] allowedDelimiters, int minDelimiters) {
        this.lines = lines;
        this.allowedDelimiters = allowedDelimiters;
        this.minDelimiters = minDelimiters;

        this.keptDelimiters = CSVFormatSniffer.asNewList(allowedDelimiters);
        this.delimCountByLine = new int[CSVFormatSniffer.ASCII_BYTE_COUNT][lines.size()];
        this.variances = new double[CSVFormatSniffer.ASCII_BYTE_COUNT];
        this.roundedMeans = new int[CSVFormatSniffer.ASCII_BYTE_COUNT];
    }

    @Override
    public byte compute() throws ParseException {
        int l = 0;
        for (final Line line : lines) {
            for (final byte delim : allowedDelimiters) {
                delimCountByLine[delim][l] = line.getCount(delim);
            }
            l++;
        }

        this.sortCandidates();
        if (this.keptDelimiters.size() > 1) {
            this.tryToRemoveQuote();
        }
        return this.keptDelimiters.get(0);
    }

    private void sortCandidates() throws ParseException {
        this.computeStatsAndRemoveBadDelimiters();
        if (keptDelimiters.isEmpty()) {
            throw new ParseException("", 0);
        }
        Collections.sort(keptDelimiters, new StatsComparator());
    }

    // compute roundedMean and variance for each delim
    // remove delim when roundedMin < minDelimiters or variance > 4
    private void computeStatsAndRemoveBadDelimiters() {
        final Iterator<Byte> it = keptDelimiters.iterator();
        while (it.hasNext()) {
            final byte delim = it.next();
            final StatisticsBasic statisticsBasic = new StatisticsBasic(delimCountByLine[delim]);
            roundedMeans[delim] = (int) Math.round(statisticsBasic.getMean());
            variances[delim] = statisticsBasic.getVariance();
            if (roundedMeans[delim] < minDelimiters || variances[delim] > 4) {
                it.remove();
            }
        }
    }
    private void tryToRemoveQuote() {
        final byte maybeQuote = this.keptDelimiters.get(0);
        final byte maybeDelimiter = this.keptDelimiters.get(0);

        for (final Line line : lines) {
            final List<Part> parts = line.asParts(maybeDelimiter);
            for (final Part part : parts) {
                part.trimSpaces();
                if (part.cannotHaveQuote(maybeQuote)) {
                    return; // maybeQuote is not a quote!
                }
            }
        }
        this.keptDelimiters.remove(0);
    }

    // sort by ascending variance and then by descending mean
    private class StatsComparator implements Comparator<Byte> {
        @Override
        public int compare(final Byte d1, final Byte d2) {
            if (Math.abs(variances[d1] - variances[d2]) < 1e-3) {
                return (int) Math.signum(roundedMeans[d2] - roundedMeans[d1]);
            } else {
                return (int) Math.signum(variances[d1] - variances[d2]);
            }
        }
    }
}