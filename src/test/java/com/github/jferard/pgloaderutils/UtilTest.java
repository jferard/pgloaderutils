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

package com.github.jferard.pgloaderutils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

public class UtilTest {
    @Test
    public void testNormalize() {
        Assert.assertEquals("c'est l'ete", Util.normalize("c'est l'été"));
    }

    @Test
    public void testJoin() {
        Assert.assertEquals("", String.join(",", Collections.<String>emptyList()));
        Assert.assertEquals("a", String.join(",", Collections.singletonList("a")));
        Assert.assertEquals("a,b,c", String.join(",", Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testJoinIntoEmpty() throws IOException {
        final StringBuilder sb = new StringBuilder();
        Util.joinInto(sb, Collections.emptyList(), ",");
        Assert.assertEquals("", sb.toString());
    }

    @Test
    public void testJoinIntoSingleton() throws IOException {
        final StringBuilder sb = new StringBuilder();
        Util.joinInto(sb, Collections.singletonList("a"), ",");
        Assert.assertEquals("a", sb.toString());
    }

    @Test
    public void testJoinInto() throws IOException {
        final StringBuilder sb = new StringBuilder();
        Util.joinInto(sb, Arrays.asList("a", "b", "c"), ",");
        Assert.assertEquals("a,b,c", sb.toString());
    }

    @Test
    public void testEscapeId() {
        Assert.assertEquals("foo", Util.pgEscapeIdentifier("FOO"));
        Assert.assertEquals("\"table\"", Util.pgEscapeIdentifier("table"));
        Assert.assertEquals("\"from\"", Util.pgEscapeIdentifier("from"));
        Assert.assertEquals("\"1foo\"", Util.pgEscapeIdentifier("1foo"));
        Assert.assertEquals("\"a foo\"", Util.pgEscapeIdentifier("a foo"));
        Assert.assertEquals("\"a \"\"foo\"\"\"", Util.pgEscapeIdentifier("a \"foo\""));
    }

    @Test
    public void testEscapeStr() {
        Assert.assertEquals("'table'", Util.pgEscapeString("table"));
        Assert.assertEquals("'1table'", Util.pgEscapeString("1table"));
        Assert.assertEquals("'a table'", Util.pgEscapeString("a table"));
        Assert.assertEquals("E'a ''table'''", Util.pgEscapeString("a 'table'"));
        Assert.assertEquals("E'a ''''table'''''", Util.pgEscapeString("a ''table''"));
    }

    @Test
    public void testToPGString() {
        Assert.assertEquals("", Util.toPGString(null));
        Assert.assertEquals("table", Util.toPGString("table"));
        Assert.assertEquals("1970-01-01", Util.toPGString(new java.util.Date(0)));
        Assert.assertEquals("2022-01-23", Util.toPGString(LocalDate.of(2022, 1, 23)));
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2022, Calendar.JANUARY, 23);
        Assert.assertEquals("2022-01-23", Util.toPGString(c));
    }
}