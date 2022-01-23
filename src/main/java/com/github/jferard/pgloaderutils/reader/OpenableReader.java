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
package com.github.jferard.pgloaderutils.reader;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * An OpenableReader is a basic reader that must be opened.
 * All read instructions are blocked until {@code open()} is called.
 * <p>
 * This is useful to write a multi-threaded application:
 * <pre>
 *     thread.start(); // this thread reads data from reader, and closes it
 *     reader.open();  // open the floodgates
 *     thread.join();  // wait for the end of flood
 * </pre>
 */
public abstract class OpenableReader extends Reader {
    private IOException ioException;
    private SQLException sqlException;

    /**
     * Unlock read
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void open() throws IOException;

    /**
     * @param e the exception
     */
    public void setException(final IOException e) {
        this.ioException = e;
    }

    /**
     * @param e the exception
     */
    public void setException(final SQLException e) {
        this.sqlException = e;
    }

    /**
     * @return an exception or null
     */
    public IOException getIOException() {
        return this.ioException;
    }

    /**
     * @return an exception or null
     */
    public SQLException getSQLException() {
        return this.sqlException;
    }
}
