package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class Line {
	private byte[] array;
	private int i;

	private PartState state;

	public Line(int size) {
		this.array = new byte[size];
		this.i = 0;
	}

	public int getSize() {
		return this.i;
	}

	public void append(byte c) {
		assert c >= 0;
		this.array[this.i++] = c;
		this.checkArrayLength();
	}

	protected void checkArrayLength() {
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
		this.array[this.i] = -1;
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
		this.array[this.i] = -1;
		if (escape == quote)
			return this.asPartsWithEscapeIsQuote(delim, quote);
		else
			return this.asPartsWithEscapeIsNotQuote(delim, quote, escape);
	}

	private List<Part> asPartsWithEscapeIsNotQuote(int delim, int quote,
			int escape) {
		LineParser lineParser = new LineParser(this.array, this.i);
		lineParser.parse(delim, quote,
			escape);
	}
		
		
		for (int j=0 ; j<this.i ; j++) {
			byte c = this.array[j];
			this.state.handle(c);
		}
		
		
		}
		return parts;
	}

	private void getOpt(char c) {
		if (this.get() != c)
			this.unget();
	}

	private int get() {
		return this.array[this.j++];
	}

	private void unget() {
		this.j--;
	}
	
	private void start() {
		this.j = 0;
	}

	private List<Part> asPartsWithEscapeIsQuote(int delim, int escapeAndQuote) {
		List<Part> parts = new LinkedList<Part>();
		int from = 0;
		int to = 0;
		int state = INITIAL;

		for (int j = 1; j < this.i; j++) {
			int c = this.array[j];
			char a = (char) c;
			switch (state) {
			case INITIAL:
				if (c == ' ' && j + 1 < this.i
						&& this.array[j + 1] == escapeAndQuote) {
					// ignore char
				} else if (c == delim)
					parts.add(new Part(this.array, from, to));
				else if (c == escapeAndQuote) {
					state = QUOTED_RECORD;
					from = j + 1;
				} else {
					state = RECORD;
					from = j + 1;
				}
				break;
			case BETWEEN_RECORDS:
				if (c == ' ') {
					// do nothing
				} else if (c == delim)
					parts.add(new Part(this.array, from, to));
				else if (c == escapeAndQuote) {
					state = QUOTED_RECORD;
					from = j + 1;
				} else {
					state = RECORD;
					from = j + 1;
				}
				break;
			case RECORD: // from is set
				if (c == delim) {
					parts.add(new Part(this.array, from, j - 1));
					state = BETWEEN_RECORDS;
					from = j + 1;
					to = j + 1;
				}
				break;
			case QUOTED_RECORD: // from is set
				if (c == escapeAndQuote) {
					state = QUOTE_IN_QUOTED_RECORD;
					to = j;
				}
				break;
			case QUOTE_IN_QUOTED_RECORD: // from, to are set
				if (c == escapeAndQuote) {
					state = QUOTED_RECORD;
				} else {
					state = BETWEEN_RECORDS;
					j--;
				}
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

	public void setState(PartState newState) {
		this.state = newState;
	}
}
