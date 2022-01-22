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
import org.junit.function.ThrowingRunnable;

import java.util.Arrays;
import java.util.Collections;

public class HashIndexTest {
    @Test
    public void testEmpty() {
        Assert.assertThrows(AssertionError.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new HashIndex("index", "table", Collections.emptyList());
            }
        });
    }

    @Test
    public void testCreateOneCol() {
        final HashIndex index = new HashIndex("index", "table",
                Collections.singletonList(new Column("foo", GeneralDataType.INTEGER)));
        Assert.assertEquals("CREATE INDEX index ON table (foo) USING hash",
                index.createIndexQuery());
    }

    @Test
    public void testTwoCols() {
        final HashIndex index = new HashIndex("index", "table",
                Arrays.asList(new Column("foo", GeneralDataType.INTEGER),
                        new Column("2bar", GeneralDataType.BOOLEAN)));
        Assert.assertEquals("CREATE INDEX index ON table (foo, \"2bar\") USING hash",
                index.createIndexQuery());
    }

    @Test
    public void testDrop() {
        final HashIndex index = new HashIndex("index", "table",
                Collections.singletonList(new Column("foo", GeneralDataType.INTEGER)));
        Assert.assertEquals("DROP INDEX index",
                index.dropIndexQuery());
    }

}