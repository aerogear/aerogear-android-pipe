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

import android.support.test.runner.AndroidJUnit4;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.jboss.aerogear.android.pipe.test.util.UnitTestUtils.setPrivateField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.jboss.aerogear.android.pipe.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.pipe.test.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HttpRestProviderTest extends PatchedActivityInstrumentationTestCase {

    private static final URL SIMPLE_URL;
    private static final String HEADER_KEY1_NAME = "KEY1";
    private static final String HEADER_KEY2_NAME = "KEY2";
    private static final byte[] RESPONSE_DATA = "12345".getBytes();/* Not real data */

    private static final String REQUEST_DATA = "12345";/* Not real data */

    private static final Map<String, List<String>> RESPONSE_HEADERS;
    private static final String HEADER_VALUE = "VALUE";

    static {
        try {
            SIMPLE_URL = new URL("http", "localhost", 80, "/");
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpRestProviderTest.class.getName()).log(
                    Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        RESPONSE_HEADERS = new HashMap<String, List<String>>(2);
        RESPONSE_HEADERS.put(HEADER_KEY1_NAME, new ArrayList<String>(1));
        RESPONSE_HEADERS.put(HEADER_KEY2_NAME, new ArrayList<String>(1));
        RESPONSE_HEADERS.get(HEADER_KEY1_NAME).add(HEADER_VALUE);
        RESPONSE_HEADERS.get(HEADER_KEY2_NAME).add(HEADER_VALUE);

    }

    public HttpRestProviderTest() {
        super(MainActivity.class);
    }

    @Test
    public void testGetFailsWith404() throws Exception {
        try {
            HttpURLConnection connection404 = mock(HttpURLConnection.class);

            doReturn(404).when(connection404).getResponseCode();
            when(connection404.getErrorStream()).thenReturn(
                    new ByteArrayInputStream(RESPONSE_DATA));

            HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
            setPrivateField(provider, "connectionPreparer",
                    new HttpUrlConnectionProvider(connection404));

            try {
                provider.get();
            } catch (HttpException exception) {
                for (int i = 0; i < RESPONSE_DATA.length; i++) {
                    assertEquals(RESPONSE_DATA[i], exception.getData()[i]);
                }

                assertEquals(404, exception.getStatusCode());
                throw exception;
            }
        } catch (HttpException exception) {
            return;
        }
        fail("Expected HttpException exception");
    }

    @Test
    public void testGet() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
        setPrivateField(provider, "connectionPreparer",
                new HttpUrlConnectionProvider(connection));

        doReturn(200).when(connection).getResponseCode();
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(RESPONSE_DATA));
        when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);

        HeaderAndBody result = provider.get();
        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], result.getBody()[i]);
        }
        assertNotNull(result.getHeader(HEADER_KEY1_NAME));
        assertNotNull(result.getHeader(HEADER_KEY2_NAME));
        assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));

    }

    @Test
    public void testPost() throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
                RESPONSE_DATA.length);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
        setPrivateField(provider, "connectionPreparer",
                new HttpUrlConnectionProvider(connection));

        doReturn(200).when(connection).getResponseCode();
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(RESPONSE_DATA));
        when(connection.getOutputStream()).thenReturn(outputStream);
        when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);
        doCallRealMethod().when(connection).setRequestMethod(anyString());
        when(connection.getRequestMethod()).thenCallRealMethod();

        HeaderAndBody result = provider.post(REQUEST_DATA);
        assertEquals("POST", connection.getRequestMethod());
        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], result.getBody()[i]);
        }
        assertNotNull(result.getHeader(HEADER_KEY1_NAME));
        assertNotNull(result.getHeader(HEADER_KEY2_NAME));
        assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], outputStream.toByteArray()[i]);
        }

    }

    @Test
    public void testPut() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
                RESPONSE_DATA.length);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        HttpUrlConnectionProvider providerProvider = new HttpUrlConnectionProvider(
                connection);
        final String id = "1";

        doAnswer(new ConnectingAnswer<Integer>(connection, 200)).when(connection).getResponseCode();
        doAnswer(new ConnectingAnswer<ByteArrayInputStream>(connection, new ByteArrayInputStream(RESPONSE_DATA))).when(connection).getInputStream();
        doAnswer(new ConnectingAnswer<ByteArrayOutputStream>(connection, outputStream)).when(connection).getOutputStream();
        doAnswer(new ConnectingAnswer<Map>(connection, RESPONSE_HEADERS)).when(connection).getHeaderFields();
        doCallRealMethod().when(connection).setRequestMethod(anyString());
        when(connection.getRequestMethod()).thenCallRealMethod();

        HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
        setPrivateField(provider, "connectionPreparer", providerProvider);

        HeaderAndBody result = provider.put(id, REQUEST_DATA);
        assertEquals("PUT", connection.getRequestMethod());
        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], result.getBody()[i]);
        }

        assertNotNull(result.getHeader(HEADER_KEY1_NAME));
        assertNotNull(result.getHeader(HEADER_KEY2_NAME));
        assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], outputStream.toByteArray()[i]);
        }

        assertEquals(id, providerProvider.id);
    }

    @Test
    public void testDelete() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        HttpUrlConnectionProvider providerProvider = new HttpUrlConnectionProvider(
                connection);
        final String id = "1";

        doReturn(200).when(connection).getResponseCode();
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(RESPONSE_DATA));
        when(connection.getHeaderFields()).thenReturn(RESPONSE_HEADERS);
        doCallRealMethod().when(connection).setRequestMethod(anyString());
        when(connection.getRequestMethod()).thenCallRealMethod();

        HttpRestProvider provider = new HttpRestProvider(SIMPLE_URL);
        setPrivateField(provider, "connectionPreparer", providerProvider);

        HeaderAndBody result = provider.delete(id);

        for (int i = 0; i < RESPONSE_DATA.length; i++) {
            assertEquals(RESPONSE_DATA[i], result.getBody()[i]);
        }

        assertEquals("DELETE", connection.getRequestMethod());
        assertNotNull(result.getHeader(HEADER_KEY1_NAME));
        assertNotNull(result.getHeader(HEADER_KEY2_NAME));
        assertEquals(HEADER_VALUE, result.getHeader(HEADER_KEY2_NAME));
        assertEquals(id, providerProvider.id);
    }

    static class HttpUrlConnectionProvider
            implements
            Provider<HttpURLConnection> {

        public HttpURLConnection connection;
        public String id;

        public HttpUrlConnectionProvider(HttpURLConnection connection) {
            this.connection = connection;
        }

        @Override
        public HttpURLConnection get(Object... in) {
            if (in != null && in.length > 0) {
                id = (String) in[0];
            }
            return connection;
        }
    }

    private static class ConnectingAnswer<T> implements Answer<T> {

        private final HttpURLConnection real;
        private final T i;

        public ConnectingAnswer(HttpURLConnection real, T i) {
            this.real = real;
            this.i = i;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            setPrivateField(real, "connected", true);
            return i;
        }
    }
}
