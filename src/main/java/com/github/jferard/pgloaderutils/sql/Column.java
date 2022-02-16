/*
 * Some utilities for loading csv data into a PostgreSQL database:
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

package com.github.jferard.pgloaderutils.sql;

import com.github.jferard.pgloaderutils.Util;

/** A column in table */
public class Column {
    private final String name;
    private final DataType type;

    public Column(final String name, final DataType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return the column def in text format
     */
    public String getDefinition() {
        return String.format("%s %s", Util.pgEscapeIdentifier(this.name), this.type);
    }

    /**
     * @return the data type of the column
     */
    public DataType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}