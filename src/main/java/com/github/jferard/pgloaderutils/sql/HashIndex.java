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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HashIndex implements Index {
    public static HashIndex create(final Table table, final Column... columns) {
        return HashIndex.create(table, Arrays.asList(columns));
    }

    public static HashIndex create(final Table table, final List<Column> columns) {
        assert !columns.isEmpty();
        final Stream<Column> stream1 = columns.stream();
        final Stream<Column> stream2 = columns.stream();
        final String tableName1 = table.getName();
        final String name1 = tableName1 + "_" +
                stream1.map(Column::getName)
                        .collect(Collectors.joining("_")) + "_idx";
        final String expression1 =
                stream2.map(c -> Util.pgEscapeIdentifier(c.getName()))
                        .collect(Collectors.joining(", "));
        return new HashIndex(name1, tableName1, expression1);
    }

    private final String name;
    private final String tableName;
    private final String expression;

    public HashIndex(final String name, final String tableName,
                     final String expression) {
        this.name = name;
        this.tableName = tableName;
        this.expression = expression;
    }

    @Override
    public String createIndexQuery() {
        return String.format("CREATE INDEX %s ON %s USING hash(%s)",
                Util.pgEscapeIdentifier(this.name), Util.pgEscapeIdentifier(this.tableName),
                this.expression);
    }

    @Override
    public String dropIndexQuery() {
        return String.format("DROP INDEX %s", Util.pgEscapeIdentifier(this.name));
    }
}
