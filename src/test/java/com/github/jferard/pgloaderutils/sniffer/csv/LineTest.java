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

import com.github.jferard.pgloaderutils.sniffer.csv.Line;
import com.github.jferard.pgloaderutils.sniffer.csv.Part;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

public class LineTest {

	private static final byte COMMA = (byte) ',';
	private static final byte PIPE = (byte)'|';
	private static final Charset CS = Charset.forName("ASCII");

	@Test
	public final void test() {
		final Line line = new Line(1);
		for (final byte b : "a|b|c".getBytes(CS)) {
            line.append(b);
        }
		Assert.assertEquals(2, line.getCount(PIPE));
		final List<Part> parts = line.asParts(PIPE);
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals('a', parts.get(0).getFirstChar());
		Assert.assertEquals('a', parts.get(0).getLastChar());
		Assert.assertEquals('b', parts.get(1).getFirstChar());
		Assert.assertEquals('b', parts.get(1).getLastChar());
		Assert.assertEquals('c', parts.get(2).getFirstChar());
		Assert.assertEquals('c', parts.get(2).getLastChar());
	}
	
	@Test
	public final void test2() {
		final Line line = new Line(1);
		for (final byte b : "a|b|c".getBytes(CS)) {
            line.append(b);
        }
		Assert.assertEquals(0, line.getCount(COMMA));
		final List<Part> parts = line.asParts(COMMA);
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals('a', parts.get(0).getFirstChar());
		Assert.assertEquals('c', parts.get(0).getLastChar());
	}
	
}
