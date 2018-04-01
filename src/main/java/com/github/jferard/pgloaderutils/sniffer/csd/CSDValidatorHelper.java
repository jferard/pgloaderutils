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

import org.apache.commons.csv.CSVRecord;

import java.util.logging.Logger;

/**
 * A CSDValidatorHelper validates a header or a normal record.
 */
class CSDValidatorHelper<F extends CSDFieldPattern> {
    private Logger logger;
    private ColumnMatcher matcher;

    public CSDValidatorHelper(Logger logger, ColumnMatcher matcher) {
        this.logger = logger;
        this.matcher = matcher;
    }

    /**
     * @param result the result that will hold the errors
     * @param fields the schema / schema pattern
     * @param firstRecord the first record of the stream
     * @return the error count, -1 the schema has too many fields
     */
    public int validateHeader(CSDValidationResult<F> result, SizedIterable<F> fields, CSVRecord firstRecord) {
        int headerErrorCount = 0;
        if (firstRecord.size() < fields.size()) {
            result.schemaHasTooManyFieldsForHeader(firstRecord);
            return -1;
        }

        int j = 0;
        for (F field : fields) {
            String value = firstRecord.get(j++);
            if (!matcher.match(field, value)) {
                result.incorrectColumnName(field, value);
                headerErrorCount++;
            }
        }
        return headerErrorCount;
    }

    /**
     * @param result the result that will hold the errors
     * @param fields the schema / schema pattern
     * @param record the current record of the stream
     * @param line the current line number
     * @return the error count, -1 the schema has too many fields
     */
    public int validateRecord(CSDValidationResult<F> result, SizedIterable<F> fields, CSVRecord record, int line) {
        if (record.size() < fields.size()) {
            result.schemaHasTooManyFieldsForRecord(line, record);
            return -1;
        }

        int errorCount = 0;
        int j = 0;
        for (F field : fields) {
            String value = record.get(j++);
            if (!field.validate(value)) {
                result.incorrectValue(line, value, field);
                errorCount++;
            }
        }
        return errorCount;
    }

}
