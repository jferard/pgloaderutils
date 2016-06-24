package com.github.jferard.pgloaderutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Three things to determine : finalDelimiter, quotechar, escapechar
 *
 * Consider the following line : a\|a,"a|a","a|a","a|a"
 *
 * @author Julien Férard
 *
 */
@SuppressWarnings("unused")
public class CSVSniffer implements AsciiSniffer {
	private static final int ASCII_BYTE_COUNT = 128;
	private static final int BONUS_FOR_IRREGULAR_LINES = 5;
	private static final int DEFAULT_LINE_SIZE = 1024;

	private static List<Byte> asNewList(final byte[] array) {
		final List<Byte> l = new LinkedList<Byte>();
		for (final byte i : array)
			l.add(i);
		return l;
	}

	private final CSVConstraints csvParams;
	private byte finalDelimiter;
	private byte finalEscape;
	private byte finalQuote;

	public CSVSniffer(final CSVConstraints csvParams) {
		this.csvParams = csvParams;
	}

	public byte getFinalDelimiter() {
		return this.finalDelimiter;
	}

	public byte getFinalEscape() {
		return this.finalEscape;
	}

	public byte getFinalQuote() {
		return this.finalQuote;
	}

	@Override
	public void sniff(final InputStream inputStream, final int size) throws IOException {
		final StreamParser streamParser = new StreamParser(
				CSVSniffer.DEFAULT_LINE_SIZE);

		final byte[] allowedDelimiters = this.csvParams.getAllowedDelimiters();
		final byte[] allowedQuotes = this.csvParams.getAllowedQuotes();
		final byte[] allowedEscapes = this.csvParams.getAllowedEscapes();

		int c = inputStream.read();
		int i = 0;
		while (c != -1 && i<size) {
			i++;
			streamParser.put((byte) c);
			c = inputStream.read();
		}

		final List<Line> lines = streamParser.getLines();
		final int[][] delimCountByLine = new int[CSVSniffer.ASCII_BYTE_COUNT][lines
				.size()];
		int l = 0;
		for (final Line line : lines) {
			for (final byte delim : allowedDelimiters)
				delimCountByLine[delim][l] = line.getCount(delim);
			l++;
		}

		try {
			this.finalDelimiter = this.computeDelimiter(delimCountByLine,
					allowedDelimiters);
			this.finalQuote = this.computeQuote(lines, allowedQuotes);
			this.finalEscape = this.computeEscape(lines, allowedEscapes);
		} catch (final NoSuchElementException e) {
			throw e;
		}
	}

	private byte computeEscape(final List<Line> lines,
			final byte[] allowedEscapes) {
		final int[] escapes = new int[CSVSniffer.ASCII_BYTE_COUNT];
		List<Byte> keptEscapes = CSVSniffer.asNewList(allowedEscapes);
		keptEscapes.add(this.finalQuote);

		for (final Line line : lines) {
			final List<Part> parts = line.asParts(this.finalDelimiter);
			for (final Part part : parts) {
				part.trim();
				part.trimOne(this.finalQuote);
			}
			for (final Part part : parts) {
				final int c = part.findCharBefore(this.finalQuote);
				if (c >= 0 && keptEscapes.contains(Byte.valueOf((byte) c)))
					escapes[c]++;
			}
		}

		return Collections.max(keptEscapes, new Comparator<Byte>() {

			@Override
			public int compare(final Byte e1, final Byte e2) {
				return escapes[e1] - escapes[e2];
			}
		});
	}

	public void sniff(final String path, final int size) throws IOException {
		final InputStream stream = new FileInputStream(path);
		try {
			this.sniff(stream, size);
		} finally {
			stream.close();
		}
	}

	private byte computeDelimiter(final int[][] delimCountByLine,
			final byte[] allowedDelimiters) {
		final double[] variances = new double[CSVSniffer.ASCII_BYTE_COUNT];
		final int[] roundedMeans = new int[CSVSniffer.ASCII_BYTE_COUNT];
		final List<Byte> keptDelimiters = CSVSniffer
				.asNewList(allowedDelimiters);

		final Iterator<Byte> it = keptDelimiters.iterator();
		while (it.hasNext()) {
			final byte delim = it.next();
			final StatisticsBasic statisticsBasic = new StatisticsBasic(
					delimCountByLine[delim]);
			roundedMeans[delim] = (int) Math.round(statisticsBasic.getMean());
			variances[delim] = statisticsBasic.getVariance();
			if (roundedMeans[delim] < 1 || variances[delim] > 4)
				it.remove();
		}

		return Collections.max(keptDelimiters, new Comparator<Byte>() {
			@Override
			public int compare(final Byte d1, final Byte d2) {
				return (int) Math.signum(variances[d1] - variances[d2]);
			}
		});
	}

	private byte computeQuote(final List<Line> lines,
			final byte[] allowedQuotes) {
		final int[] quotes = new int[CSVSniffer.ASCII_BYTE_COUNT];
		final List<Byte> keptQuotes = CSVSniffer.asNewList(allowedQuotes);

		for (final Line line : lines) {
			final List<Part> parts = line.asParts(this.finalDelimiter);
			for (final Part part : parts) {
				part.trim();
			}
			for (final byte q : keptQuotes) {
				for (final Part part : parts) {
					if (part.hasQuote(q))
						quotes[q]++;
				}
			}
		}

		return Collections.max(keptQuotes, new Comparator<Byte>() {

			@Override
			public int compare(final Byte q1, final Byte q2) {
				return quotes[q1] - quotes[q2];
			}
		});
	}
}