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

package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Util {
    public final static Charset UTF_8 = Charset.forName("UTF-8");
    public final static Charset US_ASCII = Charset.forName("US-ASCII");

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
                    throw new AssertionError(e);
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
    public static String pgEscapeIdentifier(final String identifier) {
        if (Character.isJavaIdentifierStart(identifier.charAt(0))
                && identifier.chars().skip(1).allMatch(Character::isJavaIdentifierPart)) {
            return identifier.toLowerCase(Locale.ROOT);
        } else {
            return String.format("\"%s\"", identifier.replaceAll("\"", "\"\""));
        }
    }

    public static String pgEscapeString(final String str) {
        if (str.contains("'")) {
            return String.format("E'%s'", str.replaceAll("'", "''"));
        } else {
            return String.format("'%s'", str);
        }
    }

}
