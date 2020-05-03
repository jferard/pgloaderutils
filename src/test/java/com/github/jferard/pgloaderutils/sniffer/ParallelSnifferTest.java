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
package com.github.jferard.pgloaderutils.sniffer;

import com.github.jferard.pgloaderutils.sniffer.csv.BasicCSVConstraints;
import com.github.jferard.pgloaderutils.sniffer.csv.CSVFormatSniffer;
import com.github.jferard.pgloaderutils.sniffer.encoding.EncodingSniffer;
import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ParallelSnifferTest {
	private static final Charset ASCII = Charset.forName("US-ASCII");
	private Joiner joiner;

	@Before
	public void setUp() {
		this.joiner = Joiner.on('\n');
	}

	@Test
	public final void test() throws IOException {
		CSVFormatSniffer csvSniffer = CSVFormatSniffer.createBasic(
				BasicCSVConstraints.builder().minFields(5).build());
		EncodingSniffer encodingSniffer = new EncodingSniffer();
		ParallelSniffer parallelSniffer = new ParallelSniffer(csvSniffer,
				encodingSniffer);

		final byte[] bytes = this.joiner
				.join("Year,Make,Model,Description,Price",
						"1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
				.getBytes(ASCII);
		InputStream stream = new ByteArrayInputStream(bytes);

		parallelSniffer.sniff(stream, 1000);
		Assert.assertEquals(',', (char) csvSniffer.getDelimiter());
		Assert.assertEquals('"', (char) csvSniffer.getQuote());
		Assert.assertEquals('"', (char) csvSniffer.getEscape());
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}
}
