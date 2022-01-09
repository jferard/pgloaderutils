package com.github.jferard.pgloaderutils.sql;

import java.sql.Types;
import java.util.Locale;

/**
 * https://www.postgresql.org/docs/current/datatype.html
 * Table 8.1
 */
public enum GeneralDataType implements DataType {
    /** signed eight-byte integer */
    BIGINT(Types.BIGINT),

    /** signed eight-byte integer */
    INT8(Types.BIGINT),

    /** autoincrementing eight-byte integer */
    BIGSERIAL(Types.BIGINT),

    /** autoincrementing eight-byte integer */
    SERIAL8(Types.BIGINT),

    /** fixed-length bit string */
    BIT(Types.BIT),

    /** variable-length bit string */
    VARBIT(Types.BIT),

    /** logical Boolean (true/false) */
    BOOLEAN(Types.BOOLEAN),

    /** logical Boolean (true/false) */
    BOOL(Types.BOOLEAN),

    /** binary data (“byte array”) */
    BYTEA(Types.BINARY),

    /** fixed-length character string */
    CHARACTER(Types.CHAR),

    /** fixed-length character string */
    CHAR(Types.CHAR),

    /** variable-length character string */
    VARCHAR(Types.VARCHAR),

    /** calendar date (year, month, day) */
    DATE(Types.DATE),

    /** double precision floating-point number (8 bytes) */
    DOUBLE(Types.DOUBLE),

    /** double precision floating-point number (8 bytes) */
    FLOAT8(Types.DOUBLE),

    /** signed four-byte integer */
    INTEGER(Types.INTEGER),

    /** signed four-byte integer */
    INT(Types.INTEGER),

    /** signed four-byte integer */
    INT4(Types.INTEGER),

    /** exact numeric of selectable precision */
    NUMERIC(Types.NUMERIC),

    /** exact numeric of selectable precision */
    DECIMAL(Types.DECIMAL),

    /** single precision floating-point number (4 bytes) */
    REAL(Types.REAL),

    /** single precision floating-point number (4 bytes) */
    FLOAT4(Types.REAL),

    /** signed two-byte integer */
    SMALLINT(Types.SMALLINT),

    /** signed two-byte integer */
    INT2(Types.SMALLINT),

    /** variable-length character string */
    TEXT(Types.VARCHAR),

    /** time of day (no time zone) */
    TIME(Types.TIME),

    /** time of day, including time zone */
    TIMETZ(Types.TIME), // Types.TIME_WITH_TIMEZONE

    /** date and time (no time zone) */
    TIMESTAMP(Types.TIMESTAMP),

    /** date and time, including time zone */
    TIMESTAMPTZ(Types.TIMESTAMP), // Types.TIMESTAMP_WITH_TIMEZONE

    /** XML data */
    XML(Types.SQLXML);

    private final int sqlType;

    GeneralDataType(final int sqlType) {
        this.sqlType = sqlType;
    }


    @Override
    public int getSqlType() {
        return sqlType;
    }

    @Override
    public String toString() {
        return this.name().toUpperCase(Locale.US);
    }
}
