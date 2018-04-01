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

package com.github.jferard.pgloaderutils.sniffer.csd;

import java.text.Normalizer;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * A Set of util methods.
 */
public class CSDUtil {
    Logger logger;

    public CSDUtil(Logger logger) {
        this.logger = logger;
    }

    public boolean isEmpty(String value) {
        if(value.length()>0&&(value.charAt(0)==' '||value.charAt(value.length()-1)==' '))
            this.logger.fine("RECOMMANDATION : Les valeurs devraient n'avoir espace ni avant, ni après");

        return value.isEmpty() || value.trim().isEmpty();
    }

    public String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s.toLowerCase(Locale.US);
    }

    /** See https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     *      Page name: Algorithm Implementation/Strings/Levenshtein distance
     *      Author: Wikibooks contributors
     *      Publisher: Wikibooks, The Free Textbook Project.
     *      Date of last revision: 6 January 2017 23:57 UTC
     *      Date retrieved: 23 March 2017 07:09 UTC
     *      Permanent URL: https://en.wikibooks.org/w/index.php?title=Algorithm_Implementation/Strings/Levenshtein_distance&oldid=3173832
     *      Page Version ID: 3173832
     * @param lhs
     * @param rhs
     * @return
     */
    public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    /*
    public String toSQL() {
        return this.code+" "+this.type.toString().toLowerCase(Locale.US); // +(optional ? "" : " NOT NULL");
    }

    public String toString() {
        String s = this.name + " (" + this.code + "): " + this.type.toString();
        if (optional)
            s = "[" + s + "]";
        return s;
    }
    */
}
