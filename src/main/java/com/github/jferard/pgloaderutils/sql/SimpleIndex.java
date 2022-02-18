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
import java.util.Locale;
import java.util.stream.Collectors;

public class SimpleIndex implements Index {
    public static SimpleIndex create(final IndexMethod method, final Table table,
                                     final Column... columns) {
        return SimpleIndex.create(method, table, Arrays.asList(columns));
    }

    public static SimpleIndex create(final IndexMethod method, final Table table,
                                     final List<Column> columns) {
        assert !columns.isEmpty();
        final String expression =
                columns.stream().map(c -> Util.pgEscapeIdentifier(c.getName()))
                        .collect(Collectors.joining(", "));
        return create(method, table, columns, expression);
    }

    public static SimpleIndex create(final IndexMethod method, final Table table,
                                     final List<Column> columns,
                                     final String expression) {
        final String tableName = table.getName();
        final String name = tableName + "_" +
                columns.stream().map(Column::getName)
                        .collect(Collectors.joining("_")) + "_idx";
        return new SimpleIndex(method, name, tableName, expression);
    }

    private final String name;
    private final String tableName;
    private final String expression;
    private final IndexMethod method;

    public SimpleIndex(final IndexMethod method, final String name, final String tableName,
                       final String expression) {
        this.method = method;
        this.name = name;
        this.tableName = tableName;
        this.expression = expression;
    }

    @Override
    public String createIndexQuery() {
        return String.format("CREATE INDEX %s ON %s USING %s(%s)",
                Util.pgEscapeIdentifier(this.name), Util.pgEscapeIdentifier(this.tableName),
                this.method.toString().toLowerCase(Locale.ROOT), this.expression);
    }

    @Override
    public String dropIndexQuery() {
        return String.format("DROP INDEX %s", Util.pgEscapeIdentifier(this.name));
    }
}
