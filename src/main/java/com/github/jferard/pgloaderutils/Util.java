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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final Set<String> RESERVED_KEYWORDS =
            Stream.of("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC",
                            "AUTHORIZATION", "BINARY", "BOTH", "CASE", "CAST", "CHECK", "COLLATE",
                            "COLLATION", "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS",
                            "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_SCHEMA",
                            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE",
                            "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR",
                            "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE", "IN",
                            "INITIALLY", "INNER", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "LATERAL",
                            "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATURAL",
                            "NOT", "NOTNULL", "NULL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUTER",
                            "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT",
                            "SESSION_USER", "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "TABLESAMPLE", "THEN",
                            "TO", "TRAILING", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VARIADIC",
                            "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH")
                    .collect(Collectors.toCollection(HashSet::new));
    private final static Pattern p = Pattern
            .compile("\\p{InCombiningDiacriticalMarks}+");

    public static String normalize(final String s) {
        final String decomposed = java.text.Normalizer.normalize(s.trim(),
                Normalizer.Form.NFD);
        return p.matcher(decomposed) // $NON-NLS-1$
                .replaceAll("").trim();
    }

    public static String join(final List<String> chunks, final String delimiter) {
        switch (chunks.size()) {
            case 0:
                return "";
            case 1:
                return chunks.get(0);
            default:
                final StringBuilder sb = new StringBuilder();
                try {
                    Util.joinInto(sb, chunks, delimiter);
                } catch (final IOException e) {
                    throw new AssertionError(e); // this should not happen
                }
                return sb.toString();

        }
    }

    public static void joinInto(final Appendable appendable, final List<String> chunks,
                                final String delimiter)
            throws IOException {
        switch (chunks.size()) {
            case 0:
                break;
            case 1:
                appendable.append(chunks.get(0));
                break;
            default:
                final Iterator<String> it = chunks.iterator();
                appendable.append(it.next());
                while (it.hasNext()) {
                    appendable.append(delimiter).append(it.next());
                }
                break;
        }
    }

    // https://www.postgresql.org/docs/current/sql-syntax-lexical.html
    // https://www.postgresql.org/docs/current/sql-keywords-appendix.html
    public static String pgEscapeIdentifier(final String identifier) {
        if (isValidIdentifier(identifier)) {
            return identifier.toLowerCase(Locale.ROOT);
        } else {
            return String.format("\"%s\"", identifier.replaceAll("\"", "\"\""));
        }
    }

    private static boolean isValidIdentifier(final String identifier) {
        return Character.isJavaIdentifierStart(identifier.charAt(0))
                && identifier.chars().skip(1).allMatch(Character::isJavaIdentifierPart)
                && !RESERVED_KEYWORDS.contains(identifier.toUpperCase(Locale.ROOT));
    }

    public static String pgEscapeString(final String str) {
        if (str.contains("'")) {
            return String.format("E'%s'", str.replaceAll("'", "''"));
        } else {
            return String.format("'%s'", str);
        }
    }

    public static String toPGString(final Object value) {
        final String valueStr;
        if (value == null) {
            valueStr = "";
        } else if (value instanceof java.util.Date) {
            valueStr = SIMPLE_DATE_FORMAT.format(value);
        } else if (value instanceof Calendar) {
            valueStr = SIMPLE_DATE_FORMAT.format(((Calendar) value).getTime());
        } else if (value instanceof TemporalAccessor) {
            valueStr = DATE_TIME_FORMATTER.format((TemporalAccessor) value);
        } else {
            valueStr = value.toString();
        }
        return valueStr;
    }

    /**
     * @param reader the reader
     * @return the same reader, buffered if necessary
     */
    public static BufferedReader ensureBuffered(final Reader reader) {
        final BufferedReader bufReader;
        if (reader instanceof BufferedReader) {
            bufReader = (BufferedReader) reader;
        } else {
            bufReader = new BufferedReader(reader);
        }
        return bufReader;
    }
}
