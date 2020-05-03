/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
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

package com.github.jferard.pgloaderutils.sniffer;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
	final static Pattern p = Pattern
			.compile("\\p{InCombiningDiacriticalMarks}+");

	public static String normalize(String s) {
		String decomposed = java.text.Normalizer.normalize(s.trim(),
				Normalizer.Form.NFD);
		return p.matcher(decomposed) // $NON-NLS-1$
				.replaceAll("").trim();
	}

	/**
	 * see https://github.com/richmilne/JaroWinkler/blob/master/jaro/strcmp95.c
	 */
	public static double strcmp95(final String first, final String second) {
		if (first == null || second == null) {
            throw new IllegalArgumentException();
        }
		return StringUtils.strcmp95Normalized(StringUtils.normalize(first).toLowerCase(),
				StringUtils.normalize(second).toLowerCase());
	}

	// TODO
	public static double strcmp95Normalized(String first, String second) {
		return 0;
	}
}
