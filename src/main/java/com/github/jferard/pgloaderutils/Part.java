package com.github.jferard.pgloaderutils;

import java.util.List;

public class Part {

	private int[] array;
	private int from;
	private int to;

	public Part(int[] array, int from, int to) {
		this.array = array;
		this.from = from;
		this.to = to;
	}

	public void trim() {
		while (this.array[this.from] == ' ')
			this.from++;
		
		while (this.array[this.to] == ' ')
			this.to--;
	}
	
	public int getFirstChar() {
		return this.array[this.from];
	}

	public int getLastChar() {
		return this.array[this.to];
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
}
