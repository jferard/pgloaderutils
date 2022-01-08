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

package com.github.jferard.pgloaderutils.sniffer;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testJaro() {
        Assert.assertEquals(1.0, StringUtils.getJaroDistance("T", ""), 0.01);
        Assert.assertEquals(0.0, StringUtils.getJaroDistance("T", "T"), 0.01);
        Assert.assertEquals(0.133333333, StringUtils.getJaroDistance("TRATE", "TRACE"), 0.01);
        Assert.assertEquals(0.177777777, StringUtils.getJaroDistance("DwAyNE", "DuANE"), 0.01);
        Assert.assertEquals(0.266666666, StringUtils.getJaroDistance("CRATE", "TRACE"), 0.01);
        Assert.assertEquals(0.066666666, StringUtils.getJaroDistance("arnab", "anrab"), 0.01);
        Assert.assertEquals(0.055555555, StringUtils.getJaroDistance("MARTHA", "MARHTA"), 0.01);
        Assert.assertEquals(0.233333333, StringUtils.getJaroDistance("DIXON", "DICKSONX"), 0.01);
    }

    @Test
    public void testJaroWinkler() {
        Assert.assertEquals(1.0, StringUtils.getJaroWinklerDistance("T", ""), 0.01);
        Assert.assertEquals(0.0, StringUtils.getJaroWinklerDistance("T", "T"), 0.01);
        Assert.assertEquals(0.093333333, StringUtils.getJaroWinklerDistance("TRATE", "TRACE"), 0.01);
        Assert.assertEquals(0.159999999, StringUtils.getJaroWinklerDistance("DwAyNE", "DuANE"), 0.01);
        Assert.assertEquals(0.266666666, StringUtils.getJaroWinklerDistance("CRATE", "TRACE"), 0.01);
        Assert.assertEquals(0.06, StringUtils.getJaroWinklerDistance("arnab", "anrab"), 0.01);
        Assert.assertEquals(0.038888888, StringUtils.getJaroWinklerDistance("MARTHA", "MARHTA"), 0.01);
        Assert.assertEquals(0.186666666, StringUtils.getJaroWinklerDistance("DIXON", "DICKSONX"), 0.01);
    }
}
