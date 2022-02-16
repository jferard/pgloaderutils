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

package com.github.jferard.pgloaderutils.reader;

import com.github.jferard.javamcsv.MetaCSVDataException;
import com.github.jferard.javamcsv.MetaCSVParseException;
import com.github.jferard.javamcsv.MetaCSVReadException;
import com.github.jferard.javamcsv.MetaCSVReader;
import com.github.jferard.pgloaderutils.TestHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FromMetaCSVFileReaderTest {
    @Test
    public void test()
            throws MetaCSVDataException, MetaCSVReadException, MetaCSVParseException, IOException {
        final byte[] bytes = ("boolean,currency,date,datetime,float,integer,percentage,text\r\n" +
                "T,$15,01/12/2020,NULL,\"10,000.5\",12 354,56.5%,Foo\r\n" +
                "F,\"$-1,900.5\",NULL,2020-12-01 09:30:55,-520.8,-1 000,-12.8%,Bar\r\n").getBytes(
                StandardCharsets.UTF_8);
        final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        final MetaCSVReader reader = MetaCSVReader
                .create(is, "data,null_value,NULL", "data,col/0/type,boolean/T/F",
                        "data,col/1/type,\"currency/pre/$/decimal/,/.\"",
                        "data,col/2/type,date/dd\\/MM\\/yyyy",
                        "data,col/3/type,datetime/yyyy-MM-dd HH:mm:ss",
                        "data,col/4/type,\"float/,/.\"", "data,col/5/type,\"integer/ \"",
                        "data,col/6/type,\"percentage/post/%/float/,/.\"");
        final FromMetaCSVFileReader fromReader = new FromMetaCSVFileReader(reader);
        Assert.assertEquals("CREATE TABLE table (\n" +
                "    boolean BOOLEAN,\n" +
                "    currency DECIMAL,\n" +
                "    date DATE,\n" +
                "    datetime TIMESTAMP,\n" +
                "    float FLOAT,\n" +
                "    integer INTEGER,\n" +
                "    percentage FLOAT,\n" +
                "    text TEXT\n" +
                ")", fromReader.createSQL("table"));
        fromReader.open();
        Assert.assertEquals("true,15,2020-12-01,,10000.5,12354,0.565,Foo\r\n" +
                        "false,-1900.5,,2020-12-01T09:30:55,-520.8,-1000,-0.128,Bar\r\n",
                TestHelper.readAll(fromReader));
        fromReader.close();
    }

}