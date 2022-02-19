[![Build Status](https://app.travis-ci.com/jferard/pgloaderutils.svg?branch=master)](https://travis-ci.com/github/jferard/pgloaderutils)
[![Code Coverage](https://img.shields.io/codecov/c/github/jferard/pgloaderutils/master.svg)](https://codecov.io/github/jferard/pgloaderutils?branch=master)

# pgLoader Utils
(C) J. Férard 2016-2018 & 2020-2022

Some utilities for loading csv data into a PostgreSQL database: detect file encoding, CSV format and populate database, under GPL v3.

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
