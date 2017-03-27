package com.github.jferard.pgloaderutils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * Created by jferard on 24/03/17.
 */
public class ScriptParserTest {
    @Test
    public void test() throws IOException {
        Reader r = new StringReader("{a}\n\n{b}\n\n{c}");
        ScriptParser sc = new ScriptParser(r);
        try {
            Map<String, String> m = ImmutableMap.of("a", "1", "b", "2", "c", "3");
            List<String> l = Lists.newArrayList("1", "2", "3");
            Assert.assertEquals(l, sc.read(m));
        } finally {
            sc.close();
        }
    }
}