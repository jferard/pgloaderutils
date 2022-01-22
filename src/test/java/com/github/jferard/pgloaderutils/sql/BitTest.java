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

import java.sql.Types;

public class BitTest {
    @Test
    public void test() {
        final Bit b = Bit.BIT6;
        Assert.assertEquals(Types.BIT, b.getSqlType());
        Assert.assertEquals("BIT(6)", b.toString());
    }

    @Test
    public void testHashCode() {
        final Bit b = Bit.BIT6;
        Assert.assertEquals(6, b.hashCode());
    }

    @Test
    public void testEquals() {
        final Bit b = Bit.BIT6;
        Assert.assertEquals(Bit.BIT6, Bit.BIT6);
        Assert.assertEquals(new Bit(6), Bit.BIT6);

        Assert.assertNotEquals(Bit.BIT6, Bit.BIT7);
        Assert.assertNotEquals(Bit.BIT6, new Object());
    }
}