package com.github.jferard.pgloaderutils;

import java.util.LinkedList;
import java.util.List;

public class CSVParams {
	private static final int ASCII_CHAR_COUNT = 128;
	private static final int DELIMITER = 1;
	private static final int QUOTE = 2;
	private static final int ESCAPE = 4;
	private final int[] allowedDelimiters;
	private final int[] allowedQuotes;
	private final int minFields;
	private final int[] allowedEscapes;
	private final int[] tab;

	// TODO : builder
	CSVParams(int[] allowedDelimiters, int[] allowedQuotes,
			int[] allowedEscapes, int minFields) {
		this.allowedDelimiters = allowedDelimiters == null ? new int[] { ',', ';', '\t', '|' } : allowedDelimiters;
		this.allowedQuotes = allowedQuotes == null ? new int[] { '"', '\'' } : allowedQuotes;
		this.allowedEscapes = allowedEscapes == null ? new int[] { '\\' } : allowedEscapes;
		this.minFields = minFields;
		this.tab = new int[ASCII_CHAR_COUNT];
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
	
	public int[] getAllowedDelimiters() {
		return this.allowedDelimiters;
	}

	public int[] getAllowedQuotes() {
		return this.allowedQuotes;
	}

	public int[] getAllowedEscapes() {
		return this.allowedEscapes;
	}

	public int getMinFields() {
		return this.minFields;
	}
}
