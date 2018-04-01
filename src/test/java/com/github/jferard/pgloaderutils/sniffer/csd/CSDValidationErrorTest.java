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

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jferard on 06/04/17.
 */
public class CSDValidationErrorTest {
    @Test
    public void toStringTest() throws Exception {
        CSDValidationError ve = new CSDValidationError(10, CSDValidationError.Type.INCORRECT_COLUMN_NAME, "@");
        Assert.assertEquals("CSDValidationError of type INCORRECT_COLUMN_NAME: @ (line 10)", ve.toString());
    }

}