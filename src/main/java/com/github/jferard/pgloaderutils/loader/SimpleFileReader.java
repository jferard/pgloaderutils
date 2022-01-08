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
package com.github.jferard.pgloaderutils.loader;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.logging.Logger;

/**
 * Normalized File Reader. It's an OpenableReader that writes a log entry every 16 MB.
 *
 * @author Julien Férard
 */
public class SimpleFileReader extends OpenableReader {
    private static final int ONE_KB;
    private static final int BUFFER_SIZE;
    private static final int ONE_MB;

    static {
        ONE_KB = 1024;
        BUFFER_SIZE = 4 * ONE_KB;
        ONE_MB = ONE_KB * ONE_KB;
    }

    private final Reader modifiedStreamReader;
    private final Reader reader;
    private final Logger logger;
    private final int mbStep;
    private final PipedWriter pipedWriter;

    /**
     * Create a new openable reader
     *
     * @param reader the wrapped reader
     * @param logger a logger
     * @param mbStep the step for log entries
     * @throws IOException if an I/O error occurs
     */
    public SimpleFileReader(final Reader reader, final Logger logger, final int mbStep) throws IOException {
        this.reader = reader;
        this.logger = logger;
        this.mbStep = mbStep;
        this.pipedWriter = new PipedWriter();
        this.modifiedStreamReader = new PipedReader(this.pipedWriter, BUFFER_SIZE);
    }

    @Override
    public void open() throws IOException {
        int i = 0;
        int c = this.reader.read();
        while (c != -1) {
            this.pipedWriter.write(c);
            c = this.reader.read();
            i++;
            if (i % this.mbStep * ONE_MB == 0) {
                this.logger.fine("MBytes read: " + i / ONE_MB);
            }
        }
        this.reader.close();
        this.pipedWriter.close();
    }

    @Override
    public void close() throws IOException {
        this.modifiedStreamReader.close();
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return this.modifiedStreamReader.read(cbuf, off, len);
    }

    @Override
    public int read() throws IOException {
        return this.modifiedStreamReader.read();
    }

}
