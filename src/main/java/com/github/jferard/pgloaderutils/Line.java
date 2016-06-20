package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class Line {
	private byte[] array;
	private int i;

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

	public int getCount(byte c) {
		int count = 0;
		for (int j = 0; j < this.i; j++) {
			if (this.array[j] == c)
				count++;
		}
		return count;
	}

	public List<Part> asParts(byte delim) {
		LineParser lineParser = new LineParser(this.array, this.i);
		return lineParser.parse(delim);
	}

	/**
	 * 
	 * @param delim
	 * @param quote
	 * @param escape
	 * @return
	 * 
	 */
	public List<Part> asParts(byte delim, byte quote, byte escape) {
		LineParser lineParser = new LineParser(this.array, this.i);
		if (escape == quote)
			return lineParser.parse(delim, quote);
		else
			return lineParser.parse(delim, quote, escape);
	}


	@Override
	public String toString() {
		return new String(this.array, 0, this.i, Charset.forName("ASCII"));
	}
}
