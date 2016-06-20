package com.github.jferard.pgloaderutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Three things to determine : delimiter, quotechar, escapechar
 * 
 * Consider the following line : a\|a,"a|a","a|a","a|a"
 * 
 * @author Julien Férard
 *
 */
@SuppressWarnings("unused")
public class CSVSniffer implements AsciiSniffer {
	private static final int DEFAULT_LINE_SIZE = 1024;
	private static final int ASCII_BYTE_COUNT = 128;
	private int delimiter;
	private CSVParams csvParams;

	private static List<Byte> asNewList(byte[] array) {
		final List<Byte> l = new LinkedList<Byte>();
		for (byte i : array)
			l.add(i);
		return l;
	}

	public CSVSniffer(CSVParams csvParams) {
		this.csvParams = csvParams;
	}

	public void sniff(String path) throws IOException {
		InputStream stream = new FileInputStream(path);
		try {
			this.sniff(stream);
		} finally {
			stream.close();
		}
	}

	@Override
	public void sniff(final InputStream inputStream) throws IOException {
		StreamParser streamParser = new StreamParser(DEFAULT_LINE_SIZE);

		final byte[] allowedDelimiters = this.csvParams.getAllowedDelimiters();
		final byte[] allowedQuotes = this.csvParams.getAllowedQuotes();
		final byte[] allowedEscapes = this.csvParams.getAllowedEscapes();
		final List<Byte> keptDelimiters = CSVSniffer
				.asNewList(allowedDelimiters);

		int c = inputStream.read();
		while (c != -1) {
			streamParser.put((byte) c);
			c = inputStream.read();
		}

		final List<Line> lines = streamParser.getLines();
		int[][] delimCountByLine = new int[ASCII_BYTE_COUNT][lines.size()];
		int l = 0;
		for (Line line : lines) {
			for (byte delim : allowedDelimiters)
				delimCountByLine[delim][l] = line.getCount(delim);
			l++;
		}

		final double[] means = new double[ASCII_BYTE_COUNT];
		final double[] variances = new double[ASCII_BYTE_COUNT];
		for (int delim : allowedDelimiters) {
			StatisticsBasic statisticsBasic = new StatisticsBasic(
					delimCountByLine[delim]);
			means[delim] = statisticsBasic.getMean();
			variances[delim] = statisticsBasic.getVariance();
			if (variances[delim] >= 0.25)
				keptDelimiters.remove(delim);
		}

		Collections.sort(keptDelimiters, new Comparator<Byte>() {
			@Override
			public int compare(Byte d1, Byte d2) {
				if (variances[d1] < variances[d2])
					return 1;
				else if (variances[d1] > variances[d2])
					return -1;
				else
					return 0;
			}
		});

		// consider the regular lines
		for (byte delim : keptDelimiters) {
			final List<Byte> keptQuotes = CSVSniffer
					.asNewList(allowedQuotes);
			final Map<Byte, List<Byte>> keptEscapesByQuote = new HashMap<Byte, List<Byte>>();
			for (Byte quote : keptQuotes)
				keptEscapesByQuote.put(quote,
						CSVSniffer.asNewList(allowedEscapes));

			final int[] quotes = new int[ASCII_BYTE_COUNT];
			final int[][] escapes = new int[ASCII_BYTE_COUNT][ASCII_BYTE_COUNT];
			for (Line line : streamParser.getLines()) {
				int count = line.getCount(delim);
				if (count == means[delim]) {
					List<Part> parts = line.asParts(delim);
					for (Part part : parts) {
						part.trim();
						final int firstChar = part.getFirstChar();
						if (this.csvParams.isAllowedQuote(firstChar)) {
							if (part.getLastChar() != firstChar)
								keptQuotes.remove(firstChar);
							else {
								quotes[firstChar]++;
								int charBeforeQuote = part
										.findCharBefore(firstChar);
								if (charBeforeQuote == -2)
									keptQuotes.remove(firstChar);
								else if (charBeforeQuote != -1 && this.csvParams
										.isAllowedEscape(charBeforeQuote))
									escapes[firstChar][charBeforeQuote]++;
							}
						}
					}
				}
			}
			if (keptQuotes.isEmpty())
				continue;

			// consider the irregular lines
			for (Line line : streamParser.getLines()) {
				int count = line.getCount(delim);
				if (count > means[delim]) {
					for (Byte quote : keptQuotes) {
						for (Byte escape : keptEscapesByQuote.get(quote)) {
							List<Part> parts = line.asParts(delim, quote,
									escape);
							if (parts.size() == means[delim]) {
								quotes[quote]++;
								escapes[quote][escape]++;
							} else
								keptEscapesByQuote.get(quote).remove(escape);
						}
					}
				}
			}

			if (keptQuotes.size() > 0) {
				final Byte quote = Collections.max(keptQuotes,
						new Comparator<Byte>() {

							@Override
							public int compare(Byte q1, Byte q2) {
								return quotes[q2] - quotes[q1];
							}
						});
				List<Byte> keptEscapes = keptEscapesByQuote.get(quote);
				if (keptEscapes.size() > 0) {
					Byte escape = Collections.max(keptQuotes,
							new Comparator<Byte>() {

								@Override
								public int compare(Byte e1, Byte e2) {
									return escapes[quote][e2]
											- escapes[quote][e1];
								}
							});

					break;
				}
			}
		}
	}
}