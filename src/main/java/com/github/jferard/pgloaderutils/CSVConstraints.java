package com.github.jferard.pgloaderutils;

/**
 * The main constraint is that delimiter, quote and escape can't be a letter or
 * a digit. More precisely, char allowed are [0, 2F] u [3A, 40] u (5B, 60] u
 * [7B, 7F]. Only ascii char are allowed.
 */
public class CSVConstraints {
	private static final int ASCII_CHAR_COUNT = 128;
	private static final int DELIMITER = 1;
	private static final int QUOTE = 2;
	private static final int ESCAPE = 4;
	private final byte[] allowedDelimiters;
	private final byte[] allowedQuotes;
	private final int minFields;
	private final byte[] allowedEscapes;
	private final byte[] tab;

	public static CSVConstraintsBuilder builder() {
		return new CSVConstraintsBuilder();
	}

	public static CSVConstraintsBuilder basicBuilder() {
		return new CSVConstraintsBuilder()
				.allowedDelimiters(new byte[] { ',', ';', '\t', '|' })
				.allowedQuotes(new byte[] { '"', '\'' })
				.allowedEscapes(new byte[] { '\\' }).minFields(2);
	}

	CSVConstraints(byte[] allowedDelimiters, byte[] allowedQuotes,
			byte[] allowedEscapes, int minFields) {
		this.allowedDelimiters = allowedDelimiters;
		this.allowedQuotes = allowedQuotes;
		this.allowedEscapes = allowedEscapes;
		this.minFields = minFields;
		this.tab = new byte[ASCII_CHAR_COUNT];
		for (int d : this.allowedDelimiters) {
			this.tab[d] |= CSVConstraints.DELIMITER;
		}
		for (int q : this.allowedQuotes) {
			this.tab[q] |= CSVConstraints.QUOTE;
		}
		for (int e : this.allowedQuotes) {
			this.tab[e] |= CSVConstraints.ESCAPE;
		}
	}

	public boolean isAllowedDelimiter(int c) {
		return (this.tab[c] & CSVConstraints.DELIMITER) != 0;
	}

	public boolean isAllowedQuote(int c) {
		return (this.tab[c] & CSVConstraints.QUOTE) != 0;
	}

	public boolean isAllowedEscape(int c) {
		return (this.tab[c] & CSVConstraints.ESCAPE) != 0;
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
