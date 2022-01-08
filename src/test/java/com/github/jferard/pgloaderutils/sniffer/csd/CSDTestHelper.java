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

package com.github.jferard.pgloaderutils.sniffer.csd;

import java.util.Collection;
import java.util.Iterator;

/**
 * A helper for CSD tests
 */
public class CSDTestHelper {
    public static CSDFieldPattern namedField(final String name) {
        return new CSDFieldPattern() {
            @Override
            public boolean isWildCard() {
                return false;
            }

            @Override
            public String getType() {
                return null;
            }

            @Override
            public String getColumnName() {
                return name;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public boolean validate(final String value) {
                return false;
            }
        };
    }

    public static CSDFieldPattern getMandatoryField() {
        return new CSDFieldPattern() {
            @Override
            public boolean isWildCard() {
                return false;
            }

            @Override
            public String getType() {
                return "type";
            }

            @Override
            public String getColumnName() {
                return "name";
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public boolean validate(final String value) {
                return true;
            }

            @Override
            public String toString() {
                return "CSDFieldPattern["+getType()+": "+getColumnName()+"]";
            }

        };
    }

    public static CSDFieldPattern getOptionalField() {
        return new CSDFieldPattern() {
            @Override
            public boolean isWildCard() {
                return false;
            }

            @Override
            public String getType() {
                return "type";
            }

            @Override
            public String getColumnName() {
                return "name";
            }

            @Override
            public boolean isOptional() {
                return true;
            }

            @Override
            public boolean validate(final String value) {
                return true;
            }
        };
    }

    public static CSDFieldPattern getStarField() {
        return new CSDFieldPattern() {
            @Override
            public boolean isWildCard() {
                return true;
            }

            @Override
            public String getType() {
                return "type";
            }

            @Override
            public String getColumnName() {
                return "name";
            }

            @Override
            public boolean isOptional() {
                return true;
            }

            @Override
            public boolean validate(final String value) {
                return true;
            }
        };
    }

    public static <F> SizedIterable<F> fromCollection(final Collection<F> c) {
        return new SizedIterable<F>() {
            @Override
            public int size() {
                return c.size();
            }

            @Override
            public Iterator<F> iterator() {
                return c.iterator();
            }
        };
    }
}
