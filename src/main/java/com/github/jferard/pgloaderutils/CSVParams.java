package com.github.jferard.pgloaderutils;

import java.util.LinkedList;
import java.util.List;

public class CSVParams {
	private static final int ASCII_CHAR_COUNT = 128;
	private static final int DELIMITER = 1;
	private static final int QUOTE = 2;
	private static final int ESCAPE = 4;
	private final byte[] allowedDelimiters;
	private final byte[] allowedQuotes;
	private final int minFields;
	private final byte[] allowedEscapes;
	private final byte[] tab;

	// TODO : builder
	CSVParams(byte[] allowedDelimiters, byte[] allowedQuotes,
			byte[] allowedEscapes, int minFields) {
		this.allowedDelimiters = allowedDelimiters == null ? new byte[] { ',', ';', '\t', '|' } : allowedDelimiters;
		this.allowedQuotes = allowedQuotes == null ? new byte[] { '"', '\'' } : allowedQuotes;
		this.allowedEscapes = allowedEscapes == null ? new byte[] { '\\' } : allowedEscapes;
		this.minFields = minFields;
		this.tab = new byte[ASCII_CHAR_COUNT];
		for (int d : this.allowedDelimiters) {
			this.tab[d] |= CSVParams.DELIMITER;
		}
		for (int q : this.allowedQuotes) {
			this.tab[q] |= CSVParams.QUOTE;
		}
		for (int e : this.allowedQuotes) {
			this.tab[e] |= CSVParams.ESCAPE;
		}
	}

	public boolean isAllowedDelimiter(int c) {
		return (this.tab[c] & CSVParams.DELIMITER) != 0;
	}

	public boolean isAllowedQuote(int c) {
		return (this.tab[c] & CSVParams.QUOTE) != 0;
	}

	public boolean isAllowedEscape(int c) {
		return (this.tab[c] & CSVParams.ESCAPE) != 0;
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
