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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class SimpleIndexTest {
    public static final Column COL1 = new Column("foo", GeneralDataType.INTEGER);
    public static final Column COL2 = new Column("2bar", GeneralDataType.BOOLEAN);
    public static final Table TABLE = Table.create("table", COL1, COL2);

    @Test
    public void testEmpty() {
        Assert.assertThrows(AssertionError.class,
                () -> SimpleIndex.create(IndexMethod.HASH,
                        TABLE, Collections.emptyList())
        );
    }

    @Test
    public void testCreateOneCol() {
        final SimpleIndex index = SimpleIndex.create(IndexMethod.BTREE, TABLE, COL1);
        Assert.assertEquals("CREATE INDEX table_foo_idx ON table USING btree(foo)",
                index.createIndexQuery());
    }

    @Test
    public void testTwoCols() {
        final SimpleIndex index = SimpleIndex.create(IndexMethod.HASH, TABLE, COL1, COL2);
        Assert.assertEquals("CREATE INDEX table_foo_2bar_idx ON table USING hash(foo, \"2bar\")",
                index.createIndexQuery());
    }

    @Test
    public void testDrop() {
        final SimpleIndex index = SimpleIndex.create(IndexMethod.HASH, TABLE, COL1);
        Assert.assertEquals("DROP INDEX table_foo_idx", index.dropIndexQuery());
    }

}