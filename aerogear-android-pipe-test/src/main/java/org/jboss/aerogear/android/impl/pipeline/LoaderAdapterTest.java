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
package org.jboss.aerogear.android.impl.pipeline;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.jboss.aerogear.android.pipe.test.MainActivity;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.ObjectVarArgsMatcher;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;
import org.jboss.aerogear.android.impl.pipeline.loader.ReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.RemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.SaveLoader;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.json.JSONObject;
import org.mockito.Mockito;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.pipeline.AbstractFragmentCallback;
import org.jboss.aerogear.android.pipeline.PipeManager;
import org.mockito.Matchers;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class LoaderAdapterTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public LoaderAdapterTest() {
        super(MainActivity.class);
    }

    private static final String TAG = LoaderAdapterTest.class
            .getSimpleName();
    private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
    private URL url;
    private URL listUrl;

    public void setUp() throws MalformedURLException, Exception {
        super.setUp();
        url = new URL("http://server.com/context/");
        listUrl = new URL("http://server.com/context/ListClassId");
    }

    public void testSingleObjectRead() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(listUrl, response);
        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        List<LoaderAdapterTest.ListClassId> result = runRead(adapter);

        List<Point> returnedPoints = result.get(0).points;
        Assert.assertEquals(10, returnedPoints.size());

    }

    public void testReadCallbackFailsWithIncompatibleType() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(listUrl, response);

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        try {
            adapter.read(new AbstractFragmentCallback<List<LoaderAdapterTest.ListClassId>>() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSuccess(List<LoaderAdapterTest.ListClassId> data) {
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        } catch (Exception e) {
            return;
        }
        fail("Incorrect callback should throw exception.");
    }

    public void testSaveCallbackFailsWithIncompatibleType() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(listUrl, response);

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        try {

            adapter.save(new ListClassId(true), new AbstractFragmentCallback<LoaderAdapterTest.ListClassId>() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSuccess(ListClassId data) {

                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        } catch (Exception e) {
            return;
        }
        fail("Incorrect callback should throw exception.");

    }

    public void testDeleteCallbackFailsWithIncompatibleType() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(listUrl, response);

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        try {
            adapter.remove("1", new AbstractFragmentCallback<Void>() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSuccess(Void data) {
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        } catch (Exception e) {
            return;
        }
        fail("Incorrect callback should throw exception.");

    }

    public void testSingleObjectSave() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());

        final HttpStubProvider provider = mock(HttpStubProvider.class);
        when(provider.getUrl()).thenReturn(listUrl);

        when(provider.post((byte[]) anyObject()))
                .thenReturn(new HeaderAndBody(
                        SERIALIZED_POINTS.getBytes(),
                        new HashMap<String, Object>())
                );

        when(provider.put(any(String.class), (byte[]) anyObject()))
                .thenReturn(new HeaderAndBody(
                        SERIALIZED_POINTS.getBytes(),
                        new HashMap<String, Object>())
                );

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        runSave(adapter);

        verify(provider).put(any(String.class), (byte[]) anyObject());

    }

    public void testSingleObjectMultipartSave() throws Exception {

        final HttpStubProvider provider = mock(HttpStubProvider.class);
        when(provider.getUrl()).thenReturn(url);

        when(provider.post((byte[]) anyObject()))
                .thenReturn(new HeaderAndBody(
                        SERIALIZED_POINTS.getBytes(),
                        new HashMap<String, Object>())
                );

        when(provider.put(any(String.class), (byte[]) anyObject()))
                .thenReturn(new HeaderAndBody(
                        SERIALIZED_POINTS.getBytes(),
                        new HashMap<String, Object>())
                );

        RestfulPipeConfiguration config = PipeManager.config("MultiPartData", RestfulPipeConfiguration.class);

        Pipe<MultiPartData> restPipe = config.withUrl(url)
                .requestBuilder(new MultipartRequestBuilder())
                .forClass(MultiPartData.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<MultiPartData> adapter = PipeManager.get(config.getName(), getActivity());

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);

        adapter.save(new MultiPartData(),
                new Callback<MultiPartData>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSuccess(MultiPartData data) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        hasException.set(true);
                        Logger.getLogger(LoaderAdapterTest.class.getSimpleName())
                                .log(Level.SEVERE, e.getMessage(), e);
                        latch.countDown();
                    }
                });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());

        verify(provider).put(any(String.class), (byte[]) anyObject());

    }

    public void testSingleObjectDelete() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());

        final HttpStubProvider provider = mock(HttpStubProvider.class);
        when(provider.getUrl()).thenReturn(listUrl);

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<LoaderAdapterTest.ListClassId> restPipe = config.withUrl(listUrl)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(LoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<LoaderAdapterTest.ListClassId> adapter = PipeManager.get(config.getName(), getActivity());

        runRemove(adapter, "1");

        verify(provider).delete(eq("1"));

    }

    public void testMultipleCallsToLoadCallDeliver() {
        PipeHandler handler = mock(PipeHandler.class);
        final AtomicBoolean called = new AtomicBoolean(false);
        when(handler.onRawReadWithFilter((ReadFilter) any(), (Pipe) any())).thenReturn(new HeaderAndBody(new byte[] {}, new HashMap<String, Object>()));
        ReadLoader loader = new ReadLoader(getActivity(), null, handler, null, null) {

            @Override
            public void deliverResult(Object data) {
                called.set(true);
                return;
            }

            @Override
            public void forceLoad() {
                throw new IllegalStateException("Should not be called");
            }

            @Override
            public void onStartLoading() {
                super.onStartLoading();
            }

        };
        loader.loadInBackground();
        UnitTestUtils.callMethod(loader, "onStartLoading");

        assertTrue(called.get());

    }

    public void testMultipleCallsToSaveCallDeliver() {
        PipeHandler handler = mock(PipeHandler.class);
        final AtomicBoolean called = new AtomicBoolean(false);
        when(handler.onRawSave(Matchers.anyString(), (byte[]) anyObject())).thenReturn(new HeaderAndBody(new byte[] {}, new HashMap<String, Object>()));
        SaveLoader loader = new SaveLoader(getActivity(), null, handler, null, null) {

            @Override
            public void deliverResult(Object data) {
                called.set(true);
                return;
            }

            @Override
            public void forceLoad() {
                throw new IllegalStateException("Should not be called");
            }

            @Override
            public void onStartLoading() {
                super.onStartLoading();
            }

        };
        loader.loadInBackground();
        UnitTestUtils.callMethod(loader, "onStartLoading");

        assertTrue(called.get());

    }

    public void testMultipleCallsToRemoveCallDeliver() {
        PipeHandler handler = mock(PipeHandler.class);
        final AtomicBoolean called = new AtomicBoolean(false);
        when(handler.onRawReadWithFilter((ReadFilter) any(), (Pipe) any())).thenReturn(new HeaderAndBody(new byte[] {}, new HashMap<String, Object>()));

        RemoveLoader loader = new RemoveLoader(getActivity(), null, handler, null) {

            @Override
            public void deliverResult(Object data) {
                called.set(true);
                return;
            }

            @Override
            public void forceLoad() {
                throw new IllegalStateException("Should not be called");
            }

            @Override
            public void onStartLoading() {
                super.onStartLoading();
            }

        };
        loader.loadInBackground();
        UnitTestUtils.callMethod(loader, "onStartLoading");

        assertTrue(called.get());

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
    private <T> List<T> runRead(Pipe<T> restPipe, ReadFilter readFilter)
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<List<T>> resultRef = new AtomicReference<List<T>>();

        restPipe.read(readFilter, new Callback<List<T>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSuccess(List<T> data) {
                resultRef.set(data);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                Logger.getLogger(LoaderAdapterTest.class.getSimpleName())
                        .log(Level.SEVERE, e.getMessage(), e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());

        return resultRef.get();
    }

    /**
     * Runs a remove method, returns the result of the call back and makes sure
     * no exceptions are thrown
     * 
     */
    private <T> void runRemove(Pipe<T> restPipe, String id)
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);

        restPipe.remove(id, new Callback<Void>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSuccess(Void data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                Logger.getLogger(LoaderAdapterTest.class.getSimpleName())
                        .log(Level.SEVERE, e.getMessage(), e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());
    }

    private void runSave(Pipe<ListClassId> restPipe)
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);

        restPipe.save(new ListClassId(true),
                new Callback<ListClassId>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSuccess(ListClassId data) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        hasException.set(true);
                        Logger.getLogger(LoaderAdapterTest.class.getSimpleName())
                                .log(Level.SEVERE, e.getMessage(), e);
                        latch.countDown();
                    }
                });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());
    }

    public void testRunReadWithFilter() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        RestfulPipeConfiguration config = PipeManager.config("data", RestfulPipeConfiguration.class);

        Pipe<Data> pipe = config.withUrl(url)
                .forClass(Data.class);

        Object restRunner = UnitTestUtils.getPrivateField(pipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                factory);

        ReadFilter filter = new ReadFilter();
        filter.setLimit(10);
        filter.setWhere(new JSONObject("{\"model\":\"BMW\"}"));

        LoaderAdapter<Data> adapter = (LoaderAdapter<Data>) PipeManager.get("data", getActivity());

        adapter.read(filter, new Callback<List<Data>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSuccess(List<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE,
                        TAG, e);
            }
        });
        latch.await(60, TimeUnit.SECONDS);

        verify(factory).get(Mockito.argThat(new ObjectVarArgsMatcher(new URL("http://server.com/context?limit=10&model=BMW"), 60000)));

    }

    private static class PointTypeAdapter implements InstanceCreator,
            JsonSerializer, JsonDeserializer {

        @Override
        public Object createInstance(Type type) {
            return new Point();
        }

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc,
                JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("x", ((Point) src).x);
            object.addProperty("y", ((Point) src).y);
            return object;
        }

        @Override
        public Object deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return new Point(json.getAsJsonObject().getAsJsonPrimitive("x")
                    .getAsInt(), json.getAsJsonObject().getAsJsonPrimitive("y")
                    .getAsInt());
        }
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

    public static class MultiPartData {

        private byte[] byteArray = { 'a', 'b', 'c', 'd', 'e', 'f' };
        private InputStream inputStream = new ByteArrayInputStream(byteArray);

        @RecordId
        private String string = "This is a String";

        public byte[] getByteArray() {
            return byteArray;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getString() {
            return string;
        }

        public void setByteArray(byte[] byteArray) {
            this.byteArray = byteArray;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}
