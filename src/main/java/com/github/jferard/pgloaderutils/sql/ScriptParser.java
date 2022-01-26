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

package com.github.jferard.pgloaderutils.sql;

import com.github.jferard.pgloaderutils.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a script having queries separated by a newline
 *
 * @author Julien Férard
 */
public class ScriptParser {
    public static List<String> parse(final Reader reader) throws IOException {
        return new ScriptParser(reader).parseScript();
    }

    private final BufferedReader br;
    private List<String> queries;
    private List<String> curQueryLines;

    ScriptParser(final Reader reader) {
        this.br = Util.ensureBuffered(reader);
    }

    public List<String> parseScript() throws IOException {
        this.queries = new ArrayList<>();
        this.curQueryLines = new ArrayList<>();
        for (String line = this.br.readLine(); line != null; line = this.br.readLine()) {
            if (this.isCommentLine(line)) {
                continue;
            }

            if (this.isBlankLine(line) && this.curQueryLines.size() > 0) {
                this.flushQuery();
            } else {
                this.curQueryLines.add(line);
            }
        }
        if (this.curQueryLines.size() > 0) {
            this.flushQuery();
        }
        return this.queries;
    }

    private void flushQuery() {
        String query = String.join("\n", this.curQueryLines).trim();
        while (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        this.curQueryLines = new ArrayList<>();
        this.queries.add(query);
    }

    private boolean isBlankLine(final String line) {
        final String trimmed = line.trim();
        return trimmed.isEmpty();
    }

    private boolean isCommentLine(final String line) {
        final String trimmed = line.trim();
        return trimmed.startsWith("--");
    }
}
