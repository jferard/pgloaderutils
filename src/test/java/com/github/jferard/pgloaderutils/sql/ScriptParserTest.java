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

package com.github.jferard.pgloaderutils.sql;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Created by jferard on 24/03/17.
 */
public class ScriptParserTest {
    @Test
    public void test() throws IOException {
        final Reader r = new StringReader("-- 1\n" +
                "DROP TABLE IF EXISTS %s;\n" +
                "\n" +
                "-- 2\n" +
                "CREATE TABLE %s (\n" +
                "    col INT\n" +
                ");");
        Assert.assertEquals(Arrays.asList("DROP TABLE IF EXISTS %s",
                "CREATE TABLE %s (\n" +
                        "    col INT\n" +
                        ")"), ScriptParser.parse(r));
    }
}
