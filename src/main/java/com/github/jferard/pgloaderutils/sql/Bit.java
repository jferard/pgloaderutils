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

import java.sql.Types;

/** fixed-length bit string */
public class Bit implements DataType {
    public static final Bit BIT1 = new Bit(1);
    public static final Bit BIT2 = new Bit(2);
    public static final Bit BIT3 = new Bit(3);
    public static final Bit BIT4 = new Bit(4);
    public static final Bit BIT5 = new Bit(5);
    public static final Bit BIT6 = new Bit(6);
    public static final Bit BIT7 = new Bit(7);
    public static final Bit BIT8 = new Bit(8);

    private final int n;

    public Bit(final int n) {
        this.n = n;
    }

    @Override
    public int getSqlType() {
        return Types.BIT;
    }

    @Override
    public String toString() {
        return String.format("BIT(%d)", this.n);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Bit)) {
            return false;
        }
        final Bit other = (Bit) o;
        return this.n == other.n;
    }

    @Override
    public int hashCode() {
        return this.n;
    }
}
