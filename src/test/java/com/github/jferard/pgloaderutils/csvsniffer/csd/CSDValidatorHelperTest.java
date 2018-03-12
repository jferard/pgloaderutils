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

import org.apache.commons.csv.CSVRecord;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by jferard on 07/04/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CSVRecord.class)
public class CSDValidatorHelperTest {
    private Logger logger;
    private CSDValidatorHelper<CSDFieldPattern> vh;
    private CSDValidationResult<CSDFieldPattern> vr;
    private CSVRecord r;

    @Before
    public void setUp() {
        this.logger = PowerMock.createNiceMock(Logger.class);
        this.vh = new CSDValidatorHelper<CSDFieldPattern>(this.logger, new ExactColumnMatcher(logger));
        this.vr = (CSDValidationResult<CSDFieldPattern>) PowerMock.createMock(CSDValidationResult.class);
        this.r = PowerMock.createMock(CSVRecord.class);
    }

    @Test
    public void validateHeaderHavingTooFewRows() throws Exception {
        EasyMock.expect(this.r.size()).andReturn(1);
        this.vr.schemaHasTooManyFieldsForHeader(this.r);

        PowerMock.replayAll();
        SizedIterable<CSDFieldPattern> fields = CSDTestHelper.fromCollection(Arrays.asList(CSDTestHelper.getMandatoryField(), CSDTestHelper.getMandatoryField(), CSDTestHelper.getOptionalField()));
        Assert.assertEquals(-1, this.vh.validateHeader(this.vr, fields, this.r));
        PowerMock.verifyAll();
   }

    @Test
    public void validateHeaderOk() throws Exception {
        EasyMock.expect(this.r.size()).andReturn(3);
        EasyMock.expect(this.r.get(0)).andReturn("name");
        EasyMock.expect(this.r.get(1)).andReturn("name");
        EasyMock.expect(this.r.get(2)).andReturn("name");

        PowerMock.replayAll();
        SizedIterable<CSDFieldPattern> fields = CSDTestHelper.fromCollection(Arrays.asList(CSDTestHelper.getMandatoryField(), CSDTestHelper.getMandatoryField(), CSDTestHelper.getOptionalField()));
        Assert.assertEquals(3, fields.size());
        Assert.assertEquals(0, this.vh.validateHeader(this.vr, fields, this.r));
        PowerMock.verifyAll();
    }

    @Test
    public void validateRecordHavingTooFewRows() throws Exception {
        EasyMock.expect(this.r.size()).andReturn(1);
        this.vr.schemaHasTooManyFieldsForRecord(5, this.r);

        PowerMock.replayAll();
        SizedIterable<CSDFieldPattern> fields = CSDTestHelper.fromCollection(Arrays.asList(CSDTestHelper.getMandatoryField(), CSDTestHelper.getMandatoryField(), CSDTestHelper.getOptionalField()));
        Assert.assertEquals(-1, this.vh.validateRecord(this.vr, fields, this.r, 5));
        PowerMock.verifyAll();
    }

    @Test
    public void validateRecordOk() throws Exception {
        EasyMock.expect(this.r.size()).andReturn(3);
        EasyMock.expect(this.r.get(0)).andReturn("name");
        EasyMock.expect(this.r.get(1)).andReturn("name");
        EasyMock.expect(this.r.get(2)).andReturn("name");

        PowerMock.replayAll();
        SizedIterable<CSDFieldPattern> fields = CSDTestHelper.fromCollection(Arrays.asList(CSDTestHelper.getMandatoryField(), CSDTestHelper.getMandatoryField(), CSDTestHelper.getOptionalField()));
        Assert.assertEquals(3, fields.size());
        Assert.assertEquals(0, this.vh.validateRecord(this.vr, fields, this.r, 5));
        PowerMock.verifyAll();
    }
}