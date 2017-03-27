package com.github.jferard.pgloaderutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parses a script with query separated by a newline
 *
 * @author Julien Férard
 */
public class ScriptParser {
    private final BufferedReader br;
    private QueryProvider provider;

    public ScriptParser(Reader reader) {
        this(reader, new QueryProvider());
    }

    public ScriptParser(Reader reader, QueryProvider provider) {
        this.br = new BufferedReader(reader);
        this.provider = provider;
    }

    public void close() throws IOException {
        this.br.close();
    }

    public List<String> read(Map<String, String> valueByKey) throws IOException {
        List<String> queries = new ArrayList<String>();

        List<String> curQueryLines = new ArrayList<String>();
        for (String line = this.br.readLine(); line != null; line = this.br.readLine()) {
            if (this.isCommentLine(line))
                continue;

            if (this.isBlankLine(line) && curQueryLines.size() > 0) {
                queries.add(this.newQuery(curQueryLines, valueByKey));
            } else {
                curQueryLines.add(line);
            }
        }
        if (curQueryLines.size() > 0)
            queries.add(this.newQuery(curQueryLines, valueByKey));
        return queries;
    }

    public String newQuery(List<String> curQueryLines, Map<String, String> valueByKey) {
        Iterator<String> it = curQueryLines.iterator();
        if (!it.hasNext())
            throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(it.next());
        while (it.hasNext()) {
            sb.append('\n').append(it.next());
        }

        String template = sb.toString();
        curQueryLines.clear();
        return this.provider.newQuery(template, valueByKey);
    }

    private boolean isBlankLine(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty();
    }

    private boolean isCommentLine(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("--");
    }
}
