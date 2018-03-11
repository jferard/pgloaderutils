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

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * The CSDSchemaSniffer#validateHeader method returns the full result of a validation.
 */
public class CSDSchemaValidator<F extends CSDField> {
    private final Logger logger;
    private final CSDValidatorHelper<F> validatorHelper;
    private CSDValidationResult<F> result;

    public static <G extends CSDField> CSDSchemaValidator<G> create(Logger logger) {
        CSDValidatorHelper<G> hv = new CSDValidatorHelper<G>(logger, new ExactColumnMatcher(logger));
        return new CSDSchemaValidator<G>(logger, hv);
    }

    public CSDSchemaValidator(Logger logger, CSDValidatorHelper<F> validatorHelper) {
        this.logger = logger;
        this.validatorHelper = validatorHelper;
    }

    /**
     * @param schema the schema to be tested
     * @param parser the CSVCRecord provider
     * @return a validation result
     */
    public CSDValidationResult<F> validate(CSDSchema<F> schema, CSVParser parser) {
        this.result = new CSDValidationResult<F>(logger, schema);
        Iterator<CSVRecord> it = parser.iterator();
        if (it.hasNext()) {
            CSVRecord firstRecord = it.next();
            this.validateHeaderOrFirstRecord(schema, firstRecord);
            int i=1;
            while (it.hasNext())
                this.validatorHelper.validateRecord(result, schema, it.next(), i++);

        } else {
            result.noLine();
        }
        return this.result;
    }

    private void validateHeaderOrFirstRecord(CSDSchema<F> schema, CSVRecord firstRecord) {
        int headerErrorCount = this.validatorHelper.validateHeader(this.result, schema, firstRecord);
        if (headerErrorCount > 1 && schema.hasOptionalHeader()) {
            CSDValidationResult<F> tempResult = new CSDValidationResult<F>(this.logger, schema);
            this.validatorHelper.validateRecord(tempResult, schema, firstRecord, 1);
            if (tempResult.isOk()) {
                this.result = tempResult;
                this.result.missingHeader();
            } else {
                this.result.badHeader();
            }
        }
    }
}