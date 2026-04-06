/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.fileupload.MultipartStream}.
 */
public class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize =
                boundary.length + MultipartStream.BOUNDARY_PREFIX.length + 1;
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSmallBuffer() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize = 1;
        new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
    }

    @Test
    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test
    public void testMalformedUploadTruncatedHeadersOnBoundary() throws IOException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n";
        // Intentionally no terminating CRLF CRLF

        final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(-1);
        upload.setPartHeaderSizeMax(-1);

        final MockHttpServletRequest req = new MockHttpServletRequest(
                request.toString().getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadException e) {
            // Expected
            assertTrue(e.getCause() instanceof MalformedStreamException);
        }
    }

}
