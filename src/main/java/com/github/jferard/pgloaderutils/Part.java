package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;

public class Part {
	public static final int MULTIPLE = -2;
	public static final int NONE = -1;
	private byte[] array;
	private int from;
	private int to;

	public Part(byte[] array, int from, int to) {
		this.array = array;
		this.from = from;
		this.to = to;
	}

	public void trim() {
		while (this.array[this.from] == ' ')
			this.from++;

		while (this.array[this.to - 1] == ' ')
			this.to--;
	}

	public boolean trimOne(byte c) {
		boolean result = this.hasQuote(c);
		if (result) {
			this.from++;
			this.to--;
		}
		return result;
	}

	public boolean hasQuote(byte quote) {
		return this.array[this.from] == quote
				&& this.array[this.to - 1] == quote;
	}

	public byte getFirstChar() {
		return this.array[this.from];
	}

	public byte getLastChar() {
		return this.array[this.to - 1];
	}

	public int findCharBefore(int quote) {
		int c = NONE;
		int j = this.to - 1;
		while (j >= this.from+1) {
			if (this.array[j] == quote) {
				j--;
				if (c == NONE)
					c = this.array[j];
				else if (c != this.array[j])
					return MULTIPLE;
			}
			j--;
		}
		return c;
	}

	@Override
	public String toString() {
		return new String(this.array, this.from, this.to - this.from,
				Charset.forName("ASCII"));
	}
}
