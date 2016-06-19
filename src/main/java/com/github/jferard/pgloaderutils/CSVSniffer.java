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
	private static final int INT_COUNT = 128;
	private int delimiter;
	private CSVParams csvParams;

	private static List<Integer> asNewList(int[] array) {
		final List<Integer> l = new LinkedList<Integer>();
		for (int i : array)
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
		LineParser lineParser = new LineParser(DEFAULT_LINE_SIZE);

		final int[] allowedDelimiters = this.csvParams.getAllowedDelimiters();
		final int[] allowedQuotes = this.csvParams.getAllowedQuotes();
		final int[] allowedEscapes = this.csvParams.getAllowedEscapes();
		final List<Integer> keptDelimiters = CSVSniffer
				.asNewList(allowedDelimiters);

		int c = inputStream.read();
		while (c != -1) {
			lineParser.put(c);
			c = inputStream.read();
		}

		final List<Line> lines = lineParser.getLines();
		int[][] delimCountByLine = new int[INT_COUNT][lines.size()];
		int l = 0;
		for (Line line : lines) {
			for (int delim : allowedDelimiters)
				delimCountByLine[delim][l] = line.getCount(delim);
			l++;
		}

		final double[] means = new double[INT_COUNT];
		final double[] variances = new double[INT_COUNT];
		for (int delim : allowedDelimiters) {
			StatisticsBasic statisticsBasic = new StatisticsBasic(
					delimCountByLine[delim]);
			means[delim] = statisticsBasic.getMean();
			variances[delim] = statisticsBasic.getVariance();
			if (variances[delim] >= 0.25)
				keptDelimiters.remove(delim);
		}

		Collections.sort(keptDelimiters, new Comparator<Integer>() {
			@Override
			public int compare(Integer d1, Integer d2) {
				if (variances[d1] < variances[d2])
					return 1;
				else if (variances[d1] > variances[d2])
					return -1;
				else
					return 0;
			}
		});

		// consider the regular lines
		for (int delim : keptDelimiters) {
			final List<Integer> keptQuotes = CSVSniffer
					.asNewList(allowedQuotes);
			final Map<Integer, List<Integer>> keptEscapesByQuote = new HashMap<Integer, List<Integer>>();
			for (Integer quote : keptQuotes)
				keptEscapesByQuote.put(quote,
						CSVSniffer.asNewList(allowedEscapes));

			final int[] quotes = new int[INT_COUNT];
			final int[][] escapes = new int[INT_COUNT][INT_COUNT];
			for (Line line : lineParser.getLines()) {
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
			for (Line line : lineParser.getLines()) {
				int count = line.getCount(delim);
				if (count > means[delim]) {
					for (Integer quote : keptQuotes) {
						for (Integer escape : keptEscapesByQuote.get(quote)) {
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
				final Integer quote = Collections.max(keptQuotes,
						new Comparator<Integer>() {

							@Override
							public int compare(Integer q1, Integer q2) {
								return quotes[q2] - quotes[q1];
							}
						});
				List<Integer> keptEscapes = keptEscapesByQuote.get(quote);
				if (keptEscapes.size() > 0) {
					Integer escape = Collections.max(keptQuotes,
							new Comparator<Integer>() {

								@Override
								public int compare(Integer e1, Integer e2) {
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