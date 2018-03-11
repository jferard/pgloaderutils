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

package com.github.jferard.pgloaderutils.csvsniffer;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Computes a delimiter from lines
 */
class DelimiterComputer {
    private final int[][] delimCountByLine;
    private byte[] allowedDelimiters;
    private final int minDelimiters;
    private final List<Line> lines;
    private final double[] variances;
    private final int[] roundedMeans;
    private final List<Byte> keptDelimiters;

    DelimiterComputer(final List<Line> lines, final byte[] allowedDelimiters, int minDelimiters) {
        this.lines = lines;
        this.keptDelimiters = CSVFormatSniffer
                .asNewList(allowedDelimiters);

        this.delimCountByLine = new int[CSVFormatSniffer.ASCII_BYTE_COUNT][lines.size()];
        this.allowedDelimiters = allowedDelimiters;
        this.minDelimiters = minDelimiters;
        this.variances = new double[CSVFormatSniffer.ASCII_BYTE_COUNT];
        this.roundedMeans = new int[CSVFormatSniffer.ASCII_BYTE_COUNT];
    }

    public byte compute() throws ParseException {
        int l = 0;
        for (final Line line : lines) {
            for (final byte delim : allowedDelimiters)
                delimCountByLine[delim][l] = line.getCount(delim);
            l++;
        }

        return this.computeDelimiter();
    }

    private byte computeDelimiter() throws ParseException {
        this.computeStatsAndRemoveBadDelimiters();

        switch (keptDelimiters.size()) {
            case 0 : throw new ParseException("", 0);
            case 1 : return keptDelimiters.get(0);
            default: break;
        }
        this.keepBestDelimiters();

        switch (keptDelimiters.size()) {
            case 0 : throw new AssertionError("");
            case 1 : return keptDelimiters.get(0);
            default: return keptDelimiters.get(this.keptDelimiters.size()-2);
        }
    }

    private void keepBestDelimiters() {
        Comparator<Byte> comparator = new StatsComparator();
        Collections.sort(keptDelimiters, comparator);

        final Iterator<Byte> it = keptDelimiters.iterator();
        Byte first = it.next();
        while (it.hasNext()) {
            Byte next = it.next();
            if (Math.abs(variances[first] - variances[next]) < 1e-3)
                continue;

            it.remove();
        }
    }

    private void computeStatsAndRemoveBadDelimiters() {
        final Iterator<Byte> it = keptDelimiters.iterator();
        while (it.hasNext()) {
            final byte delim = it.next();
            final StatisticsBasic statisticsBasic = new StatisticsBasic(
                    delimCountByLine[delim]);
            roundedMeans[delim] = (int) Math.round(statisticsBasic.getMean());
            variances[delim] = statisticsBasic.getVariance();
            if (roundedMeans[delim] < minDelimiters || variances[delim] > 4)
                it.remove();
        }
    }

    private class StatsComparator implements Comparator<Byte> {
        @Override
        public int compare(final Byte d1, final Byte d2) {
            if (Math.abs(variances[d1] - variances[d2]) < 1e-3) {
                return (int) Math
                        .signum(roundedMeans[d1] - roundedMeans[d2]);
            } else
                return (int) Math.signum(variances[d1] - variances[d2]);
        }
    }
}