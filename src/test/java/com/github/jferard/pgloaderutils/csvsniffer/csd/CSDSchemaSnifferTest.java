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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import java.io.StringReader;
import java.util.logging.Logger;

/**
 * Created by jferard on 07/04/17.
 */
public class CSDSchemaSnifferTest {
    private CSDSchemaSniffer<CSDFieldPattern> sniffer;
    private Logger logger;
    private CSDFieldFactory<CSDFieldPattern> fy;
    private CSDValidatorHelper<CSDFieldPattern> vh;
    private CSDSchemaPattern<CSDFieldPattern> sp;
    private CSDSchema<CSDFieldPattern> s;

    @Before
    public void setUp() {
        this.logger = PowerMock.createNiceMock(Logger.class);
        this.fy = PowerMock.createMock(CSDFieldFactory.class);
        this.vh = PowerMock.createMock(CSDValidatorHelper.class);
        this.sp = PowerMock.createMock(CSDSchemaPattern.class);
        this.s = PowerMock.createMock(CSDSchema.class);

        this.sniffer = new CSDSchemaSniffer<CSDFieldPattern>(this.logger, this.fy, this.vh);

    }

    @Test
    public void sniffNoLine() throws Exception {
        CSVParser p = CSVFormat.RFC4180.parse(new StringReader(""));

        PowerMock.replayAll();
        CSDSchema<CSDFieldPattern> s = this.sniffer.sniff(this.sp, p, 10);
        Assert.assertEquals(null, s);
        PowerMock.verifyAll();
    }

    @Test
    public void sniffBadHeader1() throws Exception {
        CSVParser p = CSVFormat.RFC4180.parse(new StringReader("a,b,c\n1,2,3\n4,5,6"));

        EasyMock.expect(this.vh.validateHeader(EasyMock.isA(CSDValidationResult.class), EasyMock.eq(this.sp), EasyMock.isA(CSVRecord.class))).andReturn(10);
        EasyMock.expect(this.sp.hasOptionalHeader()).andReturn(false);

        PowerMock.replayAll();
        CSDSchema<CSDFieldPattern> s = this.sniffer.sniff(this.sp, p, 10);
        Assert.assertEquals(null, s);
        PowerMock.verifyAll();
    }

    @Test
    public void sniffBadHeader2() throws Exception {
        CSVParser p = CSVFormat.RFC4180.parse(new StringReader("a,b,c\n1,2,3\n4,5,6"));

        EasyMock.expect(this.vh.validateHeader(EasyMock.isA(CSDValidationResult.class), EasyMock.eq(this.sp), EasyMock.isA(CSVRecord.class))).andReturn(-1);

        PowerMock.replayAll();
        CSDSchema<CSDFieldPattern> s = this.sniffer.sniff(this.sp, p, 10);
        Assert.assertEquals(null, s);
        PowerMock.verifyAll();
    }

    @Test
    public void sniffGoodHeader() throws Exception {
        CSVParser p = CSVFormat.RFC4180.parse(new StringReader("a,b,c\n1,2,3\n4,5,6"));

        EasyMock.expect(this.vh.validateHeader(EasyMock.isA(CSDValidationResult.class), EasyMock.eq(this.sp), EasyMock.isA(CSVRecord.class))).andReturn(0);
        EasyMock.expect(this.vh.validateRecord(EasyMock.isA(CSDValidationResult.class), EasyMock.eq(this.sp), EasyMock.isA(CSVRecord.class), EasyMock.eq(1))).andReturn(0);
        EasyMock.expect(this.vh.validateRecord(EasyMock.isA(CSDValidationResult.class), EasyMock.eq(this.sp), EasyMock.isA(CSVRecord.class), EasyMock.eq(2))).andReturn(0);
        EasyMock.expect(this.sp.newSchema(EasyMock.eq(this.fy), EasyMock.isA(CSVRecord.class))).andReturn(this.s);

        PowerMock.replayAll();
        CSDSchema<CSDFieldPattern> s2 = this.sniffer.sniff(this.sp, p, 10);
        Assert.assertEquals(s, s2);
        PowerMock.verifyAll();
    }
}