package com.jagrosh.jmusicbot.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class OtherUtilTest
{
    @Test
    public void testNormalizeVersion_stripsVPrefix()
    {
        assertEquals("0.4.6", OtherUtil.normalizeVersion("v0.4.6"));
        assertEquals("0.4.6", OtherUtil.normalizeVersion("V0.4.6"));
        assertEquals("0.4.6", OtherUtil.normalizeVersion("  v0.4.6  "));
    }

    @Test
    public void testNormalizeVersion_handlesNullAndUnknown()
    {
        assertNull(OtherUtil.normalizeVersion(null));
        assertNull(OtherUtil.normalizeVersion(""));
        assertNull(OtherUtil.normalizeVersion("   "));
        assertNull(OtherUtil.normalizeVersion("UNKNOWN"));
        assertNull(OtherUtil.normalizeVersion(" unknown "));
    }

    @Test
    public void testNormalizeVersion_doesNotStripNonVersionV()
    {
        assertEquals("version", OtherUtil.normalizeVersion("version"));
        assertEquals("vNext", OtherUtil.normalizeVersion("vNext"));
    }
}

