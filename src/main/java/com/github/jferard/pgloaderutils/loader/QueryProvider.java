package com.github.jferard.pgloaderutils.loader;

import java.util.Map;

/**
 * Replaces keys in a template query. The template has the format: "some text {key} some other text".
 * If valueByKey[key] == value, then the template is converted to "some text value some other text"
 *
 * @author Julien Férard
 */
public class QueryProvider {
    /**
     * @param template the template, keys are written between brackets.
     * @param valueByKey the map key->value
     * @return the query
     */
    public String newQuery(String template, Map<String, String> valueByKey) {
        StringBuilder queryBuilder = new StringBuilder();

        int lastJ = 0;
        int i = template.indexOf('{');
        while (i >= 0) {
            int j = template.indexOf('}', i);
            if (j == -1)
                throw new IllegalStateException(template);
            String before = template.substring(lastJ, i);
            String key = template.substring(i+1, j);
            queryBuilder.append(before);
            String value = valueByKey.get(key);
            if (value == null)
                throw new IllegalStateException(template+" % "+valueByKey+" missing "+key);

            queryBuilder.append(value);

            lastJ = j+1;
            i = template.indexOf('{', lastJ);
        }
        queryBuilder.append(template.substring(lastJ));
        return queryBuilder.toString();
    }
}
