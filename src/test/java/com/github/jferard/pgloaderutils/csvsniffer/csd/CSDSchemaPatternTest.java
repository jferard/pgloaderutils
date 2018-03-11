/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
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

package com.github.jferard.pgloaderutils.csvsniffer.csd;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by jferard on 06/04/17.
 */
public class CSDSchemaPatternTest {
    @Test
    public void testEmpty() {
        CSDSchemaPattern<CSDField> s = new CSDSchemaPattern<CSDField>(Collections.<CSDField>emptyList(), false);
        Assert.assertFalse(s.hasOptionalHeader());
        Assert.assertFalse(s.iterator().hasNext());
        Assert.assertEquals(0, s.size());
        Assert.assertEquals("[]", s.toString());
    }

    @Test
    public void testOneField() {
        CSDField f = TestUtil.getMandatoryField();
        CSDSchemaPattern<CSDField> s = new CSDSchemaPattern<CSDField>(Arrays.asList(f), true);
        Assert.assertTrue(s.hasOptionalHeader());
        Assert.assertTrue(s.iterator().hasNext());
        Assert.assertEquals(f, s.iterator().next());
        Assert.assertEquals(1, s.size());
    }

    @Test
    public void testOneStartField() {
        CSDField f = TestUtil.getStarField();
        CSDSchemaPattern<CSDField> s = new CSDSchemaPattern<CSDField>(Arrays.asList(f), true);
        Assert.assertTrue(s.hasOptionalHeader());
        Assert.assertTrue(s.iterator().hasNext());
        Assert.assertEquals(f, s.iterator().next());
        Assert.assertEquals(0, s.size());
    }
}