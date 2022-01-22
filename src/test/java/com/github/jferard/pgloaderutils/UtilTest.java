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

package com.github.jferard.pgloaderutils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class UtilTest {
    @Test
    public void testNormalize() {
        Assert.assertEquals("c'est l'ete", Util.normalize("c'est l'été"));
    }

    @Test
    public void testJoin() {
        Assert.assertEquals("", Util.join(Collections.emptyList(), ","));
        Assert.assertEquals("a", Util.join(Collections.singletonList("a"), ","));
        Assert.assertEquals("a,b,c", Util.join(Arrays.asList("a", "b", "c"), ","));
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
}