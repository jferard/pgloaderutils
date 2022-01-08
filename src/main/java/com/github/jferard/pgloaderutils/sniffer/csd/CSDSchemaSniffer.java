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

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * The CSDSchemaSniffer is a csv. Given a CSDSchemaPattern, a CSVParser and a maximul line, it will return a
 * CSDSchema (or null).
 */
public class CSDSchemaSniffer<F extends CSDFieldPattern> {
    public static <G extends CSDFieldPattern> CSDSchemaSniffer<G> create(final Logger logger, final CSDFieldFactory<G> factory) {
        final CSDUtil u = new CSDUtil(logger);
        final CSDValidatorHelper<G> hv = new CSDValidatorHelper<G>(logger, new FlexibleColumnMatcher(logger, u, 2));
        return new CSDSchemaSniffer<G>(logger, factory, hv);
    }
    private final Logger logger;
    private CSDValidationResult<F> result;
    private CSDFieldFactory<F> factory;
    private CSDValidatorHelper<F> validatorHelper;

    public CSDSchemaSniffer(final Logger logger, final CSDFieldFactory<F> factory, final CSDValidatorHelper<F> validatorHelper) {
        this.logger = logger;
        this.factory = factory;
        this.validatorHelper = validatorHelper;
    }

    /**
     * @param schemaPattern the pattern to be tested
     * @param parser        the CSVCRecord provider
     * @param maxLine       the maximum number of lines
     * @return the real CSDSchema, or null if the pattern does not match.
     */
    public CSDSchema<F> sniff(final CSDSchemaPattern<F> schemaPattern, final CSVParser parser, final int maxLine) {
        this.result = new CSDValidationResult<F>(logger, schemaPattern);
        final Iterator<CSVRecord> it = parser.iterator();

        if (!it.hasNext()) {
            result.noLine();
            return null;
        }

        final CSVRecord firstRecord = it.next();
        if (!this.validateHeaderOrFirstRecord(result, schemaPattern, firstRecord)) {
            return null;
        }

        int i = 1;
        while (it.hasNext() && i < maxLine) {
            this.validatorHelper.validateRecord(result, schemaPattern, it.next(), i++);
        }

        if (this.result.errorCount() > maxLine) {
            return null;
        }

        return schemaPattern.newSchema(factory, firstRecord);
    }

    private boolean validateHeaderOrFirstRecord(final CSDValidationResult<F> result, final CSDSchemaPattern<F> schemaPattern,
                                                final CSVRecord firstRecord) {
        final int headerErrorCount = this.validatorHelper.validateHeader(this.result, schemaPattern, firstRecord);

        switch (headerErrorCount) {
            case -1:
                return false;
            case 0:
                return true;
            case 1:
                this.logger.severe("One clear error in header, but it seems to match.");
                return true;
            default:
                if (schemaPattern.hasOptionalHeader()) {
                    this.logger.info("The header does not match. Maybe a missing header.");
                    final CSDValidationResult<F> tempResult = new CSDValidationResult<F>(this.logger, schemaPattern);
                    this.validatorHelper.validateRecord(tempResult, schemaPattern, firstRecord, 1);
                    if (tempResult.isOk()) {
                        this.result = tempResult;
                        this.result.missingHeader();
                        return true;
                    } else {
                        this.result.badHeader();
                        return false;
                    }
                } else {
                    return false;
                }
        }

    }
}
