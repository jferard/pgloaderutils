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

package com.github.jferard.pgloaderutils.sniffer.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A ByteArray matcher is a tool that compares a set of byte arrays to the bytes read from a stream.
 * It leaves the set unmodified, but consumes bytes from the stream. One should mark a stream before
 * passing it to a ByteArraysMatcher.
 *
 * @author Julien Férard
 */
public class ByteArraysMatcher {
    private final Set<byte[]> remainingByteArrays;
    private final InputStream is;
    private int max;
    private byte[] byteArray;

    public ByteArraysMatcher(final Set<byte[]> byteArrays, final InputStream is) {
        if (byteArrays.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.remainingByteArrays = new HashSet<byte[]>(byteArrays); // we need a copy
        max = getRemainingByteArraysMaxLength();
        if (max == 0) {
            throw new IllegalArgumentException();
        }

        this.is = is;

    }

    private int getRemainingByteArraysMaxLength() {
        int len = 0;
        for (final byte[] byteArray : this.remainingByteArrays) {
            if (byteArray.length > len) {
                len = byteArray.length;
            }
        }
        return len;
    }

    /**
     * @return null if no match found
     * @throws IOException
     */
    public byte[] shortestMatch() throws IOException {
        this.byteArray = null;
        for (int i = 0; i < max; i++) {
            final int c = is.read();
            if (c == -1) {
                break;
            }

            this.filterByteArraysOnNthByte(i, (byte) c);
            this.checkByteArraysMatch(i);
            if (this.noByteArrayOrByteArrayFound()) {
                break;
            }
        }
        return this.byteArray;
    }

    /**
     * @return null if no match found
     * @throws IOException
     */
    public byte[] longestMatch() throws IOException {
        this.byteArray = null;
        for (int i = 0; i < max; i++) {
            final int c = is.read();
            if (c == -1) {
                break;
            }

            this.filterByteArraysOnNthByte(i, (byte) c);
            this.checkByteArraysMatch(i);
            if (this.noByteArray()) {
                break;
            }
        }
        return this.byteArray;
    }

    private void filterByteArraysOnNthByte(final int i, final byte c) {
        final Iterator<byte[]> it = remainingByteArrays.iterator();
        while (it.hasNext()) {
            final byte[] bytes = it.next();
            if (i >= bytes.length || bytes[i] != c) {
                it.remove();
            }
        }
    }

    private void checkByteArraysMatch(final int i) {
        for (final byte[] byteArray : this.remainingByteArrays) {
            if (i == byteArray.length - 1) { // ok jusqu'au dernier byte du ByteArray
                this.byteArray = byteArray;
            }
        }
    }

    private boolean noByteArrayOrByteArrayFound() {
        return remainingByteArrays.size() == 0 || this.byteArray != null;
    }

    private boolean noByteArray() {
        return remainingByteArrays.size() == 0;
    }

    public void mark(final InputStream is) {
        assert is.markSupported();
        is.mark(this.getRemainingByteArraysMaxLength());
    }
}