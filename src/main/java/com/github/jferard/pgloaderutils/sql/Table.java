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

import java.util.List;

public class Table {
    private final String name;
    private final List<Column> columns;

    public Table(final String name, final List<Column> columns) {
        assert columns.size() > 0;
        this.name = name;
        this.columns = columns;
    }

    public String createTableQuery() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE \"").append(this.name).append("\"\n");
        sb.append("    ").append(this.columns.get(0).getDefinition());
        for (int i = 1; i < this.columns.size(); i++) {
            sb.append(",\n    ");
            sb.append(this.columns.get(i).getDefinition());
        }
        ;
        sb.append("\n)");
        return sb.toString();
    }


    public String dropTableQuery() {
        return String.format("DROP TABLE \"%s\"", this.name);
    }


    public String insertValuesQuery() {
        final StringBuilder sb =
                new StringBuilder("INSERT INTO \"").append(this.name).append("\" VALUES (\n");
        sb.append("?");
        for (int i = 1; i < this.columns.size(); i++) {
            sb.append(", ?");
        }
        sb.append("\n)");
        return sb.toString();
    }

    public DataType getType(final int i) {
        return this.columns.get(i).getType();
    }

    public String disableAllIndicesQuery() {
        return this.indIsReadyQuery("false");
    }

    public String enableAllIndicesQuery() {
        return this.indIsReadyQuery("true");
    }

    private String indIsReadyQuery(final String indIsReady) {
        return String.format("UPDATE pg_index\n" +
                "SET indisready=%s\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='%s'\n" +
                ")", indIsReady, this.name);
    }
}