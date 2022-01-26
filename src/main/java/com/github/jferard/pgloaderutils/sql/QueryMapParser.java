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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QueryMapParser {
    private static final Logger logger = Logger.getLogger(QueryMapParser.class.getName());

    public static Map<String, String> parse(final Reader reader) throws IOException {
        return new QueryMapParser(reader).parseQueries();
    }

    private final BufferedReader br;
    private final Map<String, String> queryMap;
    private List<String> curQueryLines;
    private String curKey;
    private int state;

    QueryMapParser(final Reader reader) {
        this.br = Util.ensureBuffered(reader);
        this.curQueryLines = new ArrayList<>();
        this.queryMap = new HashMap<>();
        this.curKey = null;
        this.state = -1;
    }

    public Map<String, String> parseQueries() throws IOException {
        String line = this.br.readLine();
        int lineNumber = 1;
        while (line != null) {
            final int i = this.quotesCount(line);
            if (this.state == -1) {
                if (i > 0) {
                    this.state = i;
                    this.curKey = line.substring(i).trim();
                } else if (!line.isEmpty()) {
                    QueryMapParser.logger.warning(
                            String.format("Ignore garbage at line %d: %s", lineNumber,
                                    line));
                }
            } else {
                if (i > 0) {
                    if (i == this.state) {
                        final String garbage = line.substring(i);
                        if (!garbage.isEmpty()) {
                            QueryMapParser.logger.warning(
                                    String.format("Ignore garbage at line %d: %s", lineNumber,
                                            garbage));
                        }
                        this.flushQuery();
                    } else {
                        if (i > 1) {
                            QueryMapParser.logger.warning(
                                    String.format("Query not closed at line %d: %d quotes",
                                            lineNumber, i));
                        }
                        this.curQueryLines.add(line);
                    }
                } else {
                    this.curQueryLines.add(line);
                }
            }
            line = this.br.readLine();
            lineNumber++;
        }
        if (this.curQueryLines.stream().anyMatch(l -> !l.isEmpty())) {
            QueryMapParser.logger.warning(
                    String.format("Missing closing quotes at line %s",
                            lineNumber));

            this.flushQuery();
        }
        return this.queryMap;
    }

    private void flushQuery() {
        String query = String.join("\n", this.curQueryLines);
        while (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        this.queryMap.put(this.curKey, query);
        this.curQueryLines = new ArrayList<>();
        this.curKey = null;
        this.state = -1;
    }

    private int quotesCount(final String line) {
        int i = 0;
        if (!line.isEmpty()) {
            if (line.charAt(0) == '"') {
                i = 1;
                while (i < line.length() && line.charAt(i) == '"') {
                    i++;
                }
            }
        }
        return i;
    }

}
