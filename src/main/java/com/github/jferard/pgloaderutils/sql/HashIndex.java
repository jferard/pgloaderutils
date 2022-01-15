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

package com.github.jferard.pgloaderutils.sql;

import com.github.jferard.pgloaderutils.Util;

import java.util.ArrayList;
import java.util.List;

public class HashIndex implements Index {
    private final String name;
    private final String tableName;
    private final List<Column> columns;

    public HashIndex(final String name, final String tableName,
                     final List<Column> columns) {
        assert !columns.isEmpty();
        this.name = name;
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public String createIndexQuery() {
        final List<String> columnNames = new ArrayList<String>(this.columns.size());
        for (final Column column : this.columns) {
            columnNames.add(column.getName());
        }
        return String.format("CREATE INDEX \"%s\" ON \"%s\" (\"%s\") USING hash", this.name, this.tableName,
                Util.join(columnNames, "\", \""));
    }

    @Override
    public String dropIndexQuery() {
        return String.format("DROP INDEX \"%s\"", this.name);
    }
}
