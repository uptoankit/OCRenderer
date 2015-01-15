/*
 * Copyright (C) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Authors : Dave Tucker
 */

package org.opendaylight.groupbasedpolicy.jsonrpc;

import static io.netty.buffer.Unpooled.copiedBuffer;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.CharsetUtil;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.groupbasedpolicy.jsonrpc.JsonRpcDecoder;

public class JsonRpcDecoderTest {
    static int testJson_BYTES = 179;
    String testJson;
    String prettyTestJson;
    static final String PREAMBLE = "                    ";
    static final String PARTIAL_START = "{\"foo\":";
    static final String PARTIAL_END = "{\"bar\":\"baz\"}}";

    JsonRpcDecoder decoder;
    EmbeddedChannel ch;

    @Before
    public void setUp() throws Exception {
        decoder = new JsonRpcDecoder(1000);
        ch = new EmbeddedChannel(decoder);

        URL testJsonUrl = Resources.getResource(JsonRpcDecoderTest.class, "test.json");
        testJson = Resources.toString(testJsonUrl, Charsets.UTF_8);
        URL prettyTestJsoUrl = Resources.getResource(JsonRpcDecoderTest.class, "pretty-test.json");
        prettyTestJson = Resources.toString(prettyTestJsoUrl, Charsets.UTF_8);
    }

    @Test
    public void testDecode() throws Exception {
        for (int i = 0; i < 10; i++) {
            ch.writeInbound(copiedBuffer(testJson, CharsetUtil.UTF_8));
        }
        ch.readInbound();
        assertEquals(10, decoder.getRecordsRead());
        ch.finish();
    }

    @Test
    public void testDecodePrettyJson() throws Exception {
        ch.writeInbound(copiedBuffer(prettyTestJson, CharsetUtil.UTF_8));
        ch.readInbound();
        assertEquals(1, decoder.getRecordsRead());
        ch.finish();
    }

    @Test
    public void testDecodeSkipSpaces() throws Exception {
        ch.writeInbound(copiedBuffer(PREAMBLE + testJson + PREAMBLE + testJson, CharsetUtil.UTF_8));
        ch.readInbound();
        assertEquals(2, decoder.getRecordsRead());
        ch.finish();
    }

    @Test
    public void testDecodePartial() throws Exception {
        ch.writeInbound(copiedBuffer(PARTIAL_START, CharsetUtil.UTF_8));
        ch.readInbound();
        Thread.sleep(10);
        ch.writeInbound(copiedBuffer(PARTIAL_END, CharsetUtil.UTF_8));
        ch.readInbound();
        assertEquals(1, decoder.getRecordsRead());
        ch.finish();
    }

    @Test(expected= DecoderException.class)
    public void testDecodeInvalidEncoding() throws Exception {
        ch.writeInbound(copiedBuffer(testJson, CharsetUtil.UTF_16));
        ch.finish();
    }

    @Test(expected=TooLongFrameException.class)
    public void testDecodeFrameLengthExceed() {
        decoder = new JsonRpcDecoder(testJson_BYTES -1);
        ch = new EmbeddedChannel(decoder);
        ch.writeInbound(copiedBuffer(testJson, CharsetUtil.UTF_8));
        ch.finish();
    }
} 
