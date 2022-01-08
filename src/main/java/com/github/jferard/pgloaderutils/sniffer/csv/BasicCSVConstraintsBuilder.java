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

/**
 * The main constraints are : - delimiter, quote and escape can't be a letter or
 * a digit. - quote will be added to escape chars.
 */
public class BasicCSVConstraintsBuilder {
	private static final int ASCII_CHAR_COUNT = 128;
	private byte[] allowedDelimiters;
	private byte[] allowedQuotes;
	private int minFields;
	private byte[] allowedEscapes;

	private static byte all[];

	BasicCSVConstraintsBuilder() {
		this.allowedDelimiters = getAllChars();
		this.allowedQuotes = getAllChars();
		this.allowedEscapes = getAllChars();
		this.minFields = 2;
	}

	private static byte[] getAllChars() {
		if (BasicCSVConstraintsBuilder.all == null) {
			all = new byte[ASCII_CHAR_COUNT-10-26*2-2];
			int i = 0;
			// hack to avoid endless loop : b ALWAYS < 128
			for (byte b = 0; b >= 0 ; b++) {  
				if (b == '\r' || b == '\n') {
					continue;
				}
				if (b >= '0' && b <= '9') {
					continue;
				}
				if (b >= 'A' && b <= 'Z') {
					continue;
				}
				if (b >= 'a' && b <= 'z') {
					continue;
				}

				all[i++] = b;
			}
		}
		return BasicCSVConstraintsBuilder.all;
	}

	public BasicCSVConstraintsBuilder allowedDelimiters(final byte[] allowedDelimiters) {
		this.allowedDelimiters = allowedDelimiters;
		return this;
	}

	public BasicCSVConstraintsBuilder allowedQuotes(final byte[] allowedQuotes) {
		this.allowedQuotes = allowedQuotes;
		return this;
	}

	public BasicCSVConstraintsBuilder allowedEscapes(final byte[] allowedEscapes) {
		this.allowedEscapes = allowedEscapes;
		return this;
	}

	public BasicCSVConstraintsBuilder minFields(final int minFields) {
		this.minFields = minFields;
		return this;
	}

	public BasicCSVConstraints build() {
		return new BasicCSVConstraints(this.allowedDelimiters, this.allowedQuotes,
				this.allowedEscapes, this.minFields);
	}
}
