/*******************************************************************************
 * CSV Sniffer - A simple sniffer to detect file encoding and CSV format of a file
 *    Copyright (C) 2016 J. Férard <https://github.com/jferard>
 * 
 * This file is part of CSV Sniffer.
 * 
 * CSV Sniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CSV Sniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.github.jferard.csvsniffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jferard.csvsniffer.CSVConstraints;
import com.github.jferard.csvsniffer.CSVFormatSniffer;
import com.google.common.base.Joiner;

public class CSVLinesSnifferTest {
	private static final Charset ASCII = Charset.forName("US-ASCII");
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
	public final void testWithOtherChar() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().build());
		InputStream stream = new ByteArrayInputStream(this.joiner
				.join("A,B,C", "1,abcd,A-A", "2,efgh,A-B", "3,\"ijk,l\",A-C",
						"4,mnop,A-D", "5,\"qrs,t\",A-E", "6,\"uvw,x\",A-F")
				.getBytes(ASCII));

		csvSniffer.sniff(stream, 1000);
		Assert.assertEquals('-', (char) csvSniffer.getDelimiter());
	}

	@Test
	public final void testWithOtherChar2() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().minFields(3).build());
		InputStream stream = new ByteArrayInputStream(
				this.joiner
						.join("A,B,C", "1,abcd,A-A", "2,efgh,A-B", "3,\"ijk,l\",A-C",
								"4,mnop,A-D", "5,\"qrs,t\",A-E", "6,\"uvw,x\",A-F")
						.getBytes(ASCII));

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
	public final void test() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().build());
		InputStream stream = new ByteArrayInputStream(
				this.joiner.join("Year,Make,Model", "1997,Ford,E350",
						"2000,Mercury,Cougar").getBytes(ASCII));

		csvSniffer.sniff(stream, 1000);
		Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
		Assert.assertEquals(0, (char) csvSniffer.getQuote());
		Assert.assertEquals(0, (char) csvSniffer.getEscape());
	}

	@Test
	public final void test2() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().build());
		InputStream stream = new ByteArrayInputStream(this.joiner
				.join("Year,Make,Model,Description,Price",
						"1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
				.getBytes(ASCII));

		csvSniffer.sniff(stream, 1000);
		Assert.assertEquals('.', (char) csvSniffer.getDelimiter());
	}
	
	@Test
	public final void test2b() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().minFields(5).build());
		InputStream stream = new ByteArrayInputStream(this.joiner
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

}
