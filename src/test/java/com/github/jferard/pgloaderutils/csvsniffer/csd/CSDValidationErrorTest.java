package com.github.jferard.pgloaderutils.csvsniffer.csd;

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