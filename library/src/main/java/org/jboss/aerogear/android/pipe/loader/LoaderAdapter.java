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
package org.jboss.aerogear.android.pipe.loader;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.HashMap;

import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.pipe.callback.AbstractActivityCallback;
import org.jboss.aerogear.android.pipe.callback.AbstractFragmentCallback;
import org.jboss.aerogear.android.pipe.LoaderPipe;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeHandler;
import org.jboss.aerogear.android.pipe.RequestBuilder;
import org.jboss.aerogear.android.pipe.ResponseParser;

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
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Map;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.core.reflection.Scan;
import org.jboss.aerogear.android.pipe.callback.AbstractSupportFragmentCallback;

/**
 * This class wraps a Pipe in an asynchronous Loader.
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoaderAdapter<T> implements LoaderPipe<T>,
        LoaderManager.LoaderCallbacks<HeaderAndBody> {

    private static final String TAG = LoaderAdapter.class.getSimpleName();
    private final Handler handler;
    private Map<String, List<Integer>> idsForNamedPipes;

    private enum Methods {

        READ, READ_ID, SAVE, REMOVE
    }

    private final Context applicationContext;
    private Fragment fragment;
    private Activity activity;
    private android.support.v4.app.Fragment supportFragment;
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
    
        public LoaderAdapter(android.support.v4.app.Fragment supportFragment, Context applicationContext,
            Pipe<T> pipe, String name) {
        this.pipe = pipe;
        this.manager = supportFragment.getActivity().getLoaderManager();
        this.requestBuilder = pipe.getRequestBuilder();
        this.responseParser = pipe.getResponseParser();
        this.applicationContext = applicationContext;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.supportFragment = supportFragment;
    }

    @Override
    public URL getUrl() {
        return pipe.getUrl();
    }

    @Override
    public void read(String idx, Callback<T> callback) {
        ReadFilter filter = new ReadFilter();
        filter.setLinkUri(URI.create(idx));

        int id = Arrays.hashCode(new Object[] { name, filter, callback });
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, filter);
        bundle.putSerializable(METHOD, Methods.READ_ID);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void read(Callback<List<T>> callback) {
        int id = Arrays.hashCode(new Object[] { name, callback });
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, null);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void read(ReadFilter filter, Callback<List<T>> callback) {
        int id = Arrays.hashCode(new Object[] { name, filter, callback });
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, filter);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void save(T item, Callback<T> callback) {
        int id = Arrays.hashCode(new Object[] { name, item, callback });
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(ITEM, requestBuilder.getBody(item));
        bundle.putString(SAVE_ID, Scan.findIdValueIn(item));
        bundle.putSerializable(METHOD, Methods.SAVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void remove(String toRemoveId, Callback<Void> callback) {
        int id = Arrays.hashCode(new Object[] { name, toRemoveId, callback });
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
        addId(name, id);

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
            case READ_ID: {
                ReadFilter filter = (ReadFilter) bundle.get(FILTER);
                loader = new IdReadLoader(applicationContext, callback,
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
        if (idsForNamedPipes == null) {
            idsForNamedPipes = new HashMap<String, List<Integer>>();
        }

        List<Integer> ids = idsForNamedPipes.get(name);
        if (ids != null) {
            for (Integer id : ids) {
                if (id != null) {
                    Loader loader = manager.getLoader(id);
                    if (loader != null) {
                        manager.destroyLoader(id);
                    }
                }
            }
        }

        idsForNamedPipes.put(name, new ArrayList<Integer>());
    }

    @Override
    public void setLoaderIds(Map<String, List<Integer>> idsForNamedPipes) {
        this.idsForNamedPipes = idsForNamedPipes;
    }

    private void fragmentSuccess(Callback typelessCallback, Object data) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback typelessCallback, Exception exception) {
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

    private void activityFailure(Callback typelessCallback, Exception exception) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onFailure(exception);
        callback.setActivity(null);
    }
    
    private void supportFragmentSuccess(Callback typelessCallback, Object data) {
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setSupportFragment(supportFragment);
        callback.onSuccess(data);
        callback.setSupportFragment(null);
    }

    private void supportFragmentFailure(Callback typelessCallback, Exception exception) {
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setSupportFragment(supportFragment);
        callback.onFailure(exception);
        callback.setSupportFragment(null);
    }

    private Object extractObject(HeaderAndBody data, AbstractPipeLoader<HeaderAndBody> modernLoader) {
        List results = responseParser.handleResponse(data, getKlass());

        if (results == null || results.size() == 0) {
            return results;
        } else if ((modernLoader instanceof SaveLoader) || (modernLoader instanceof IdReadLoader)) {
            return results.get(0);
        } else {
            return results;
        }

    }

    public static class CallbackHandler<T> implements Runnable {

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
                    adapter.fragmentFailure(modernLoader.getCallback(),exception);
                } else if (modernLoader.getCallback() instanceof AbstractSupportFragmentCallback){
                    adapter.supportFragmentFailure(modernLoader.getCallback(),exception);
                } else if (modernLoader.getCallback() instanceof AbstractActivityCallback) {
                    adapter.activityFailure(modernLoader.getCallback(),
                            exception);
                } else {
                    modernLoader.getCallback().onFailure(exception);
                }

            } else {

                if (modernLoader.getCallback() instanceof AbstractFragmentCallback) {
                    adapter.fragmentSuccess(modernLoader.getCallback(), data);
                } else if (modernLoader.getCallback() instanceof AbstractSupportFragmentCallback) {
                    adapter.supportFragmentSuccess(modernLoader.getCallback(), data);
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
        } else if (callback instanceof AbstractSupportFragmentCallback) {
            if (supportFragment == null) {
                throw new IllegalStateException("An AbstractSupportFragmentCallback was supplied, but there is no Fragment.");
            }
        }
    }

    private synchronized void addId(String name, int id) {
        List<Integer> ids = this.idsForNamedPipes.get(name);
        if (ids == null) {
            this.idsForNamedPipes.put(name, (ids = new ArrayList<Integer>()));
        }
        ids.add(id);
    }

}
