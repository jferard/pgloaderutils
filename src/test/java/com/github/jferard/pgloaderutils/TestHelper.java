/*
 * Some utilities for loading csv data into a PostgreSQL database:
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

package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TestHelper {
    public static final int BUFFER_SIZE = 8 * 1024;

    public static String readAll(final Reader reader) throws IOException {
        final char[] buffer = new char[BUFFER_SIZE];
        final StringBuilder ret = new StringBuilder();
        int count = reader.read(buffer, 0, buffer.length);
        while (count != -1) {
            ret.append(buffer, 0, count);
            count = reader.read(buffer, 0, buffer.length);
        }
        return ret.toString();
    }

    public static RecordsStore configLog(final Class<?> clazz) {
        return configLog(clazz.getName());
    }

    public static RecordsStore configLog(final String name) {
        final Logger logger = Logger.getLogger(name);
        final TestLogHandlerHandler handler = new TestLogHandlerHandler();
        handler.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        return handler;
    }
}

