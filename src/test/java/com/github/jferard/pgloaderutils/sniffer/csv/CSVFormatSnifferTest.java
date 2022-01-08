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

package com.github.jferard.pgloaderutils.sniffer.csv;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;

public abstract class CSVFormatSnifferTest {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private Joiner joiner;

    @Before
    public void setUp() {
        this.joiner = Joiner.on('\n');
    }

    /**
     * @throws IOException
     * @see https://en.wikipedia.org/wiki/Comma-separated_values
     */
    @Test
    public final void testWithOtherChar() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("A,B,C", "1,abcd,A-A", "2,efgh,A-B", "3,\"ijk,l\",A-C", "4,mnop,A-D",
                        "5,\"qrs,t\",A-E",
                        "6,\"uvw,x\",A-F").getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
    }

    @Test
    public final void testWithOtherChar2() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getFieldsSniffer(3);
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("A,B,C", "1,abcd,A-A", "2,efgh,A-B", "3,\"ijk,l\",A-C", "4,mnop,A-D",
                        "5,\"qrs,t\",A-E",
                        "6,\"uvw,x\",A-F").getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('"', (char) csvSniffer.getQuote());
        Assert.assertEquals(0, (char) csvSniffer.getEscape());
    }

    /**
     * @throws IOException
     * @see https://en.wikipedia.org/wiki/Comma-separated_values
     */
    @Test
    public final void test() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(
                this.joiner.join("Year,Make,Model", "1997,Ford,E350", "2000,Mercury,Cougar")
                        .getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
        Assert.assertEquals(0, (char) csvSniffer.getQuote());
        Assert.assertEquals(0, (char) csvSniffer.getEscape());
    }

    @Test
    public final void test2() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("Year,Make,Model,Description,Price",
                        "1997,Ford,E350,\"ac, abs, moon\",3000.00",
                        "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
                        "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
                        "1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
                .getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
    }

    @Test
    public final void test2b() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getFieldsSniffer(5);
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("Year,Make,Model,Description,Price",
                        "1997,Ford,E350,\"ac, abs, moon\",3000.00",
                        "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
                        "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
                        "1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
                .getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('"', (char) csvSniffer.getQuote());
        Assert.assertEquals('"', (char) csvSniffer.getEscape());
    }

    @Test
    public final void test3() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getCommaSemiColonSniffer();
        final InputStream stream = Resources
                .getResource("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part" + ".csv")
                .openStream();
        csvSniffer.sniff(stream, 100000);
        Assert.assertEquals(';', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('"', (char) csvSniffer.getQuote());
        Assert.assertEquals(0, (char) csvSniffer.getEscape());
    }

    @Test
    public final void test3e() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = Resources
                .getResource("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part" + ".csv")
                .openStream();
        csvSniffer.sniff(stream, 100000);
        Assert.assertEquals(';', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('"', (char) csvSniffer.getQuote());
        Assert.assertEquals(0, (char) csvSniffer.getEscape());
    }

    @Test
    public final void test3b() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getTabPipeSniffer();
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("Year	Make	Model	Description	Price",
                        "1997	Ford	E350	moon	3000,00",
                        "1999	Chevy	Venture		4900,00",
                        "1999	Chevy	Large		5000,00",
                        "1996	Jeep	Cherokee	air	4799,00").getBytes(ASCII));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals('\t', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('\0', (char) csvSniffer.getQuote());
        Assert.assertEquals('\0', (char) csvSniffer.getEscape());
    }

    @Test
    public final void test3c() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getTabPipeSniffer();
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("Year	Make	Model	Description	Price",
                        "1997	Ford	E350	moon	3000,00",
                        "1999	Chevy	Venture		4900,00",
                        "1999	Chevy	Large		5000,00",
                        "1996	Jeep	Cherokee	air	4799,00").getBytes(UTF8));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals('\t', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('\0', (char) csvSniffer.getQuote());
        Assert.assertEquals('\0', (char) csvSniffer.getEscape());
    }

    @Test
    public final void test4() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getTabPipeSniffer();
        final InputStream stream = new ByteArrayInputStream(this.joiner
                .join("Year|Make|Model|Description|Price", "1997|*|E350|moon|3000,00",
                        "1999|*|Venture|d|4900,00",
                        "1999|*|Large|d|5000,00", "1996|*|Cherokee|air|4799,00").getBytes(UTF8));

        csvSniffer.sniff(stream, 1000);
        Assert.assertEquals('|', (char) csvSniffer.getDelimiter());
        Assert.assertEquals('\0', (char) csvSniffer.getQuote());
        Assert.assertEquals('\0', (char) csvSniffer.getEscape());
    }

    @Test
    public final void testDelimiterQuote() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(
                this.joiner.join("\"a\";b;", "\"a\";b;", "\"a\";b;", "\"a\";b;", "\"a\";b;")
                        .getBytes(UTF8));

        csvSniffer.sniff(stream, 1000);
        Assert.assertThat(this.delimQuote(csvSniffer),
                either(is(Arrays.asList(';', '"'))).or(is(Arrays.asList('"', ';'))));
    }

    @Test
    public final void testDelimiterQuote3() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(
                this.joiner.join(";c;<d<", ";c;<d<", ";c;<d<", ";c;<d<", ";c;<d<", ";c;<d<")
                        .getBytes(UTF8));

        csvSniffer.sniff(stream, 1000);
        // ';', '<' and ';' may be a quote
        Assert.assertThat(this.delimQuote(csvSniffer),
                either(is(Arrays.asList(';', '<'))).or(is(Arrays.asList('<', ';'))));
    }

    @Test
    public final void testDelimiterQuote2() throws IOException, ParseException {
        final CSVFormatSniffer csvSniffer = this.getStandardSniffer();
        final InputStream stream = new ByteArrayInputStream(
                this.joiner.join(";c;\"d\"", ";c;\"d\"", ";c;\"d\"", ";c;\"d\"", ";c;\"d\"")
                        .getBytes(UTF8));

        csvSniffer.sniff(stream, 1000);
        // '"', ';' and '"' may be a quote
        Assert.assertThat(this.delimQuote(csvSniffer),
                either(is(Arrays.asList(';', '"'))).or(is(Arrays.asList('"', ';'))));
    }

    protected abstract CSVFormatSniffer getFieldsSniffer(int i);

    protected abstract CSVFormatSniffer getTabPipeSniffer();

    protected abstract CSVFormatSniffer getCommaSemiColonSniffer();

    protected abstract CSVFormatSniffer getStandardSniffer();

    private List<Character> delimQuote(final CSVFormatSniffer csvSniffer) {
        return Arrays.asList((char) csvSniffer.getDelimiter(), (char) csvSniffer.getQuote());
    }
}
