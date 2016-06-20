package com.github.jferard.pgloaderutils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Julien
 * @see http://julien-blanc.developpez.com/tutoriels/c++/lecture-csv-c++/
 *
 */
public class LineParser {
	private final byte[] array;
	private final int i;
	/*
	private String line;
	private List<Integer> reducedLine;
	private Map<Integer, Integer> countByDelimiter;
	private Set<Integer> possibleDelimiters;
	*/

	private static final int ESCAPE_IN_QUOTED_RECORD = 4;
	private static final int WAIT_DELIMITER = 7;
	private static final int QUOTED_RECORD = 2;
	private static final int RECORD = 3;
	private static final int WAIT_RECORD = 8;
	private static final int ERROR = -1;

	public LineParser(byte[] array, int i) {
		this.array = array;
		this.i = i;
	}

	public List<Part> parse(byte delim, byte quote, byte escape) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int to = 0;
		int state = WAIT_RECORD;

		for (int j = 0; j < this.i; j++) {
			byte c = this.array[j];
			switch (state) {
			case WAIT_RECORD:
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					from = j + 1;
					to = from;
				} else {
					if (c == quote) {
						from = j + 1;
						state = QUOTED_RECORD;
					} else if (c != ' ') {
						from = j;
						state = RECORD;
					}
				}
				to = from;
				break;
			case RECORD: // from is set
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = WAIT_RECORD;
					from = j + 1;
					to = from;
				} else
					to = j + 1;
				break;
			case QUOTED_RECORD: // from is set
				if (c == escape)// escape with \
					state = ESCAPE_IN_QUOTED_RECORD;
				else if (c == quote)
					state = WAIT_DELIMITER;
				else
					to = j + 1;
				break;
			case ESCAPE_IN_QUOTED_RECORD:
				// ignore c
				state = QUOTED_RECORD;
				break;
			case WAIT_DELIMITER: // from, to are set
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = WAIT_RECORD;
					from = j + 1;
					to = from;
				} else if (c != ' ')
					state = ERROR;
				break;
			case ERROR:
			default:
				return parts;
			}
		}
		parts.add(new Part(this.array, from, to));
		return parts;
	}

	public List<Part> parse(byte delim, byte escapeAndQuote) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int to = 0;
		int state = WAIT_RECORD;

		for (int j = 0; j < this.i; j++) {
			byte c = this.array[j];
			switch (state) {
			case WAIT_RECORD:
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					from = j + 1;
					to = from;
				} else {
					if (c == escapeAndQuote) {
						from = j + 1;
						state = QUOTED_RECORD;
					} else if (c != ' ') {
						from = j;
						state = RECORD;
					}
				}
				to = from;
				break;
			case RECORD: // from is set
				if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = WAIT_RECORD;
					from = j + 1;
					to = from;
				} else
					to = j + 1;
				break;
			case QUOTED_RECORD: // from is set
				if (c == escapeAndQuote)
					state = WAIT_DELIMITER;
				else
					to = j + 1;
				break;
			case WAIT_DELIMITER: // from, to are set
				if (c == escapeAndQuote) {
					state = QUOTED_RECORD; // ignore
				} else if (c == delim) {
					parts.add(new Part(this.array, from, to));
					state = WAIT_RECORD;
					from = j + 1;
					to = from;
				} else if (c != ' ')
					state = ERROR;
				break;
			case ERROR:
			default:
				return parts;
			}
		}
		parts.add(new Part(this.array, from, to));
		return parts;
	}

	public List<Part> parse(byte delim) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		for (int j = 0; j < this.i; j++) {
			if (this.array[j] == delim) {
				parts.add(new Part(this.array, from, j));
				from = j + 1;
			}
		}
		parts.add(new Part(this.array, from, this.i));
		return parts;
	}
}
