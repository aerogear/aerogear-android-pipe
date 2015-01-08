/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipe.test.http;

import java.util.HashMap;

import android.test.AndroidTestCase;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;

public class HttpHelperTest extends AndroidTestCase {
    private static final byte[] SIMPLE_DATA = { 8, 6, 7, 5, 3, 0, 9 };
    private static final String SAMPLE_MESSAGE = "SAMPLE_MESSAGE";
    private static final String SAMPLE_HEADER = "SAMPLE_HEADER";
    private static final String DEFAULT_MESSAGE = "The server returned the error code 404.";
    private static final int NOT_FOUND = 404;

    public void testHttpExceptionConstructor() {
        HttpException exception = new HttpException(SIMPLE_DATA, NOT_FOUND);
        HttpException exceptionWithMessage = new HttpException(SIMPLE_DATA,
                NOT_FOUND, SAMPLE_MESSAGE);

        assertNotSame(SIMPLE_DATA, exception.getData());
        for (int i = 0; i < SIMPLE_DATA.length; i++) {
            assertEquals(SIMPLE_DATA[i], exception.getData()[i]);
        }
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals(DEFAULT_MESSAGE, exception.getMessage());
        assertEquals(SAMPLE_MESSAGE, exceptionWithMessage.getMessage());

    }

    public void testHeaderAndBody() {
        HeaderAndBody headerAndBody = new HeaderAndBody(SIMPLE_DATA,
                new HashMap<String, Object>());
        headerAndBody.setHeader(SAMPLE_HEADER, SAMPLE_MESSAGE);
        assertEquals(SAMPLE_MESSAGE, headerAndBody.getHeader(SAMPLE_HEADER));
    }

}
