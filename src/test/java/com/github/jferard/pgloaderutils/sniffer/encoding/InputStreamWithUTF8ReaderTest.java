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

package com.github.jferard.pgloaderutils.sniffer.encoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.jferard.pgloaderutils.sniffer.encoding.InputStreamUTF8OrByteCharsetReader;
import com.github.jferard.pgloaderutils.sniffer.encoding.InputStreamWithUTF8Charset;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

public class InputStreamWithUTF8ReaderTest {
	private Charset utf8;
	private Charset iso;
	private char[] cbuf;

	@Before
	public void setUp() throws Exception {
		this.utf8 = Charset.forName("UTF-8");
		this.iso = Charset.forName("ISO-8859-15");
		this.cbuf = new char[100];
	}

	@Test
	public final void testWithZeroToSevenChars() throws IOException {
		final String s = "&éè-_-ç";
		for (int i = 0; i <= 7; i++) {
			final InputStreamWithUTF8Charset r = this.getReaderFromString(s,
					this.utf8);
			Assert.assertEquals(i, r.read(null, this.cbuf, 0, i));
			Assert.assertEquals(new String(s.toCharArray()).substring(0, i),
					new String(this.cbuf, 0, i));
		}
	}

	@Test
	public final void testWithLongRead() throws IOException {
		final String s = "&éè-_-ç";
		final InputStreamWithUTF8Charset r = this.getReaderFromString(s, this.utf8);
		Assert.assertEquals(7, r.read(null, this.cbuf, 0, 100));
		Assert.assertEquals(new String(s.toCharArray()),
				new String(this.cbuf, 0, 7));
	}

	@Test
	public final void testIsoFalse3Bytes() throws IOException {
		final InputStreamUTF8OrByteCharsetReader p = PowerMock
				.createMock(InputStreamUTF8OrByteCharsetReader.class);

		// play
		p.fall();
		EasyMock.expect(p.read(this.cbuf, 0, 7)).andReturn(65);

		PowerMock.replayAll();
		final String s = "é";
		final InputStreamWithUTF8Charset r = this.getReaderFromString(s, this.iso);
		Assert.assertEquals(r.read(p, this.cbuf, 0, 7), 65);
		PowerMock.verifyAll();
	}

	@Test
	public final void testIsoButProcessedAsUTF() throws IOException {
		final String s = "Ã©";
		final InputStreamWithUTF8Charset r = this.getReaderFromString(s, this.iso);
		Assert.assertEquals(r.read(null, this.cbuf, 0, 7), 1);
		Assert.assertEquals("é", new String(this.cbuf, 0, 1));
	}

	public InputStreamWithUTF8Charset getReaderFromString(final String s,
			final Charset cs) throws IOException {
		final ByteArrayInputStream is = new ByteArrayInputStream(
				s.getBytes(cs));
		return new InputStreamWithUTF8Charset(is);
	}
}
