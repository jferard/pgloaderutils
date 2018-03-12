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

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by jferard on 07/04/17.
 */
public class CSDValidationResultTest {
    private Logger logger;
    private CSDSchemaPattern<CSDFieldPattern> sp;

    @Before
    public void setUp() {
        this.logger = PowerMock.createNiceMock(Logger.class);
        this.sp = (CSDSchemaPattern<CSDFieldPattern>) PowerMock.createMock(CSDSchemaPattern.class);
    }

    @Test
    public void testEmpty() {
        PowerMock.replayAll();
        CSDValidationResult<CSDFieldPattern> vr = new CSDValidationResult<CSDFieldPattern>(logger, sp);


        Assert.assertEquals(0, vr.errorCount());
        Assert.assertTrue(vr.isOk());
        Assert.assertFalse(vr.iterator().hasNext());
        PowerMock.verifyAll();
    }

    @Test
    public void testBunchOfErrors() {

        PowerMock.replayAll();
        CSDValidationResult<CSDFieldPattern> vr = new CSDValidationResult<CSDFieldPattern>(logger, sp);
        vr.noLine();
        vr.badHeader();
        vr.incorrectColumnName(CSDTestHelper.namedField("a"), "b");
        vr.incorrectValue(10, "c", CSDTestHelper.getMandatoryField());
        vr.missingHeader();
        vr.schemaHasTooManyFieldsForHeader(null);
        vr.schemaHasTooManyFieldsForRecord(20, null);

        Assert.assertEquals(7, vr.errorCount());
        Assert.assertFalse(vr.isOk());

        Iterator<CSDValidationError> it = vr.iterator();
        for (int i = 0; i<vr.errorCount(); i++) {
            Assert.assertTrue(it.hasNext());
            Assert.assertNotNull(it.next());
        }
        Assert.assertFalse(it.hasNext());
        PowerMock.verifyAll();
    }
}