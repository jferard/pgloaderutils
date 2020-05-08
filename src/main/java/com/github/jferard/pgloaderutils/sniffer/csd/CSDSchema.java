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

package com.github.jferard.pgloaderutils.sniffer.csd;

import java.util.Collection;
import java.util.Iterator;

/**
 * A CSDSchema is a collection of CSDFields, without "wildcard" field.
 */
public class CSDSchema<F extends CSDField> implements SizedIterable<F> {
    private Collection<F> fields;
    private boolean optionalHeader;

    public CSDSchema(final Collection<F> fields, final boolean optionalHeader) {
        this.fields = fields;
        this.optionalHeader = optionalHeader;
    }

    @Override
    public int size() {
        return this.fields.size();
    }

    @Override
    public Iterator<F> iterator() {
        return this.fields.iterator();
    }

    @Override
    public String toString() {
        return this.fields.toString();
    }

    public String getColumns() {
        final StringBuilder sb = new StringBuilder("(");
        final Iterator<F> it = fields.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(", ").append(it.next());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean hasOptionalHeader() {
        return this.optionalHeader;
    }
}
