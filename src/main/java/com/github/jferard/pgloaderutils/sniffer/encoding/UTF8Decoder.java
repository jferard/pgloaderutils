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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;

/**
 * The class UTF8Decoder provides a stream of code points. It guarantees that if
 * the stream cannot be decoded, the stream will be reset in a stable state:
 * the last bytes that cannot be merged in a code point are reset in the stream.
 * 
 * In detail: each utf-8 encoding of a unicode code point is made of one leading
 * byte and from 0 to 3 trailing byte, depending on the value of the leading
 * byte. If the leading byte, or any of the trailing bytes are not consistent,
 * those bytes are reset in the stream. The consumed bytes before the error are
 * *not* reset in the stream.
 * 
 * An optional BOM is consumed.
 * 
 * @author Julien Férard (C) 2016
 *
 */
public class UTF8Decoder {
	/**
	 * supports mark and reset
	 */
	private InputStream is;

	UTF8Decoder(final InputStream is) {
		if (is.markSupported()) {
            this.is = is;
        } else {
            this.is = new BufferedInputStream(is);
        }
	}

	/**
	 * Gobble BOM or do nothing.
	 * @return true if there was a BOM
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean gobbleBOM() throws IOException {
		this.is.mark(3);
		if (this.is.read() == SnifferConstants.BOM_1 && this.is.read() == SnifferConstants.BOM_2
				&& this.is.read() == SnifferConstants.BOM_3) {
			return true;
		} 		else {
			this.is.reset();
			return false;
		}
	}

	/**
	 * DECODING UTF-8 https://www.ietf.org/rfc/rfc2279.txt
	 * 
	 * @return the next unicode value as an int, -1 if end of stream
	 */
	public int readUnicodeValue() throws CharacterCodingException, IOException {
		this.is.mark(3);
		final int firstByte = this.is.read();
		if (firstByte == -1) {
            return -1;
        }

		int unicodeValue = 0;
		final int expectedLen;
		// 1) Lead byte analysis
		if ((firstByte & SnifferConstants.B10000000) == SnifferConstants.B00000000) {
			// b1 = 0b0xxxxxxx
			unicodeValue = firstByte;
			expectedLen = 1;
		} else if ((firstByte & SnifferConstants.B11100000) == SnifferConstants.B11000000) {
			// b1 = 0b110xxxxx
			unicodeValue = firstByte & SnifferConstants.B00011111;
			expectedLen = 2;
		} else if ((firstByte & SnifferConstants.B11110000) == SnifferConstants.B11100000) {
			// b1 = 0b1110xxxx
			unicodeValue = firstByte & SnifferConstants.B00001111;
			expectedLen = 3;
		} else if ((firstByte & SnifferConstants.B11111000) == SnifferConstants.B11110000) {
			// b1 = 0b11110xxx
			unicodeValue = firstByte & SnifferConstants.B00000111;
			expectedLen = 4;
		} else { // not a UTF-8 leading byte
			this.is.reset();
			throw new CharacterCodingException();
		}

		// build unicode value
		for (int i = 1; i < expectedLen; i++) {
			final int trailingByte = this.is.read();
			if (trailingByte == -1) { // abnormal end of trailing bytes
				this.is.reset();
				throw new CharacterCodingException();
			}

			final int ci = trailingByte & SnifferConstants.B01111111;
			// 0b10xxxxxx & 0b01111111 == 0b00xxxxxx
			if (ci <= SnifferConstants.B00111111) { // UTF-8 trailing byte : 0b10xxxxxx
				unicodeValue = (unicodeValue << SnifferConstants.UNICODE_TRAILING_BYTE_X_BITS)
						+ ci;
			} else { // abnormal trailing byte
				this.is.reset();
				throw new CharacterCodingException();
			}
		}
		return unicodeValue;
	}
}
