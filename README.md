[![Build Status](https://app.travis-ci.com/jferard/pgloaderutils.svg?branch=master)](https://travis-ci.com/github/jferard/pgloaderutils)
[![Code Coverage](https://img.shields.io/codecov/c/github/jferard/pgloaderutils/master.svg)](https://codecov.io/github/jferard/pgloaderutils?branch=master)

# pgLoader Utils
(C) J. Férard 2016-2018 & 2020-2022

Some utilities for loading csv data into a PosgtreSQL database: detect file encoding, CSV format and populate database, under GPL v3.

## Presentation
pgLoader Utils is a small set of classes to make PostgreSQL bulk load simpler.
It allows to process the CSV file on the fly (e.g. to format dates).

The CSV Sniffer part is a slow but (I hope) reliable sniffer that detects, for a given CSV file :
- its encoding, among three values : ASCII, UTF-8, "other" ;
- its delimiter char, quote char and escape char ;
- whether it has or not a header.

## Usage
Here is an example:

    try {
        Class.forName("org.postgresql.Driver");
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",
                    "postgres");
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        "DROP TABLE IF EXISTS testtable");
                statement.executeUpdate(
                        "CREATE TABLE testtable ("
                                + "col1 text," + "col2 decimal,"
                                + "col3 text)");

                CSVLoaderForPostgreSQL loader = CSVLoaderForPostgreSQL
                        .toTable("testtable");
                final StringReader stringReader = new StringReader("\"a\", 1.0, \"b,c\"\n"
                        + "\"d\", 2.0, \"f,g\"\n");
                final CSVSimpleFileReader csvReader = new CSVSimpleFileReader(
                        stringReader, Logger.getLogger(""), 16);
                loader.populate(connection,
                        csvReader);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }


## Constraints on CSV format
### Mandatory constraints
* At least two columns ;
* delimiter, quote, escape are ASCII chars (0..127), but can't be letters, digits or space char.

### Optional constraints
It is possible to specifiy :
* the minimum number of columns allowed ;
* the set of chars accepted for delimiter, quote or escape.
Those optional constraints highly improve the reliability of the result.

## Process
Given a sample chunk of the file, CSV sniffer processes the data.

### Encoding
All bytes are processed. If all bytes are less than 128, then the encoding is expected to be ASCII. If a leading UTF-8 byte is found, but the trailing bytes are missing, encoding is "other". Else (all UTF-8 leading bytes have their trailing bytes), it is expected to be UTF-8.

### Delimiter
The chunk is split into lines on CR, LF or CRLF.
All lines are processed, splitted on all possible delimiter chars. The winner depends on mean and variance in thee set of lines.

### Quote
Each line is split on the winner delimiter. First and last char may be the quote char. The winner depends on the number of apparitions as first and last char, and (less) as first or last char. 

### Escape
The character which is more present before delimiter in really (by commons csv) parsed records.

### Header
If there is a full digit record in first line, then this line is not the header. Else, first and following lines must match.