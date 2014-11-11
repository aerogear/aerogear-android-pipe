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

import java.net.URL;
import java.util.List;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.impl.pipeline.loader.AbstractPipeLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.RemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.SaveLoader;
import org.jboss.aerogear.android.pipeline.AbstractActivityCallback;
import org.jboss.aerogear.android.pipeline.AbstractFragmentCallback;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.RequestBuilder;
import org.jboss.aerogear.android.pipeline.ResponseParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.google.common.collect.Multimap;
import java.util.Arrays;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.jboss.aerogear.android.pipeline.support.AbstractFragmentActivityCallback;
import org.jboss.aerogear.android.pipeline.support.AbstractSupportFragmentCallback;

/**
 * This class wraps a Pipe in an asynchronous Loader.
 * 
 * This classes uses Loaders from android.conent. It will not work on pre
 * Honeycomb devices. If you do need to support Android devices &lt; version
 * 3.0, consider using {@link SupportLoaderAdapter}
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressWarnings( { "rawtypes", "unchecked" })
public class LoaderAdapter<T> implements LoaderPipe<T>,
        LoaderManager.LoaderCallbacks<HeaderAndBody> {

    private static final String TAG = LoaderAdapter.class.getSimpleName();
    private final Handler handler;
    private Multimap<String, Integer> idsForNamedPipes;

    private static enum Methods {

        READ, SAVE, REMOVE
    }

    private final Context applicationContext;
    private Fragment fragment;
    private Activity activity;
    private final Pipe<T> pipe;
    private final LoaderManager manager;
    private final String name;
    private final RequestBuilder<T> requestBuilder;
    private final ResponseParser<T> responseParser;

    public LoaderAdapter(Activity activity, Pipe<T> pipe,
            String name) {
        this.pipe = pipe;
        this.requestBuilder = pipe.getRequestBuilder();
        this.responseParser = pipe.getResponseParser();
        this.manager = activity.getLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.activity = activity;
    }

    public LoaderAdapter(Fragment fragment, Context applicationContext,
            Pipe<T> pipe, String name) {
        this.pipe = pipe;
        this.manager = fragment.getLoaderManager();
        this.requestBuilder = pipe.getRequestBuilder();
        this.responseParser = pipe.getResponseParser();
        this.applicationContext = applicationContext;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.fragment = fragment;
    }

    @Override
    public PipeType getType() {
        return pipe.getType();
    }

    @Override
    public URL getUrl() {
        return pipe.getUrl();
    }

    @Override
    public void read(Callback<List<T>> callback) {
        int id = Arrays.hashCode(new Object[]{name, callback});
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, null);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void read(ReadFilter filter, Callback<List<T>> callback) {
        int id = Arrays.hashCode(new Object[]{name, filter, callback});
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, filter);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void save(T item, Callback<T> callback) {
        int id = Arrays.hashCode(new Object[]{name, item, callback});
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(ITEM, requestBuilder.getBody(item));
        bundle.putString(SAVE_ID, Scan.findIdValueIn(item));
        bundle.putSerializable(METHOD, Methods.SAVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void remove(String toRemoveId, Callback<Void> callback) {
        int id = Arrays.hashCode(new Object[]{name, toRemoveId, callback});
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(REMOVE_ID, toRemoveId);
        bundle.putSerializable(METHOD, Methods.REMOVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public PipeHandler<T> getHandler() {
        return pipe.getHandler();
    }

    @Override
    public RequestBuilder<T> getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public ResponseParser<T> getResponseParser() {
        return responseParser;
    }

    @Override
    public Class<T> getKlass() {
        return pipe.getKlass();
    }

    @Override
    public Loader<HeaderAndBody> onCreateLoader(int id, Bundle bundle) {
        this.idsForNamedPipes.put(name, id);
        Methods method = (Methods) bundle.get(METHOD);
        Callback callback = (Callback) bundle.get(CALLBACK);
        verifyCallback(callback);
        AbstractPipeLoader loader = null;
        switch (method) {
        case READ: {
            ReadFilter filter = (ReadFilter) bundle.get(FILTER);
            loader = new ReadLoader(applicationContext, callback,
                    pipe.getHandler(), filter, this);
        }
            break;
        case REMOVE: {
            String toRemove = bundle.getString(REMOVE_ID, "-1");
            loader = new RemoveLoader(applicationContext, callback,
                    pipe.getHandler(), toRemove);
        }
            break;
        case SAVE: {
            byte[] data = bundle.getByteArray(ITEM);
            String dataId = bundle.getString(SAVE_ID);
            loader = new SaveLoader(applicationContext, callback,
                    pipe.getHandler(), data, dataId);
        }
            break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<HeaderAndBody> loader, final HeaderAndBody data) {
        if (!(loader instanceof AbstractPipeLoader)) {
            Log.e(TAG,
                    "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException(
                    "Adapter is listening to loaders which it doesn't support");
        } else {
            final AbstractPipeLoader<HeaderAndBody> modernLoader = (AbstractPipeLoader<HeaderAndBody>) loader;
            Object object = null;
            if (!modernLoader.hasException() && data != null && data.getBody() != null) {
                object = extractObject(data, modernLoader);
            }

            handler.post(new CallbackHandler<T>(this, modernLoader, object));
        }
    }

    @Override
    public void onLoaderReset(Loader<HeaderAndBody> loader) {
        Log.e(TAG, loader.toString());

    }

    @Override
    public void reset() {
        for (Integer id : idsForNamedPipes.get(name)) {
            Loader loader = manager.getLoader(id);
            if (loader != null) {
                manager.destroyLoader(id);
            }
        }
        idsForNamedPipes.removeAll(name);
    }

    @Override
    public void setLoaderIds(Multimap<String, Integer> idsForNamedPipes) {
        this.idsForNamedPipes = idsForNamedPipes;
    }

    private void fragmentSuccess(Callback typelessCallback, Object data) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback typelessCallback,
            Exception exception) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onFailure(exception);
        callback.setFragment(null);
    }

    private void activitySuccess(Callback typelessCallback, Object data) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onSuccess(data);
        callback.setActivity(null);
    }

    private void activityFailure(Callback typelessCallback,
            Exception exception) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onFailure(exception);
        callback.setActivity(null);
    }

    private Object extractObject(HeaderAndBody data, AbstractPipeLoader<HeaderAndBody> modernLoader) {
        List results = responseParser.handleResponse(data, getKlass());

        if (results == null || results.size() == 0) {
            return results;
        } else if (modernLoader instanceof SaveLoader) {
            return results.get(0);
        } else {
            return results;
        }

    }

    static class CallbackHandler<T> implements Runnable {

        private final LoaderAdapter<T> adapter;
        private final AbstractPipeLoader<T> modernLoader;
        private final Object data;

        public CallbackHandler(LoaderAdapter<T> adapter,
                AbstractPipeLoader loader, Object data) {
            super();
            this.adapter = adapter;
            this.modernLoader = loader;
            this.data = data;
        }

        @Override
        public void run() {
            if (modernLoader.hasException()) {
                final Exception exception = modernLoader.getException();
                Log.e(TAG, exception.getMessage(), exception);
                if (modernLoader.getCallback() instanceof AbstractFragmentCallback) {
                    adapter.fragmentFailure(modernLoader.getCallback(),
                            exception);
                } else if (modernLoader.getCallback() instanceof AbstractActivityCallback) {
                    adapter.activityFailure(modernLoader.getCallback(),
                            exception);
                } else {
                    modernLoader.getCallback().onFailure(exception);
                }

            } else {

                if (modernLoader.getCallback() instanceof AbstractFragmentCallback) {
                    adapter.fragmentSuccess(modernLoader.getCallback(), data);
                } else if (modernLoader.getCallback() instanceof AbstractActivityCallback) {
                    adapter.activitySuccess(modernLoader.getCallback(), data);
                } else {
                    modernLoader.getCallback().onSuccess((T) data);
                }
            }

        }
    }

    private void verifyCallback(Callback<List<T>> callback) {
        if (callback instanceof AbstractActivityCallback) {
            if (activity == null) {
                throw new IllegalStateException("An AbstractActivityCallback was supplied, but there is no Activity.");
            }
        } else if (callback instanceof AbstractFragmentCallback) {
            if (fragment == null) {
                throw new IllegalStateException("An AbstractFragmentCallback was supplied, but there is no Fragment.");
            }
        } else if (callback instanceof AbstractFragmentActivityCallback) {
            throw new IllegalStateException("An AbstractFragmentActivityCallback was supplied, but this is the modern Loader.");
        } else if (callback instanceof AbstractSupportFragmentCallback) {
            throw new IllegalStateException("An AbstractSupportFragmentCallback was supplied, but this is the modern Loader.");
        }
    }

}
