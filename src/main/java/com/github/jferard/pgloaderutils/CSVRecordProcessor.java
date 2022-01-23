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

import org.apache.commons.csv.CSVRecord;

import java.text.ParseException;

/**
 * The class CSVRecordProcessor cleans and transforms a CSVRecord into a String iterable.
 * It is used to clean a csv file on the fly.
 *
 * One may remove some fields, change the order of the fields, add common values.
 *
 * @author Julien Férard
 */
public interface CSVRecordProcessor {

    /**
     * @param record the commons csv record
     * @return the strings cleaned
     * @throws ParseException if a value is not parsable. Might throw a RuntimeExcepton as well.
     */
    Iterable<String> cleanRecord(CSVRecord record) throws ParseException;
}
