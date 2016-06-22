/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.pipe.test.rest;

import android.graphics.Point;
import android.support.test.runner.AndroidJUnit4;
import com.google.gson.*;
import junit.framework.Assert;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.core.RecordId;
import org.jboss.aerogear.android.pipe.MarshallingConfig;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeManager;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpProviderFactory;
import org.jboss.aerogear.android.pipe.module.ModuleFields;
import org.jboss.aerogear.android.pipe.module.PipeModule;
import org.jboss.aerogear.android.pipe.paging.PageConfig;
import org.jboss.aerogear.android.pipe.paging.PagedList;
import org.jboss.aerogear.android.pipe.paging.WrappingPagedList;
import org.jboss.aerogear.android.pipe.rest.RestAdapter;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfiguration;
import org.jboss.aerogear.android.pipe.rest.gson.GsonRequestBuilder;
import org.jboss.aerogear.android.pipe.rest.gson.GsonResponseParser;
import org.jboss.aerogear.android.pipe.test.helper.Data;
import org.jboss.aerogear.android.pipe.test.http.HttpStubProvider;
import org.jboss.aerogear.android.pipe.test.util.ObjectVarArgsMatcher;
import org.jboss.aerogear.android.pipe.test.util.UnitTestUtils;
import org.jboss.aerogear.android.pipe.util.UrlUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class RestAdapterTest {

    private static final String TAG = RestAdapterTest.class.getSimpleName();
    private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
    private static final String POINTS_ARRAY = "[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}]";
    private URL url;
    private final Provider<HttpProvider> stubHttpProviderFactory = new Provider<HttpProvider>() {
        @Override
        public HttpProvider get(Object... in) {
            return new HttpStubProvider((URL) in[0]);
        }
    };

    @Before
    public void setUp() throws MalformedURLException, Exception {
        url = new URL("http://server.com/context/");
    }

    @Test
    public void testPipeURLProperty() throws Exception {
        Pipe<Data> restPipe = new RestAdapter<Data>(Data.class, url);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", stubHttpProviderFactory);
        assertEquals("verifying the given URL", "http://server.com/context/", restPipe.getUrl().toString());
    }

    @Test
    public void testPipeFactoryPipeConfigEncoding() {
        try {
            RestfulPipeConfiguration config = PipeManager.config("data", RestfulPipeConfiguration.class).withUrl(url);
            config.getRequestBuilder().getMarshallingConfig().setEncoding(Charset.forName("UTF-16"));
            assertEquals(Charset.forName("UTF-16"), config.getRequestBuilder().getMarshallingConfig().getEncoding());
            config.getRequestBuilder().getMarshallingConfig().setEncoding((Charset) null);
        } catch (IllegalArgumentException ignore) {
            return;
        }
        fail("Expected IllegalArgumentException");
    }

    @Test
    public void testPipeFactoryPipeConfigGson() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        GsonResponseParser<ListClassId> responseParser = new GsonResponseParser<ListClassId>(builder.create());

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class)
                .withUrl(url)
                .responseParser(responseParser);

        Pipe<ListClassId> restPipe = config.forClass(ListClassId.class);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        Field gsonField = restRunner.getClass().getDeclaredField("responseParser");
        gsonField.setAccessible(true);
        GsonResponseParser gson = (GsonResponseParser) gsonField.get(restRunner);

        assertEquals(responseParser, gson);

    }

    @Test
    public void testEncoding() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        final Charset utf_16 = Charset.forName("UTF-16");

        final HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(utf_16), new HashMap<String, Object>()));

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).requestBuilder(new GsonRequestBuilder(builder.create()));

        MarshallingConfig marshallingConfig = config.getRequestBuilder().getMarshallingConfig();
        marshallingConfig.setEncoding(utf_16);
        marshallingConfig = config.getResponseParser().getMarshallingConfig();
        marshallingConfig.setEncoding(utf_16);

        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, config);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });

        runRead(restPipe);

    }

    @Test
    public void testConfigSetEncoding() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new RestAdapterTest.PointTypeAdapter());
        final Charset utf_16 = Charset.forName("UTF-16");

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class);
        config.withUrl(url).requestBuilder(new GsonRequestBuilder(builder.create()))
                .getRequestBuilder().getMarshallingConfig().setEncoding(utf_16);

        RestAdapter<ListClassId> restPipe = (RestAdapter<ListClassId>) config.forClass(ListClassId.class);

        assertEquals(utf_16, restPipe.getRequestBuilder().getMarshallingConfig().getEncoding());

    }

    @Test
    public void testSingleObjectRead() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.requestBuilder(new GsonRequestBuilder(builder.create()));

        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        List<ListClassId> result = runRead(restPipe);

        List<Point> returnedPoints = result.get(0).points;
        assertEquals(10, returnedPoints.size());

    }

    @Test
    public void testSingleObjectReadWithNestedResult() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(("{\"result\":{\"points\":" + SERIALIZED_POINTS + "}}").getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.requestBuilder(new GsonRequestBuilder(builder.create()));
        config.getResponseParser().getMarshallingConfig().setDataRoot("result.points");

        RestAdapter<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, config);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        List<ListClassId> result = runRead(restPipe);

        List<Point> returnedPoints = result.get(0).points;
        assertEquals(10, returnedPoints.size());

    }

    @Test
    public void testReadArray() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody((POINTS_ARRAY).getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.requestBuilder(new GsonRequestBuilder(builder.create()));
        config.getRequestBuilder().getMarshallingConfig().setDataRoot("");

        RestAdapter<Point> restPipe = new RestAdapter<Point>(Point.class, url, config);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        List<Point> result = runRead(restPipe);

        assertEquals(10, result.size());

    }

    @Test
    public void testGsonBuilderProperty() throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        final ByteArrayOutputStream request = new ByteArrayOutputStream();

        final HttpStubProvider provider = new HttpStubProvider(url) {
            @Override
            public HeaderAndBody put(String id, byte[] data) {
                try {
                    request.write(data);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return new HeaderAndBody(data, new HashMap<String, Object>());
            }

            @Override
            public HeaderAndBody post(byte[] data) {
                try {
                    request.write(data);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return new HeaderAndBody(data, new HashMap<String, Object>());
            }
        };

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.responseParser(new GsonResponseParser(builder.create()));

        Pipe<ListClassId> restPipe = new RestAdapter<ListClassId>(ListClassId.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        final ListClassId listClass = new ListClassId(true);
        final List<Point> returnedPoints = new ArrayList<Point>(10);

        restPipe.save(listClass, new Callback<ListClassId>() {
            @Override
            public void onSuccess(ListClassId data) {
                returnedPoints.addAll(data.points);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });

        latch.await(2, TimeUnit.SECONDS);

        int expectedId = new JSONObject(SERIALIZED_POINTS).getInt("id");
        JSONArray expectedPoints = new JSONObject(SERIALIZED_POINTS).getJSONArray("points");
        int requestId = new JSONObject(new String(request.toByteArray())).getInt("id");
        JSONArray requestPoints = new JSONObject(new String(request.toByteArray())).getJSONArray("points");
        assertEquals(expectedId, requestId);
        assertEquals(expectedPoints.toString(), requestPoints.toString());
        assertEquals(listClass.points, returnedPoints);
    }

    public void runReadWithFilterUsingUri() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, url);
        Object restRunner = UnitTestUtils.getPrivateField(adapter, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);

        ReadFilter filter = new ReadFilter();
        filter.setLinkUri(URI.create("rail%2Ftrails?limit=10&where=%7B%22model%22:%22BMW%22%7D"));

        adapter.read(filter, new Callback<List<Data>>() {
            @Override
            public void onSuccess(List<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        verify(factory).get(eq(new URL(url.toString() + "rail%2Ftrails?limit=10&where=%7B%22model%22:%22BMW%22%7D")));
    }

    public void runReadWithFilter() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        ModuleFields authFields = new ModuleFields();

        PipeModule urlModule = mock(PipeModule.class);

        when(urlModule.loadModule((URI) anyObject(), (String) anyObject(), (byte[]) anyObject())).thenReturn(authFields);

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.module(urlModule);

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(adapter, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);

        ReadFilter filter = new ReadFilter();
        filter.setLimit(10);
        filter.setWhere(new JSONObject("{\"model\":\"BMW\"}"));

        adapter.read(filter, new Callback<List<Data>>() {
            @Override
            public void onSuccess(List<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, TAG, e);
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        verify(factory).get(new URL(url.toString() + "?limit=10&model=BMW"));
    }

    /**
     * This test tests the default paging configuration.
     *
     * @throws java.lang.NoSuchFieldException If this is thrown then the test is
     * broken.
     * @throws java.lang.IllegalAccessException If this is thrown then the test
     * is broken.
     * @throws java.lang.InterruptedException If this is thrown then the test is
     * broken.
     */
    @Test
    public void testLinkPagingReturnsData() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException {

        final HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>()));

        PageConfig pageConfig = new PageConfig();
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        RestfulPipeConfiguration pipeConfig = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        pipeConfig.requestBuilder(new GsonRequestBuilder(builder.create()));
        pipeConfig.pageConfig(pageConfig);

        Pipe<ListClassId> dataPipe = pipeConfig.forClass(ListClassId.class);
        Object restRunner = UnitTestUtils.getPrivateField(dataPipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });

        ReadFilter onePageFilter = new ReadFilter();

        onePageFilter.setLimit(1);
        runRead(dataPipe, onePageFilter);
        List<ListClassId> result = runRead(dataPipe, onePageFilter);

        assertNotNull(result);
        assertFalse(result instanceof PagedList);

    }

    /**
     * This test tests the default paging configuration.
     *
     *
     * @throws java.lang.InterruptedException this should not be thrown
     * @throws java.net.URISyntaxException this should not be thrown
     * @throws java.lang.NoSuchFieldException this should not be thrown
     * @throws java.lang.IllegalAccessException this should not be thrown
     */
    @Test
    public void testDefaultPaging() throws InterruptedException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            URISyntaxException {

        PageConfig pageConfig = new PageConfig();
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new RestAdapterTest.PointTypeAdapter());

        RestfulPipeConfiguration pipeConfig = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        pipeConfig.requestBuilder(new GsonRequestBuilder(builder.create()));
        pipeConfig.pageConfig(pageConfig);

        Pipe<ListClassId> dataPipe = pipeConfig.forClass(ListClassId.class);
        Object restRunner = UnitTestUtils.getPrivateField(dataPipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                HashMap<String, Object> headers = new HashMap<String, Object>(1);
                headers
                        .put(
                                "Link",
                                "<http://example.com/TheBook/chapter2>; rel=\"previous\";title=\"previous chapter\",<http://example.com/TheBook/chapter3>; rel=\"next\";title=\"next chapter\"");
                HttpStubProvider provider = new HttpStubProvider(url, new HeaderAndBody(SERIALIZED_POINTS.getBytes(), headers));

                return provider;
            }
        });

        ReadFilter onePageFilter = new ReadFilter();
        onePageFilter.setLimit(1);
        List<ListClassId> resultList = runRead(dataPipe, onePageFilter);
        assertTrue(resultList instanceof PagedList);
        WrappingPagedList<ListClassId> pagedList = (WrappingPagedList<ListClassId>) resultList;

        assertEquals(new URI("http://example.com/TheBook/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://example.com/TheBook/chapter2"), pagedList.getPreviousFilter().getLinkUri());
    }

    @Test
    public void testBuildPagedResultsFromHeaders() throws Exception {
        PageConfig pageConfig = new PageConfig();
        pageConfig.setMetadataLocation(PageConfig.MetadataLocations.HEADERS);

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.pageConfig(pageConfig);

        RestAdapter adapter = new RestAdapter(Data.class, url, config);
        List<Data> list = new ArrayList<Data>();
        HeaderAndBody response = new HeaderAndBody(new byte[]{}, new HashMap<String, Object>() {
            {
                put("next", "chapter3");
                put("previous", "chapter2");
            }
        });
        JSONObject where = new JSONObject();

        Method method = adapter.getClass().getDeclaredMethod("computePagedList", List.class, HeaderAndBody.class, JSONObject.class, Pipe.class);
        method.setAccessible(true);

        WrappingPagedList<Data> pagedList = (WrappingPagedList<Data>) method.invoke(adapter, list, response, where, adapter);
        assertEquals(new URI("http://server.com/context/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://server.com/context/chapter2"), pagedList.getPreviousFilter().getLinkUri());

    }

    @Test
    public void testBuildPagedResultsFromBody() throws Exception {
        PageConfig pageConfig = new PageConfig();
        pageConfig.setMetadataLocation(PageConfig.MetadataLocations.BODY);
        pageConfig.setNextIdentifier("pages.next");
        pageConfig.setPreviousIdentifier("pages.previous");

        RestfulPipeConfiguration config = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(url);
        config.pageConfig(pageConfig);

        RestAdapter adapter = (RestAdapter) config.forClass(Data.class);

        List<Data> list = new ArrayList<Data>();
        HeaderAndBody response = new HeaderAndBody("{\"pages\":{\"next\":\"chapter3\",\"previous\":\"chapter2\"}}".getBytes(), new HashMap<String, Object>());
        JSONObject where = new JSONObject();
        Method method = adapter.getClass().getDeclaredMethod("computePagedList", List.class, HeaderAndBody.class, JSONObject.class, Pipe.class);
        method.setAccessible(true);

        WrappingPagedList<Data> pagedList = (WrappingPagedList<Data>) method.invoke(adapter, list, response, where, adapter);
        assertEquals(new URI("http://server.com/context/chapter3"), pagedList.getNextFilter().getLinkUri());
        assertEquals(new URI("http://server.com/context/chapter2"), pagedList.getPreviousFilter().getLinkUri());

    }

    @Test
    public void testRunTimeout() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        URL aerogearserverUrl = new URL("https://redhat.com/");
        RestfulPipeConfiguration pipeConfig = PipeManager.config("listClassId", RestfulPipeConfiguration.class).withUrl(aerogearserverUrl);
        pipeConfig.timeout(1);
        RestAdapter<Data> adapter = (RestAdapter<Data>) pipeConfig.forClass(Data.class);
        final AtomicBoolean onFailCalled = new AtomicBoolean(false);
        final AtomicReference<Exception> exceptionReference = new AtomicReference<Exception>();
        adapter.read(new Callback<List<Data>>() {
            @Override
            public void onSuccess(List<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                onFailCalled.set(true);
                exceptionReference.set(e);
                latch.countDown();
            }
        });
        latch.await(50000, TimeUnit.MILLISECONDS);
        assertTrue(onFailCalled.get());
        assertEquals(SocketTimeoutException.class, exceptionReference.get().getCause().getClass());
    }

    @Test
    public void testReadByIdURL() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        RestfulPipeConfiguration config = PipeManager.config("whatever", RestfulPipeConfiguration.class)
                .withUrl(url);

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(adapter, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);

        adapter.read(String.valueOf(1), new Callback<Data>() {
            @Override
            public void onSuccess(Data data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, TAG, e);
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        verify(factory).get(Mockito.argThat(new ObjectVarArgsMatcher(UrlUtils.appendToBaseURL(url, "1"), 60000)));
    }

    private <T> List<T> runRead(Pipe<T> restPipe) throws InterruptedException {
        return runRead(restPipe, null);
    }

    /**
     * Runs a read method, returns the result of the call back and makes sure no
     * exceptions are thrown
     *
     * @param restPipe
     */
    private <T> List<T> runRead(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<List<T>> resultRef = new AtomicReference<List<T>>();

        restPipe.read(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                resultRef.set(data);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                Logger.getLogger(RestAdapterTest.class.getSimpleName()).log(Level.SEVERE, e.getMessage(), e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());

        return resultRef.get();
    }

    /**
     * Runs a read method, returns the result of the call back and rethrows the
     * underlying exception
     *
     * @param restPipe
     */
    private <T> List<T> runReadForException(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException, Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<Exception> exceptionref = new AtomicReference<Exception>();
        restPipe.read(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                exceptionref.set(e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(hasException.get());

        throw exceptionref.get();
    }

    public final static class ListClassId {

        List<Point> points = new ArrayList<Point>(10);
        @RecordId
        String id = "1";

        public ListClassId(boolean build) {
            if (build) {
                for (int i = 0; i < 10; i++) {
                    points.add(new Point(i, i * 2));
                }
            }
        }

        public ListClassId() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return points.equals(((ListClassId) obj).points);
            } catch (Throwable ignore) {
                return false;
            }
        }
    }

    private static class PointTypeAdapter implements InstanceCreator, JsonSerializer, JsonDeserializer {

        @Override
        public Object createInstance(Type type) {
            return new Point();
        }

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("x", ((Point) src).x);
            object.addProperty("y", ((Point) src).y);
            return object;
        }

        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Point(json.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(),
                    json.getAsJsonObject().getAsJsonPrimitive("y").getAsInt());
        }
    }
}
