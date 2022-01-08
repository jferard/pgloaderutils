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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.jferard.pgloaderutils.sniffer.encoding.ByteMapProvider;
import com.github.jferard.pgloaderutils.sniffer.encoding.InputStreamUTF8OrByteCharsetReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InputStreamWithUTF8OrByteReaderTest {

	private Charset iso;
	private char[] cs;
	private char[] cbuf;

	@Before
	public void setUp() {
		final ByteMapProvider bmp = new ByteMapProvider();
		this.iso = Charset.forName("ISO-8859-15");
		this.cbuf = new char[100];
		this.cs = bmp.get(this.iso);
	}

	@Test
	public final void testIsoFalse3Bytes() throws IOException {
		final String s = "é";
		final InputStreamUTF8OrByteCharsetReader r = this.getReaderFromString(s,
				this.iso);
		Assert.assertEquals(1, r.read(this.cbuf, 0, 7));
		Assert.assertEquals("é", new String(this.cbuf, 0, 1));
	}

	public InputStreamUTF8OrByteCharsetReader getReaderFromString(final String s,
			final Charset cs) throws IOException {
		final ByteArrayInputStream is = new ByteArrayInputStream(
				s.getBytes(cs));
		return new InputStreamUTF8OrByteCharsetReader(is, this.cs);
	}
}
