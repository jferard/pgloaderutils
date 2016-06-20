package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class Line {
	private static final int INITIAL = 0;
	/*
	private String line;
	private List<Integer> reducedLine;
	private Map<Integer, Integer> countByDelimiter;
	private Set<Integer> possibleDelimiters;
	*/

	private static final int BETWEEN_RECORDS = 1;
	private static final int QUOTED_RECORD = 2;
	private static final int RECORD = 3;

	private static final int ESCAPE_IN_QUOTED_RECORD = 4;

	private static final int CLOSED_QUOTED_RECORD = 5;
	private static final int QUOTE_IN_QUOTED_RECORD = 6;

	private byte[] array;
	private int i;

	public Line(int size) {
		this.array = new byte[size];
		this.i = 0;
	}

	/*
	LineInfo(final String line) {
		this.line = line;
	}
	
	public Set<Integer> process(Set<Integer> allowedDelimiters, int minFields)
		this.createReducedLine();
		return this.checkDelimiter(allowedDelimiters, minFields);
	}
	
	/**
	 * Adds a reduced line, without aany letter or digit.
	 * 
	 * @param line
	 *            The line read in the csv file
	 *
	private void createReducedLine() {
		this.reducedLine = new ArrayList<Integer>();
		for (int j = 0; j < this.line.length(); j++) {
			int c = this.line.codePointAt(j);
			if (!Character.isAlphabetic(c)) {
				this.reducedLine.add(c);
			}
		}
	}
	
	private Set<Integer> checkDelimiter(Set<Integer> allowedDelimiters,
			int minFields) {
		this.countByDelimiter = new HashMap<Integer, Integer>();
		for (Integer c : this.reducedLine) {
			if (allowedDelimiters.contains(c)) {
				Integer count = this.countByDelimiter.get(c);
				this.countByDelimiter.put(c, count == null ? 1 : count + 1);
			}
		}
		Iterator<Entry<Integer, Integer>> iterator = this.countByDelimiter
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> entry = iterator.next();
			if (entry.getValue() < minFields)
				allowedDelimiters.remove(entry.getKey());
		}
		return allowedDelimiters;
	}
	
	public Map<Integer, Integer> getCountByDelimiter() {
		return this.countByDelimiter;
	}
	
	public String getLine() {
		return this.line;
	}*/

	public int getSize() {
		return this.i;
	}

	public void append(byte c) {
		this.array[this.i++] = c;
		if (this.i >= this.array.length) {
			byte[] newArray = new byte[this.array.length * 2];
			System.arraycopy(this.array, 0, newArray, 0, this.i);
			this.array = newArray;
		}
	}

	public int getCount(int c) {
		int count = 0;
		for (int j = 0; j < this.i; j++) {
			if (this.array[j] == c)
				count++;
		}
		return count;
	}

	public List<Part> asParts(int delim) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int j;
		for (j = 0; j < this.i; j++) {
			if (this.array[j] == delim) {
				parts.add(new Part(this.array, from, j));
				from = j + 1;
			}
		}
		parts.add(new Part(this.array, from, this.i));
		return parts;
	}

	/**
	 * @see http://julien-blanc.developpez.com/tutoriels/c++/lecture-csv-c++/
	 * 
	 * @param delim
	 * @param quote
	 * @param escape
	 * @return
	 * 
	 */
	public List<Part> asParts(int delim, int quote, int escape) {
		if (escape == quote)
			return this.asPartsWithEscapeIsQuote(delim, quote);
		else
			return this.asPartsWithEscapeIsNotQuote(delim, quote, escape);
	}
	
	private List<Part> asPartsWithEscapeIsNotQuote(int delim, int quote, int escape) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int to = 0;
		int state = BETWEEN_RECORDS;
		for (int j = 0; j < this.i; j++) {
			int c = this.array[j];
			switch (state) {
			case BETWEEN_RECORDS:
				if (c == delim)
					parts.add(new Part(this.array, from, to));
				else if (c == quote) {
					state = QUOTED_RECORD;
					from = j+1;
				} else {
					state = RECORD;
					from = j+1;
				}
				break;
			case RECORD: // from is set
				if (c == delim) {
					parts.add(new Part(this.array, from, j-1));
					state = BETWEEN_RECORDS;
					from = j+1;
					to = j+1;
				}
				break;
			case QUOTED_RECORD: // from is set
				if (c == escape)// escape with ""
					state = ESCAPE_IN_QUOTED_RECORD;
				else if (c == quote) {
					state = CLOSED_QUOTED_RECORD;
					to = j;
				}
				break;
			case ESCAPE_IN_QUOTED_RECORD:
				// ignore c
				state = QUOTED_RECORD;
				break;
			case CLOSED_QUOTED_RECORD: // from, to are set
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = BETWEEN_RECORDS;
				}
				break;
			default:
				// error
				break;
			}
		}
		return parts;
	}
	
	private List<Part> asPartsWithEscapeIsQuote(int delim, int escapeAndQuote) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int to = 0;
		int state = BETWEEN_RECORDS;
		for (int j = 0; j < this.i; j++) {
			int c = this.array[j];
			switch (state) {
			case BETWEEN_RECORDS:
				if (c == delim)
					parts.add(new Part(this.array, from, to));
				else if (c == escapeAndQuote) {
					state = QUOTED_RECORD;
					from = j+1;
				} else {
					state = RECORD;
					from = j+1;
				}
				break;
			case RECORD: // from is set
				if (c == delim) {
					parts.add(new Part(this.array, from, j-1));
					state = BETWEEN_RECORDS;
					from = j+1;
					to = j+1;
				}
				break;
			case QUOTED_RECORD: // from is set
				if (c == escapeAndQuote) {
					state = QUOTE_IN_QUOTED_RECORD;
					to = j;
				}
				break;
			case QUOTE_IN_QUOTED_RECORD: // from, to are set
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = BETWEEN_RECORDS;
				} else
					state = QUOTED_RECORD;
				break;
			default:
				// error
				break;
			}
		}
		return parts;
	}
	
	@Override
	public String toString() {
		return new String(this.array, 0, this.i, Charset.forName("ASCII"));
	}
}
