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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class InputStreamUTF8OrByteCharsetReader extends Reader {
	private InputStream is; // markSupported
	private char[] isoByteMap;

	private InputStreamWithCharset reader;

	public InputStreamUTF8OrByteCharsetReader(final InputStream is, final char[] isoByteMap) throws IOException {
		this.isoByteMap = isoByteMap;
		this.is = is;
		this.reader = new InputStreamWithUTF8Charset(this.is);
	}

	@Override
	public void close() throws IOException {
		this.is.close();
	}

	@Override
	public int read(final char[] cbuf, final int coffset, final int clen) throws IOException {
		return this.reader.read(this, cbuf, coffset, clen);
	}

	public void fall() {
		this.reader = new InputStreamWithByteCharset(this.is, this.isoByteMap);
	}
}
