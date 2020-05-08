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

/**
 * The main constraint is that delimiter, quote and escape can't be a letter or
 * a digit. More precisely, char allowed are [0, 2F] u [3A, 40] u (5B, 60] u
 * [7B, 7F]. Only ascii char are allowed.
 */
public class BasicCSVConstraints {
	private static final int ASCII_CHAR_COUNT = 128;
	private static final int DELIMITER = 1;
	private static final int QUOTE = 2;
	private static final int ESCAPE = 4;
	private final byte[] allowedDelimiters;
	private final byte[] allowedQuotes;
	private final int minFields;
	private final byte[] allowedEscapes;
	private final byte[] tab;

	public static BasicCSVConstraintsBuilder builder() {
		return new BasicCSVConstraintsBuilder();
	}

	public static BasicCSVConstraintsBuilder basicBuilder() {
		return new BasicCSVConstraintsBuilder()
				.allowedDelimiters(new byte[] { ',', ';', '\t', '|' })
				.allowedQuotes(new byte[] { '"', '\'' })
				.allowedEscapes(new byte[] { '\\' }).minFields(2);
	}

	BasicCSVConstraints(final byte[] allowedDelimiters, final byte[] allowedQuotes,
						final byte[] allowedEscapes, final int minFields) {
		this.allowedDelimiters = allowedDelimiters;
		this.allowedQuotes = allowedQuotes;
		this.allowedEscapes = allowedEscapes;
		this.minFields = minFields;
		this.tab = new byte[ASCII_CHAR_COUNT];
		for (final int d : this.allowedDelimiters) {
			this.tab[d] |= BasicCSVConstraints.DELIMITER;
		}
		for (final int q : this.allowedQuotes) {
			this.tab[q] |= BasicCSVConstraints.QUOTE;
		}
		for (final int e : this.allowedQuotes) {
			this.tab[e] |= BasicCSVConstraints.ESCAPE;
		}
	}

	public boolean isAllowedDelimiter(final int c) {
		return (this.tab[c] & BasicCSVConstraints.DELIMITER) != 0;
	}

	public boolean isAllowedQuote(final int c) {
		return (this.tab[c] & BasicCSVConstraints.QUOTE) != 0;
	}

	public boolean isAllowedEscape(final int c) {
		return (this.tab[c] & BasicCSVConstraints.ESCAPE) != 0;
	}

	public byte[] getAllowedDelimiters() {
		return this.allowedDelimiters;
	}

	public byte[] getAllowedQuotes() {
		return this.allowedQuotes;
	}

	public byte[] getAllowedEscapes() {
		return this.allowedEscapes;
	}

	public int getMinFields() {
		return this.minFields;
	}
}
