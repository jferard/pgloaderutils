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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Counter<T> {
	private Map<T, Integer> countByElement;

	public Counter() {
		this.countByElement = new HashMap<T, Integer>();
	}

	public void put(final T element) {
		Integer count = this.countByElement.get(element);
		if (count == null) {
            count = 0;
        }

		this.countByElement.put(element, count + 1);
	}

	public List<T> sortedElements() {
		final List<T> l = new ArrayList<T>(this.countByElement.keySet());
		final Map<T, Integer> closedCountByElements = this.countByElement;
		Collections.sort(l, new Comparator<T>() {
			@Override
			public int compare(final T o1, final T o2) {
				return closedCountByElements.get(o1)
						- closedCountByElements.get(o2);
			}
		});
		return l;
	}

	public T maxElementOr(final T defaultElement) {
		final List<T> l = this.sortedElements();
		if (l.isEmpty()) {
            return defaultElement;
        } else {
            return l.get(0);
        }
	}

}
