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
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import java.util.logging.Logger;

/**
 * Created by jferard on 06/04/17.
 */
public class FlexibleColumnMatcherTest {
    private ColumnMatcher matcher;
    private CSDUtil util;

    @Before
    public void setUp() {
        Logger logger = PowerMock.createNiceMock(Logger.class);
        util = new CSDUtil(logger);
        matcher = new FlexibleColumnMatcher(logger, util, 2);
    }

    @Test
    public void match() throws Exception {
        PowerMock.replayAll();
        Assert.assertTrue(matcher.match(CSDTestHelper.namedField("abcde"), "abcde"));
        Assert.assertTrue(matcher.match(CSDTestHelper.namedField("abcde"), "abcdef"));
        Assert.assertFalse(matcher.match(CSDTestHelper.namedField("abcde"), "abcdefghij"));
        PowerMock.verifyAll();
    }

}