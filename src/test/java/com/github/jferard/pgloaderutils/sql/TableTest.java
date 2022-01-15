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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TableTest {
    @Test
    public void testCreate() {
        final Table table = new Table("table", Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                new Column("bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("CREATE TABLE \"table\" (\n" +
                "    \"foo\" INTEGER,\n" +
                "    \"bar\" BOOLEAN\n" +
                ")", table.createTableQuery(false));
        Assert.assertEquals("CREATE TABLE IF NOT EXISTS \"table\" (\n" +
                "    \"foo\" INTEGER,\n" +
                "    \"bar\" BOOLEAN\n" +
                ")", table.createTableQuery(true));
    }

    @Test
    public void testDrop() {
        final Table table = new Table("table", Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                new Column("bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("DROP TABLE \"table\"", table.dropTableQuery(false));
        Assert.assertEquals("DROP TABLE IF EXISTS \"table\"", table.dropTableQuery(true));
    }

    @Test
    public void testDisable() {
        final Table table = new Table("table", Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                new Column("bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("UPDATE pg_index\n" +
                "SET indisready=false\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")", table.disableAllIndicesQuery());
    }

    @Test
    public void testEnable() {
        final Table table = new Table("table", Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                new Column("bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("UPDATE pg_index\n" +
                "SET indisready=true\n" +
                "WHERE indrelid = (\n" +
                "    SELECT oid\n" +
                "    FROM pg_class\n" +
                "    WHERE relname='table'\n" +
                ")", table.enableAllIndicesQuery());
    }

    @Test
    public void testInsert() {
        final Table table = new Table("table", Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                new Column("bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("INSERT INTO \"table\" VALUES (\n" +
                "?, ?\n" +
                ")", table.insertValuesQuery());
    }
}