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
package com.github.jferard.pgloaderutils.sniffer.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class PartTest {

	@Test
	public final void test1() throws UnsupportedEncodingException {
		final byte[] arr = "abcde".getBytes("ASCII");
		final Part part = new Part(arr, 0, arr.length);
		Assert.assertEquals('a', part.getFirstChar());
		Assert.assertEquals('e', part.getLastChar());
		
		Assert.assertEquals('c', part.findCharBefore('d'));
		Assert.assertEquals(-1, part.findCharBefore('z'));
	}
	
	@Test
	public final void test2() throws UnsupportedEncodingException {
		final byte[] arr = "   abcde ".getBytes("ASCII");
		final Part part = new Part(arr, 0, arr.length);
		part.trimSpaces();
		Assert.assertEquals('a', part.getFirstChar());
		Assert.assertEquals('e', part.getLastChar());
		
		Assert.assertEquals('c', part.findCharBefore('d'));
		Assert.assertEquals(-1, part.findCharBefore('z'));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testEmptyArray() throws UnsupportedEncodingException {
		final byte[] arr = "".getBytes("ASCII");
		final Part part = new Part(arr, 1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testEmptyPart() throws UnsupportedEncodingException {
		final byte[] arr = "   abcde ".getBytes("ASCII");
		final Part part = new Part(arr, 0, 0);
	}
}
