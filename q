warning: Les CRLF seront remplacés par des LF dans src/main/java/com/github/jferard/pgloaderutils/loader/CSVLoaderHelper.java.
Le fichier conservera ses fins de ligne d'origine dans votre copie de travail
warning: Les CRLF seront remplacés par des LF dans src/test/java/com/github/jferard/pgloaderutils/it/CSVLoaderForPostgreSQLIT.java.
Le fichier conservera ses fins de ligne d'origine dans votre copie de travail
[1mdiff --git a/src/main/java/com/github/jferard/pgloaderutils/loader/CSVLoaderHelper.java b/src/main/java/com/github/jferard/pgloaderutils/loader/CSVLoaderHelper.java[m
[1mindex fd2a7ec..dac45f2 100644[m
[1m--- a/src/main/java/com/github/jferard/pgloaderutils/loader/CSVLoaderHelper.java[m
[1m+++ b/src/main/java/com/github/jferard/pgloaderutils/loader/CSVLoaderHelper.java[m
[36m@@ -24,11 +24,12 @@[m [mpackage com.github.jferard.pgloaderutils.loader;[m
 import org.apache.commons.csv.CSVFormat;[m
 [m
 public class CSVLoaderHelper {[m
[31m-    public CSVFormat getCSVFormat(final char delimiter, final char quote, final char escape) {[m
[31m-        final CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(quote);[m
[32m+[m[32m    public static CSVFormat getCSVFormat(final char delimiter, final char quote, final char escape) {[m
[32m+[m[32m        final CSVFormat.Builder builder =[m
[32m+[m[32m                CSVFormat.Builder.create().setDelimiter(delimiter).setQuote(quote);[m
         if (escape != quote) {[m
[31m-            format.withEscape(escape);[m
[32m+[m[32m            builder.setEscape(escape);[m
         }[m
[31m-        return format;[m
[32m+[m[32m        return builder.build();[m
     }[m
 }[m
[1mdiff --git a/src/test/java/com/github/jferard/pgloaderutils/it/CSVLoaderForPostgreSQLIT.java b/src/test/java/com/github/jferard/pgloaderutils/it/CSVLoaderForPostgreSQLIT.java[m
[1mindex 6b64e01..5e0d0eb 100644[m
[1m--- a/src/test/java/com/github/jferard/pgloaderutils/it/CSVLoaderForPostgreSQLIT.java[m
[1m+++ b/src/test/java/com/github/jferard/pgloaderutils/it/CSVLoaderForPostgreSQLIT.java[m
[36m@@ -27,9 +27,7 @@[m [mimport com.github.jferard.pgloaderutils.loader.CSVBulkLoader;[m
 import com.github.jferard.pgloaderutils.loader.CSVRegularLoader;[m
 import com.github.jferard.pgloaderutils.reader.SimpleFileReader;[m
 import com.github.jferard.pgloaderutils.sql.Column;[m
[31m-import com.github.jferard.pgloaderutils.sql.DataType;[m
 import com.github.jferard.pgloaderutils.sql.GeneralDataType;[m
[31m-import com.github.jferard.pgloaderutils.sql.ValueConverter;[m
 import com.github.jferard.pgloaderutils.sql.Table;[m
 import com.google.common.io.Resources;[m
 import org.apache.commons.csv.CSVFormat;[m
[36m@@ -41,24 +39,24 @@[m [mimport java.io.IOException;[m
 import java.io.InputStreamReader;[m
 import java.io.Reader;[m
 import java.io.StringReader;[m
[32m+[m[32mimport java.nio.charset.StandardCharsets;[m
 import java.sql.Connection;[m
 import java.sql.DriverManager;[m
 import java.sql.SQLException;[m
 import java.sql.Statement;[m
[31m-import java.text.ParseException;[m
 import java.util.Arrays;[m
 import java.util.Collections;[m
 import java.util.logging.Logger;[m
 [m
 public class CSVLoaderForPostgreSQLIT {[m
[32m+[m[32m    @Test[m
     public final void test() throws IOException, InterruptedException {[m
         try {[m
             Class.forName("org.postgresql.Driver");[m
             try {[m
[31m-                final Connection connection = DriverManager.getConnection([m
[32m+[m[32m                try (final Connection connection = DriverManager.getConnection([m
                         "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",[m
[31m-                        "postgres");[m
[31m-                try {[m
[32m+[m[32m                        "postgres")) {[m
                     final Statement statement = connection.createStatement();[m
                     statement.executeUpdate([m
                             "DROP TABLE IF EXISTS testtable");[m
[36m@@ -74,8 +72,6 @@[m [mpublic class CSVLoaderForPostgreSQLIT {[m
                     final SimpleFileReader csvReader = new SimpleFileReader([m
                             stringReader, Logger.getLogger(""), 16);[m
                     loader.populate(connection, csvReader, false);[m
[31m-                } finally {[m
[31m-                    connection.close();[m
                 }[m
             } catch (final SQLException e) {[m
                 e.printStackTrace();[m
[36m@@ -90,10 +86,9 @@[m [mpublic class CSVLoaderForPostgreSQLIT {[m
         try {[m
             Class.forName("org.postgresql.Driver");[m
             try {[m
[31m-                final Connection connection = DriverManager.getConnection([m
[32m+[m[32m                try (final Connection connection = DriverManager.getConnection([m
                         "jdbc:postgresql://127.0.0.1:5432/sirene", "postgres",[m
[31m-                        "postgres");[m
[31m-                try {[m
[32m+[m[32m                        "postgres")) {[m
                     final Statement statement = connection.createStatement();[m
                     statement.executeUpdate([m
                             "DROP TABLE IF EXISTS sirc");[m
[36m@@ -205,13 +200,10 @@[m [mpublic class CSVLoaderForPostgreSQLIT {[m
                             .toTable("sirc", ';', '"');[m
                     final Reader reader = new InputStreamReader(Resources.getResource[m
                                     ("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv")[m
[31m-                            .openStream(),[m
[31m-                            "ISO-8859-1");[m
[32m+[m[32m                            .openStream(), StandardCharsets.ISO_8859_1);[m
                     final SimpleFileReader csvReader = new SimpleFileReader([m
                             reader, Logger.getLogger(""), 16);[m
                     loader.populate(connection, csvReader, false);[m
[31m-                } finally {[m
[31m-                    connection.close();[m
                 }[m
             } catch (final SQLException e) {[m
                 e.printStackTrace();[m
[36m@@ -223,7 +215,7 @@[m [mpublic class CSVLoaderForPostgreSQLIT {[m
 [m
     @Test[m
     public final void testResourceRegular()[m
[31m-			throws IOException, InterruptedException, SQLException, ParseException {[m
[32m+[m[32m            throws IOException, SQLException {[m
         final PGSimpleDataSource source = new PGSimpleDataSource();[m
         source.setServerNames(new String[]{"localhost"});[m
         source.setDatabaseName("postgres");[m
[36m@@ -335,16 +327,17 @@[m [mpublic class CSVLoaderForPostgreSQLIT {[m
             final Statement statement = connection.createStatement();[m
             statement.executeUpdate(table.dropTableQuery(true));[m
             statement.executeUpdate(table.createTableQuery(false));[m
[31m-			final Reader reader = new InputStreamReader(Resources.getResource[m
[31m-							("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv")[m
[31m-					.openStream(),[m
[31m-					"ISO-8859-1");[m
[31m-			final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withDelimiter(';'));[m
[31m-			final CSVData data = new CSVData(parser, Collections.emptyList(), 1,[m
[32m+[m[32m            final Reader reader = new InputStreamReader(Resources.getResource[m
[32m+[m[32m                            ("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv")[m
[32m+[m[32m                    .openStream(), StandardCharsets.ISO_8859_1);[m
[32m+[m[32m            final CSVFormat csvFormat =[m
[32m+[m[32m                    CSVFormat.Builder.create(CSVFormat.EXCEL).setDelimiter(';').build();[m
[32m+[m[32m            final CSVParser parser = new CSVParser(reader, csvFormat);[m
[32m+[m[32m            final CSVData data = new CSVData(parser, Collections.emptyList(), 1,[m
                     (value, type) -> value);[m
[31m-			final CSVRegularLoader loader =[m
[31m-					data.toRegularLoader(table);[m
[31m-			loader.load(connection, 100);[m
[32m+[m[32m            final CSVRegularLoader loader =[m
[32m+[m[32m                    data.toRegularLoader(table);[m
[32m+[m[32m            loader.load(connection, 100);[m
         }[m
     }[m
 }[m
[1mdiff --git a/src/test/java/com/github/jferard/pgloaderutils/reader/CSVCleanerFileReaderTest.java b/src/test/java/com/github/jferard/pgloaderutils/reader/CSVCleanerFileReaderTest.java[m
[1mindex 79719e9..c8f6c6b 100644[m
[1m--- a/src/test/java/com/github/jferard/pgloaderutils/reader/CSVCleanerFileReaderTest.java[m
[1m+++ b/src/test/java/com/github/jferard/pgloaderutils/reader/CSVCleanerFileReaderTest.java[m
[36m@@ -22,10 +22,8 @@[m
 [m
 package com.github.jferard.pgloaderutils.reader;[m
 [m
[31m-import com.github.jferard.pgloaderutils.CSVRecordProcessor;[m
 import com.github.jferard.pgloaderutils.TestHelper;[m
 import org.apache.commons.csv.CSVFormat;[m
[31m-import org.apache.commons.csv.CSVRecord;[m
 import org.junit.Assert;[m
 import org.junit.Test;[m
 [m
[36m@@ -39,17 +37,17 @@[m [mimport java.util.List;[m
 public class CSVCleanerFileReaderTest {[m
     @Test[m
     public void test() throws IOException {[m
[32m+[m[32m        final CSVFormat csvFormat =[m
[32m+[m[32m                CSVFormat.Builder.create(CSVFormat.RFC4180).setDelimiter(';').build();[m
         final CSVProcessorFileReader r =[m
[31m-                CSVProcessorFileReader.fromReader(new StringReader("a;b;c\n1,0;2;3"),[m
[31m-                        CSVFormat.RFC4180.withDelimiter(';'), new CSVRecordProcessor() {[m
[31m-                            @Override[m
[31m-                            public Iterable<String> cleanRecord(final CSVRecord record) {[m
[31m-                                final List<String> ret = new ArrayList<>(record.size());[m
[31m-                                ret.add(0, record.get(0).replace(',', '.'));[m
[31m-                                ret.add(1, record.get(1));[m
[31m-                                ret.add(2, record.get(2));[m
[31m-                                return ret;[m
[31m-                            }[m
[32m+[m[32m                CSVProcessorFileReader.fromReader([m
[32m+[m[32m                        new StringReader("a;b;c\n1,0;2;3"), csvFormat,[m
[32m+[m[32m                        record -> {[m
[32m+[m[32m                            final List<String> ret = new ArrayList<>(record.size());[m
[32m+[m[32m                            ret.add(0, record.get(0).replace(',', '.'));[m
[32m+[m[32m                            ret.add(1, record.get(1));[m
[32m+[m[32m                            ret.add(2, record.get(2));[m
[32m+[m[32m                            return ret;[m
                         });[m
         r.open();[m
         Assert.assertEquals("a,b,c\r\n" +[m
[36m@@ -59,10 +57,13 @@[m [mpublic class CSVCleanerFileReaderTest {[m
 [m
     @Test[m
     public void testFromStream() throws IOException {[m
[32m+[m[32m        final CSVFormat csvFormat =[m
[32m+[m[32m                CSVFormat.Builder.create(CSVFormat.RFC4180).setDelimiter(';').build();[m
         final CSVProcessorFileReader r =[m
[31m-                CSVProcessorFileReader.fromStream(new ByteArrayInputStream("a;b;c\n1,0;2;3".getBytes([m
[31m-                                StandardCharsets.UTF_8)), StandardCharsets.UTF_8,[m
[31m-                        CSVFormat.RFC4180.withDelimiter(';'), record -> {[m
[32m+[m[32m                CSVProcessorFileReader.fromStream([m
[32m+[m[32m                        new ByteArrayInputStream("a;b;c\n1,0;2;3".getBytes([m
[32m+[m[32m                                StandardCharsets.UTF_8)), StandardCharsets.UTF_8, csvFormat,[m
[32m+[m[32m                        record -> {[m
                             final List<String> ret = new ArrayList<>(record.size());[m
                             ret.add(0, record.get(0).replace(',', '.'));[m
                             ret.add(1, record.get(1));[m
