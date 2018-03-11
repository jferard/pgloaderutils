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

package com.github.jferard.pgloaderutils.csvsniffer.csd;

import java.util.logging.Logger;

/**
 * Returns true iff the Levensthein distance is less or equal than 2.
 */
class FlexibleColumnMatcher implements ColumnMatcher {
    private Logger logger;
    private CSDUtil util;

    public FlexibleColumnMatcher(Logger logger, CSDUtil util) {
        this.logger = logger;
        this.util = util;
    }
    @Override
    public boolean match(String expected, String actual) {
        if (expected.equals(actual))
            return true;
        else if (this.util.levenshteinDistance(this.util.stripAccents(expected), this.util.stripAccents(actual)) <= 2) {
            this.logger.fine("The column names are close but not equal. Expected : '"+expected+"'. Actual: '"+actual+"'.");
            return true;
        } else
            this.logger.fine("The column names are different. Expected : '"+expected+"'. Actual: '"+actual+"'.");
            return false;
    }
}
