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

public class ScoreCSVFormatSnifferTest extends CSVFormatSnifferTest {
    @Override
    protected CSVFormatSniffer getFieldsSniffer(final int i) {
        return CSVFormatSniffer.createScore(
                ScoreCSVConstraints.builder().minFields(i).build());
    }

    @Override
    protected CSVFormatSniffer getTabPipeSniffer() {
        return CSVFormatSniffer.createScore(
                ScoreCSVConstraints.builder().allowedDelimiters(new byte[]{'\t', '|'}).minFields(5)
                        .build());
    }

    @Override
    protected CSVFormatSniffer getCommaSemiColonSniffer() {
        return CSVFormatSniffer.createScore(
                ScoreCSVConstraints.builder().allowedDelimiters(new byte[]{',', ';', '\t', '|'})
                        .build());
    }

    @Override
    protected CSVFormatSniffer getStandardSniffer() {
        return CSVFormatSniffer.createScore(ScoreCSVConstraints.builder().build());
    }

}
