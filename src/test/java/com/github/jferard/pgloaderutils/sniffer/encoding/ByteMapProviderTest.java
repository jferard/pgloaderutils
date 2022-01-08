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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import com.github.jferard.pgloaderutils.sniffer.encoding.ByteMapProvider;
import org.junit.Assert;
import org.junit.Test;

public class ByteMapProviderTest {
	@Test
	public final void test() throws CharacterCodingException {
		final ByteMapProvider bmp = new ByteMapProvider();
		final char[] cs = bmp.get(Charset.forName("ISO-8859-15"));
		Assert.assertEquals(256, cs.length);
		Assert.assertEquals('é', cs[0xe9]);
	}

}
