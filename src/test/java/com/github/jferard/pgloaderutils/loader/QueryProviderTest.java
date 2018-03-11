package com.github.jferard.pgloaderutils.loader;

import com.github.jferard.pgloaderutils.loader.QueryProvider;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jferard on 27/03/17.
 */
public class QueryProviderTest {
    private QueryProvider provider;

    @Before
    public void setUp() {
        this.provider = new QueryProvider();
    }

    @Test
    public void testNewQuery() throws IOException {
        Map<String, String> m = ImmutableMap.of("a", "1", "b", "2", "c", "3");
        Assert.assertEquals("a2c", this.provider.newQuery("a{b}c", m));
    }
}