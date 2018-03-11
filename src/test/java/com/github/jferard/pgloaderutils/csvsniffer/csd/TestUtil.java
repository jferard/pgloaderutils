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

package com.github.jferard.pgloaderutils.csvsniffer.csd;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by jferard on 07/04/17.
 */
public class TestUtil {
    public static CSDField getMandatoryField() {
        return new CSDField() {
            @Override
            public String getCode() {
                return "code";
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
            public boolean validate(String value) {
                return true;
            }
        };
    }

    public static CSDField getOptionalField() {
        return new CSDField() {
            @Override
            public String getCode() {
                return "code";
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
            public boolean validate(String value) {
                return true;
            }
        };
    }

    public static CSDField getStarField() {
        return new CSDField() {
            @Override
            public String getCode() {
                return "*";
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
            public boolean validate(String value) {
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
