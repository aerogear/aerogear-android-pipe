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
package org.jboss.aerogear.android.impl.pipeline;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.android.pipe.MainFragmentActivity;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportRemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportSaveLoader;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import static org.jboss.aerogear.android.pipeline.LoaderPipe.*;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.impl.util.VoidCallback;
import org.jboss.aerogear.android.pipeline.PipeManager;
import org.jboss.aerogear.android.pipeline.support.AbstractSupportFragmentCallback;
import org.mockito.Matchers;
import static org.mockito.Matchers.anyObject;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings( { "unchecked", "rawtypes" })
public class SupportLoaderAdapterTest extends
        PatchedActivityInstrumentationTestCase<MainFragmentActivity> {

    public SupportLoaderAdapterTest() {
        super(MainFragmentActivity.class);
    }

    private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
    private URL url;

    public void setUp() throws MalformedURLException, Exception {
        super.setUp();
        url = new URL("http://server.com/context/");
    }

    public void testSingleObjectRead() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = config.withUrl(url)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(SupportLoaderAdapterTest.ListClassId.class);
      

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = PipeManager
                .get(config.getName(), getActivity());
        adapter = Mockito.spy(adapter);
        adapter.read(new VoidCallback());

        ArgumentCaptor<Bundle> bundlerCaptor = ArgumentCaptor
                .forClass(Bundle.class);

        verify(
                (LoaderManager.LoaderCallbacks<SupportLoaderAdapterTest.ListClassId>) adapter)
                .onCreateLoader(Mockito.anyInt(), bundlerCaptor.capture());

        Bundle bundle = bundlerCaptor.getValue();
        assertNotNull(bundle.get(CALLBACK));
        assertTrue(bundle.get(CALLBACK) instanceof VoidCallback);
        assertNotNull(bundle.get(METHOD));
        assertTrue(((Enum) bundle.get(METHOD)).name().equals("READ"));
        assertNull(bundle.get(REMOVE_ID));

    }

    public void testReadCallbackFailsWithIncompatibleType() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(
                SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        
        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = config.withUrl(url)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(SupportLoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = PipeManager
                .get(config.getName(), getActivity());
        try {
            adapter.read(new AbstractSupportFragmentCallback<List<SupportLoaderAdapterTest.ListClassId>>() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSuccess(List<SupportLoaderAdapterTest.ListClassId> data) {
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
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        
        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = config.withUrl(url)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(SupportLoaderAdapterTest.ListClassId.class);

        
        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = PipeManager
                .get(config.getName(), getActivity());
        try {
            adapter.save(new ListClassId(true), new AbstractSupportFragmentCallback<SupportLoaderAdapterTest.ListClassId>() {
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
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        
        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = config.withUrl(url)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(SupportLoaderAdapterTest.ListClassId.class);


        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = PipeManager
                .get(config.getName(), getActivity());
        try {
            adapter.remove("1", new AbstractSupportFragmentCallback<Void>() {
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

    public void testSingleObjectDelete() throws Exception {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
                Point.class, new PointTypeAdapter());

        final HttpStubProvider provider = mock(HttpStubProvider.class);
        when(provider.getUrl()).thenReturn(url);

        RestfulPipeConfiguration config = PipeManager.config("ListClassId", RestfulPipeConfiguration.class);

        Pipe<SupportLoaderAdapterTest.ListClassId> restPipe = config.withUrl(url)
                .requestBuilder(new GsonRequestBuilder(builder.create()))
                .forClass(SupportLoaderAdapterTest.ListClassId.class);

        Object restRunner = UnitTestUtils.getPrivateField(restPipe,
                "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return provider;
                    }
                });

        LoaderPipe<SupportLoaderAdapterTest.ListClassId> adapter = PipeManager
                .get(config.getName(), getActivity());
        adapter = Mockito.spy(adapter);
        ArgumentCaptor<Bundle> bundlerCaptor = ArgumentCaptor
                .forClass(Bundle.class);

        adapter.remove("1", new VoidCallback());
        verify(
                (LoaderManager.LoaderCallbacks<SupportLoaderAdapterTest.ListClassId>) adapter)
                .onCreateLoader(Mockito.anyInt(), bundlerCaptor.capture());

        Bundle bundle = bundlerCaptor.getValue();
        assertNotNull(bundle.get(CALLBACK));
        assertTrue(bundle.get(CALLBACK) instanceof VoidCallback);
        assertNotNull(bundle.get(METHOD));
        assertTrue(((Enum) bundle.get(METHOD)).name().equals("REMOVE"));
        assertNotNull(bundle.get(REMOVE_ID));
        assertEquals("1", bundle.get(REMOVE_ID));

    }

    public void testMultipleCallsToLoadCallDeliver() {
        PipeHandler handler = mock(PipeHandler.class);
        final AtomicBoolean called = new AtomicBoolean(false);
        when(handler.onRawReadWithFilter((ReadFilter) any(), (Pipe) any())).thenReturn(new HeaderAndBody(new byte[] {}, new HashMap<String, Object>()));
        SupportReadLoader loader = new SupportReadLoader(getActivity(), null,
                handler, null, null) {
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
        SupportSaveLoader loader = new SupportSaveLoader(getActivity(), null,
                handler, null, null) {

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
        when(handler.onRawReadWithFilter((ReadFilter) any(), (Pipe) any()))
                .thenReturn(new HeaderAndBody("[]".getBytes(), new HashMap<String, Object>()));
        SupportRemoveLoader loader = new SupportRemoveLoader(getActivity(),
                null, handler, null) {
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

}
