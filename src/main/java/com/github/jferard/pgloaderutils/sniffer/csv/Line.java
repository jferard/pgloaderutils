/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018, 2020-2022 J. Férard <https://github.com/jferard>
 *
 * This file is part of pgLoader Utils.
 *
 * pgLoader Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pgLoader Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.jferard.pgloaderutils.sniffer.csv;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

class Line {
	private byte[] array;
	private int size;

	public Line(final int maxSize) {
		this.array = new byte[maxSize];
		this.size = 0;
	}

	public Line(final byte[] bytes, final int maxSize) {
		this.array = new byte[maxSize];
		System.arraycopy(bytes, 0, this.array, 0, bytes.length);
		this.size = bytes.length;
	}

	public int getSize() {
		return this.size;
	}

	public void append(final byte c) {
		this.array[this.size++] = c;
		this.checkArrayLength();
	}

	protected void checkArrayLength() {
		if (this.size >= this.array.length) {
			final byte[] newArray = new byte[this.array.length * 2];
			System.arraycopy(this.array, 0, newArray, 0, this.size);
			this.array = newArray;
		}
	}

	public int getCount(final byte c) {
		int count = 0;
		for (int j = 0; j < this.size; j++) {
			if (this.array[j] == c) {
                count++;
            }
		}
		return count;
	}

	public List<Part> asParts(final byte delim) {
		final List<Part> parts = new LinkedList<Part>();
		int from = 0;
		for (int j = 0; j < this.size; j++) {
			if (this.array[j] == delim) {
				if (j  != from) {
                    parts.add(new Part(this.array, from, j));
                }
				from = j + 1;
			}
		}
		if (this.size > from) {
            parts.add(new Part(this.array, from, this.size));
        }
		return parts;
	}

	@Override
	public String toString() {
		return new String(this.array, 0, this.size, Charset.forName("ASCII"));
	}

	public byte[] toArray() {
		return this.array;
	}
}
