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
import java.nio.charset.CharacterCodingException;

class InputStreamWithUTF8Charset implements InputStreamWithCharset {
	private int remainingUTF16Char;
	private int curOffset;
	private int charCount;
	private UTF8Decoder decoder;

	InputStreamWithUTF8Charset(final InputStream is) throws IOException {
		this.decoder = new UTF8Decoder(is);
		this.decoder.gobbleBOM();
		this.remainingUTF16Char = -1;
	}

	@Override
	public int read(final InputStreamUTF8OrByteCharsetReader parent, final char[] cbuf,
                    final int coffset, final int clen) throws IOException {
		if (clen <= 0) {
            return 0;
        }

		this.charCount = 0;
		this.curOffset = coffset;

		// EDGE CASE : there was a trailing UTF-16 part of character waiting.
		if (this.remainingUTF16Char != -1) {
			cbuf[this.curOffset++] = (char) this.remainingUTF16Char;
			this.charCount++;
			this.remainingUTF16Char = -1;
		}

		try {
			while (this.charCount < clen) {
				final int unicodeValue = this.decoder.readUnicodeValue();
				if (unicodeValue == -1) {
                    return this.charCount;
                }

				this.writeUTF16Bytes(cbuf, clen, unicodeValue);
			}
			return this.charCount;
		} catch (final CharacterCodingException e) {
			return this.fallAndFinishRead(parent, cbuf, clen);
		}
	}

	/**
	 * ENCODING IN UTF-16 https://www.ietf.org/rfc/rfc2781.txt, 2.1 Encoding
	 * UTF-16
	 */
	private void writeUTF16Bytes(final char[] cbuf, final int clen, final int unicodeValue) {
		if (unicodeValue < 0x10000) {
			cbuf[this.curOffset++] = (char) unicodeValue;
			this.charCount++;
		} else {
			// U' = yyyyyyyyyyxxxxxxxxxx
			// W1 = 110110yyyyyyyyyy
			// W2 = 110111xxxxxxxxxx
			final int y = unicodeValue >> 10;
			final int x = unicodeValue - y;
			cbuf[this.curOffset++] = (char) (SnifferConstants.B1101100000000000 + y);
			this.charCount++;
			if (this.charCount == clen) { // bad luck!
				this.remainingUTF16Char = (char) (SnifferConstants.B1101110000000000
						+ x);
			} else {
				cbuf[this.curOffset++] = (char) (SnifferConstants.B1101110000000000
						+ x);
				this.charCount++;
			}
		}
	}

	/**
	 * Try to use our spare wheel...
	 * 
	 * @param parent
	 * @param cbuf
	 * @param clen
	 * @return
	 * @throws IOException
	 */
	private int fallAndFinishRead(final InputStreamUTF8OrByteCharsetReader parent,
                                  final char[] cbuf, final int clen) throws IOException {
		parent.fall();
		final int read = parent.read(cbuf, this.curOffset, clen - this.charCount);
		if (read == -1) {
            return this.charCount;
        } else {
            return this.charCount + read;
        }
	}
}