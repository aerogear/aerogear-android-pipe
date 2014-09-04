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
package org.jboss.aerogear.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.aerogear.android.impl.pipeline.DefaultPipeFactory;
import org.jboss.aerogear.android.impl.pipeline.LoaderAdapter;
import org.jboss.aerogear.android.impl.pipeline.PipeConfig;
import org.jboss.aerogear.android.impl.pipeline.SupportLoaderAdapter;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeFactory;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A {@link Pipeline} represents a ‘collection’ of server connections (aka
 * {@link Pipe}s). The {@link Pipeline} contains some simple management APIs to
 * create or remove {@link Pipe}s objects.
 * 
 * As a note, you should NOT extend this class for production or application
 * purposes. This class is made non-final ONLY for testing/mocking/academic
 * purposes.
 */
@SuppressWarnings( { "rawtypes", "unchecked" })
public class Pipeline {

    private final URL baseURL;
    private final Map<String, Pipe> pipes = new HashMap<String, Pipe>();
    private final Multimap<String, Integer> loaderIdsForNamed = HashMultimap.create();

    /**
     * This is the factory which will create all pipe types. If not provided in
     * a constructor, it defaults to an instance of {@link DefaultPipeFactory}
     */
    private final PipeFactory pipeFactory;

    /**
     * An initializer method to instantiate the Pipeline,
     *
     * @param baseURL the URL of the server
     */
    public Pipeline(URL baseURL) {
        this.baseURL = baseURL;
        pipeFactory = new DefaultPipeFactory();
    }

    /**
     * An initializer method to instantiate the Pipeline,
     *
     * @param baseURL     the URL of the server
     * @param pipeFactory a PipeFactory
     */
    public Pipeline(URL baseURL, PipeFactory pipeFactory) {
        this.baseURL = baseURL;
        this.pipeFactory = pipeFactory;
    }

    /**
     * An initializer method to instantiate the Pipeline,
     *
     * @param baseURL the URL of the server
     * @throws IllegalArgumentException if baseURL is not a valid URL
     */
    public Pipeline(String baseURL) {
        this(baseURL, new DefaultPipeFactory());
    }

    /**
     * @param baseURL     the URL of the server
     * @param pipeFactory {@link PipeFactory} implementation
     * @throws IllegalArgumentException if baseURL is not a valid URL
     */
    private Pipeline(String baseURL, PipeFactory pipeFactory) {
        this.pipeFactory = pipeFactory;

        try {
            this.baseURL = new URL(baseURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Pipe pipe(Class klass) {
        PipeConfig config = new PipeConfig(baseURL, klass);
        return pipe(klass, config);
    }

    public Pipe pipe(Class klass, PipeConfig config) {
        Pipe pipe = pipeFactory.createPipe(klass, config);
        pipes.put(config.getName(), pipe);
        return pipe;
    }

    /**
     * Removes a pipe from the Pipeline object
     *
     * @param name the name of the actual pipe
     * @return the new created Pipe object
     */
    public Pipe remove(String name) {
        return pipes.remove(name);
    }

    /**
     * Look up for a pipe object.
     *
     * @param name the name of the actual pipe
     * @return the new created Pipe object
     */
    public Pipe get(String name) {
        return pipes.get(name);
    }

    /**
     * Look up for a pipe object.  This will wrap the Pipe in a Loader.
     *
     * @param name the name of the actual pipe
     * @param activity the activity whose lifecycle the loader will follow
     * @return the new created Pipe object
     * 
     */
    public LoaderPipe get(String name, Activity activity) {
        Pipe pipe = pipes.get(name);
        LoaderAdapter adapter = new LoaderAdapter(activity, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }

    /**
     * Look up for a pipe object.  This will wrap the Pipe in a Loader.
     *
     * @param name the name of the actual pipe
     * @param fragment the Fragment whose lifecycle the activity will follow
     * @param applicationContext the Context of the application.
     * 
     * @return the new created Pipe object
     * 
     */
    public LoaderPipe get(String name, Fragment fragment, Context applicationContext) {
        Pipe pipe = pipes.get(name);
        LoaderAdapter adapter = new LoaderAdapter(fragment, applicationContext, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }

    /**
     * Look up for a pipe object.  This will wrap the Pipe in a Loader.
     *
     * @param name the name of the actual pipe
     * @param activity the activity whose lifecycle the loader will follow
     * @return the new created Pipe object
     * 
     */
    public LoaderPipe get(String name, FragmentActivity activity) {
        Pipe pipe = pipes.get(name);
        SupportLoaderAdapter adapter = new SupportLoaderAdapter(activity, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }

    /**
     * Look up for a pipe object.  This will wrap the Pipe in a Loader.
     *
     * @param name the name of the actual pipe
     * @param fragment the Fragment whose lifecycle the activity will follow
     * @param applicationContext the Context of the application.
     * 
     * @return the new created Pipe object
     * 
     */
    public LoaderPipe get(String name, android.support.v4.app.Fragment fragment, Context applicationContext) {
        Pipe pipe = pipes.get(name);
        LoaderPipe adapter = new SupportLoaderAdapter(fragment, applicationContext, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }
}
