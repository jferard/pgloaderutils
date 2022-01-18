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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CSVFile {
    /**
     * Path of the file
     */
    private final Path path;

    /**
     * Charset of the file
     */
    private final Charset charset;

    /**
     * The CSV format of the data file
     */
    private final CSVFormat csvFormat;

    public CSVFile(final Path path, final Charset charset, final CSVFormat csvFormat) {
        this.path = path;
        this.charset = charset;
        this.csvFormat = csvFormat;
    }

    public CSVParser newCSVParser()
            throws IOException {
        final BufferedReader reader = Files.newBufferedReader(this.path, this.charset);
        if (this.charset.equals(StandardCharsets.UTF_8)) {
            reader.mark(1);
            if (reader.read() != '\uFEFF') { // the infamous BOM
                reader.reset();
            }
        }
        return new CSVParser(
                reader, this.csvFormat);
    }
}
