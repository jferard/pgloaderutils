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
package com.github.jferard.pgloaderutils.sniffer.encoding;

import com.github.jferard.pgloaderutils.sniffer.encoding.EncodingSniffer;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class EncodingSnifferTest {
	static final Charset UTF_8 = Charset.forName("UTF-8");
	static final Charset ISO8859_15 = Charset.forName("ISO8859_15");
	private static final String STR = "my first test é { à @";
	private static final Charset ASCII = Charset.forName("US-ASCII");

	@Test
	public final void testUTF_8() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = new ByteArrayInputStream(
				STR.getBytes(UTF_8));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(UTF_8, encodingSniffer.getCharset());
	}

	@Test
	public final void testUTF_8b() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = new ByteArrayInputStream(
				STR.getBytes(UTF_8));
		encodingSniffer.sniff(stream, 10);
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}

	@Test
	public final void testISO() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = new ByteArrayInputStream(
				STR.getBytes(ISO8859_15));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(null, encodingSniffer.getCharset());
	}

	@Test
	public final void testASCII() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = new ByteArrayInputStream(
				STR.getBytes(ASCII));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}

	@Test
	public final void test2() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = new ByteArrayInputStream(Joiner.on("\n")
				.join("Year,Make,Model,Description,Price",
						"1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
				.getBytes(ASCII));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}

	@Test
	public final void test3() throws IOException {
		final EncodingSniffer encodingSniffer = new EncodingSniffer();
		final InputStream stream = Resources.getResource("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part" +
				".csv").openStream();
		encodingSniffer.sniff(stream, 100000);
		Assert.assertEquals(null, encodingSniffer.getCharset()); // null means: byte / byte encoding
	}


}
