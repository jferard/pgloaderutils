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

package com.github.jferard.pgloaderutils.sniffer;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    final static Pattern p = Pattern
            .compile("\\p{InCombiningDiacriticalMarks}+");

    public static String normalize(final String s) {
        final String decomposed = java.text.Normalizer.normalize(s.trim(),
                Normalizer.Form.NFD);
        return p.matcher(decomposed) // $NON-NLS-1$
                .replaceAll("").trim();
    }

    /**
     * see https://github.com/richmilne/JaroWinkler/blob/master/jaro/strcmp95.c
     */
    public static double strcmp95(final String first, final String second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException();
        }
        return StringUtils.strcmp95Normalized(StringUtils.normalize(first).toLowerCase(),
                StringUtils.normalize(second).toLowerCase());
    }

    // TODO
    public static double strcmp95Normalized(final String first, final String second) {
        return 0;
    }

    public static double getJaroDistance(final String s1, final String s2) {
        return StringUtils.getJaroWinklerDistance(s1, s2, 1.1);
    }

    public static double getJaroWinklerDistance(final String s1, final String s2) {
        return StringUtils.getJaroWinklerDistance(s1, s2, 0.7);
    }

    private static double getJaroWinklerDistance(final String s1, final String s2,
                                                 final double threshold) {
        final int s1len = s1.length();
        final int s2len = s2.length();
        if (s1len == s2len && s1.equals(s2)) {
            return 0.0;
        }

        final String shortest;
        final int shortestLen;
        final String longest;
        final int longestLen;
        if (s1len < s2len) {
            shortest = s1;
            shortestLen = s1len;
            longest = s2;
            longestLen = s2len;
        } else {
            shortest = s2;
            shortestLen = s2len;
            longest = s1;
            longestLen = s1len;
        }
        int r;
        if (longestLen % 2 == 0) {
            r = longestLen / 2 - 1;
        } else {
            r = (longestLen - 1) / 2 - 1;
        }
        int matchingCharactersCount = 0;
        final boolean[] shortestTaken = new boolean[shortestLen];
        final boolean[] longestTaken = new boolean[longestLen];
        for (int i = 0; i < shortestLen; i++) { //
            final char sc = shortest.charAt(i);
            int from = i - r;
            if (i - r < 0) {
                from = 0;
            }
            int to = i + r;
            if (to >= longestLen) {
                to = longestLen - 1;
            }
            for (int j = from; j <= to; j++) {
                if (longestTaken[j]) {
                    continue;
                }
                final char lc = longest.charAt(j);
                if (sc == lc) {
                    shortestTaken[i] = true;
                    longestTaken[j] = true;
                    matchingCharactersCount++;
                    break;
                }
            }
        }
        if (matchingCharactersCount == 0) {
            return 1.0;
        }
        int transpositions = 0;
        int j = 0;
        for (int i = 0; i < shortestLen; i++) {
            if (shortestTaken[i]) {
                while (!longestTaken[j]) { // find matching char in longest
                    j++;
                }
                assert shortestTaken[i] && longestTaken[j];
                if (shortest.charAt(i) != longest.charAt(j)) {
                    transpositions += 1;
                }
                j++;
            }
        }
        transpositions /= 2;
        final double m = matchingCharactersCount;
        final double jaroSimilarity = (m / shortestLen + m / longestLen +
                (m - transpositions) / m) / 3;
        if (jaroSimilarity <= threshold) {
            return 1 - jaroSimilarity;
        }
        int prefixMaxLen = 4;
        if (prefixMaxLen > shortestLen) {
            prefixMaxLen = shortestLen;
        }
        int commonLen = prefixMaxLen;
        for (int i = 0; i < prefixMaxLen; i++) {
            if (shortest.charAt(i) != longest.charAt(i)) {
                commonLen = i;
                break;
            }
        }
        final double jaroWinklerSimilarity =
                jaroSimilarity + 0.1 * commonLen * (1 - jaroSimilarity);
        return 1 - jaroWinklerSimilarity;
    }
}
