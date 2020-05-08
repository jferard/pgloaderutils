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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteMapProvider {
	public ByteMapProvider() {}
	
	public char[] get(final Charset charset) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate(256);
		for (int c = 0; c<256; c++) {
			byteBuffer.put((byte) c);
		}
		return new String(byteBuffer.array(), charset).toCharArray();
	}

}
