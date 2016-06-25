package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

public class CSVHeaderSniffer implements Sniffer {
	private Charset charset;
	private CSVFormat csvFormat;
	private List<String> header;

	public CSVHeaderSniffer(final byte delimiter, final byte quote,
			final byte escape, final Charset charset) {
		this.charset = charset;
		this.csvFormat = CSVFormat.newFormat((char) delimiter)
				.withQuote((char) quote).withQuoteMode(QuoteMode.MINIMAL)
				.withAllowMissingColumnNames();
		if (escape != quote)
			this.csvFormat = this.csvFormat.withEscape((char) escape);
	}

	@Override
	public void sniff(InputStream inputStream, int size) throws IOException {
		Reader streamReader = new InputStreamReader(inputStream, this.charset);

		CSVParser parser = new CSVParser(streamReader, this.csvFormat);
		Iterator<CSVRecord> iterator = parser.iterator();

		if (iterator.hasNext()) {
			CSVRecord firstRowRecord = iterator.next();
			final int firstRowSize = (int) firstRowRecord.size();

			char[] firstRowSignature = this.getSignature(firstRowRecord,
					firstRowSize);
			if (this.getMaxDigits(firstRowSignature) == 'D')
				return;

			char[] remainingRowsSignature = this
					.getRemainingRowsSignature(iterator, firstRowSize);

			for (int col = 0; col < firstRowSize; col++) {
				if (firstRowSignature[col] == '?'
						&& remainingRowsSignature[col] != '?') {
					this.header = new ArrayList<String>(firstRowSize);
					for (String s : firstRowRecord)
						this.header.add(s);
					return;
				}
			}
		}
	}

	protected char[] getRemainingRowsSignature(Iterator<CSVRecord> iterator,
			int firstRowSize) {
		char[] remainingRowsSignature = new char[firstRowSize];
		int[] digitsInColumn = new int[firstRowSize];

		int rows = 0;
		while (iterator.hasNext()) {
			rows++;
			CSVRecord record = iterator.next();

			char[] rowSignature = this.getSignature(record, firstRowSize);
			for (int col = 0; col < firstRowSize; col++) {
				if (rowSignature[col] != '?')
					digitsInColumn[col]++;
			}
		}
		
		for (int col = 0; col < firstRowSize; col++) {
			if (digitsInColumn[col] > 0.9 * rows)
				remainingRowsSignature[col] = 'd';
			else
				remainingRowsSignature[col] = '?';
		}
		return remainingRowsSignature;
	}

	private char getMaxDigits(char[] firstRowSignature) {
		char d = '?';
		for (char c : firstRowSignature) {
			if (c == 'D')
				return 'D';
			else if (c == 'd')
				d = 'd';
		}
		return d;
	}

	private char[] getSignature(CSVRecord record, int firstRowSize) {
		char[] signature = new char[firstRowSize];
		for (int col = 0; col < firstRowSize; col++) {
			if (col < record.size()) {
				String s = record.get(col);
				signature[col] = this.getType(s);
			} else
				signature[col] = '?';
		}
		return signature;
	}

	/**
	 * @param s
	 * @return 'd' (digit) if more than 80 % of the chars are digits, and there
	 *         is at least one digit.
	 */
	private char getType(String s) {
		int digits = 0;
		int letters = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetter(c))
				letters++;
			else if (Character.isDigit(c))
				digits++;
			// else ignore
		}
		if (digits > 0) {
			if (letters == 0)
				return 'D';
			else if (digits >= 4 * letters)
				return 'd';
		}

		return '?';
	}

	public List<String> getHeader() {
		return this.header;
	}
}
