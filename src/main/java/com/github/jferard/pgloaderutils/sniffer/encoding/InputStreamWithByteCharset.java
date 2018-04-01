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

import java.io.IOException;
import java.io.InputStream;

class InputStreamWithByteCharset implements InputStreamWithCharset {
	private char[] isoByteMap;
	private InputStream is;

	InputStreamWithByteCharset(InputStream is, char[] isoByteMap) {
		this.is = is;
		this.isoByteMap = isoByteMap;
	}

	@Override
	public int read(InputStreamUTF8OrByteCharsetReader parent, char[] cbuf,
			int coffset, int clen) throws IOException {
		if (clen <= 0)
			return 0;

		int charCount;
		int curOffset = coffset;
		for (charCount = 0; charCount < clen; charCount++) {
			int firstByte = this.is.read();
			if (firstByte == -1)
				return charCount;

			cbuf[curOffset++] = this.isoByteMap[firstByte];
		}
		return charCount;
	}
}