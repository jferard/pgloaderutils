package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;

public class Part {

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
		
		while (this.array[this.to-1] == ' ')
			this.to--;
	}
	
	public byte getFirstChar() {
		return this.array[this.from];
	}

	public byte getLastChar() {
		return this.array[this.to-1];
	}

	public int findCharBefore(int quote) {
		int c = -1;
		int j = this.array.length-1;
		while (j >= 0) {
			if (this.array[j] == quote) {
				j--;
				if (c == -1)
					c = this.array[j];
				else if (c != this.array[j])
					return -2;
			}
			j--;
		}
		return c;
	}
	
	@Override
	public String toString() {
		return new String(this.array, this.from, this.to-this.from, Charset.forName("ASCII"));
	}
}
