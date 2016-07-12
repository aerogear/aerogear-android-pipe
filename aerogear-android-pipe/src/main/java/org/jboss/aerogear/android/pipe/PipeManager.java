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
package org.jboss.aerogear.android.pipe;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.pipe.loader.LoaderAdapter;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfiguration;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfigurationProvider;

/**
 * A Manager which handles the registration of configurations and references to
 * created pipes.
 */
public class PipeManager {

    private static Map<String, Pipe<?>> pipes = new HashMap<String, Pipe<?>>();

    private static Map<Class<? extends PipeConfiguration<?>>, ConfigurationProvider<?>> configurationProviderMap = new HashMap<Class<? extends PipeConfiguration<?>>, ConfigurationProvider<?>>();

    private static OnPipeCreatedListener onPipeCreatedListener = new OnPipeCreatedListener() {
        @Override
        public void onPipeCreated(PipeConfiguration<?> configuration, Pipe<?> pipe) {
            pipes.put(configuration.getName(), pipe);
        }
    };

    static {
        RestfulPipeConfigurationProvider configurationProvider = new RestfulPipeConfigurationProvider();
        PipeManager.registerConfigurationProvider(RestfulPipeConfiguration.class, configurationProvider);
    }

    private static final Map<String, List<Integer>> loaderIdsForNamed = new HashMap<String, List<Integer>>();

    private PipeManager() {
    }

    /**
     * 
     * This will add a new Configuration that this Manager can build
     * Configurations for.
     * 
     * @param <CFG> the actual Configuration type
     * @param configurationClass the class of configuration to be registered
     * @param provider the instance which will provide the configuration.
     */
    public static <CFG extends PipeConfiguration<CFG>> void registerConfigurationProvider
            (Class<CFG> configurationClass, ConfigurationProvider<CFG> provider) {
        configurationProviderMap.put(configurationClass, provider);
    }

    /**
     * Begins a new fluent configuration stanza.
     * 
     * @param <CFG> the Configuration type.
     * @param name an identifier which will be used to fetch the Pipe after
     *            configuration is finished. See : {@link PipeManager#getPipe(java.lang.String) }
     * @param pipeConfigurationClass the class of the configuration type.
     * 
     * @return a PipeConfiguration which can be used to build a Pipe object.
     */
    public static <CFG extends PipeConfiguration<CFG>> CFG config(String name, Class<CFG> pipeConfigurationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends PipeConfiguration<CFG>> provider =
                (ConfigurationProvider<? extends PipeConfiguration<CFG>>)
                configurationProviderMap.get(pipeConfigurationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name)
                .addOnPipeCreatedListener(onPipeCreatedListener);

    }

    /**
     * Fetches a named pipe
     * 
     * @param name the name of the Pipe given in {@link PipeManager#config(java.lang.String, java.lang.Class) }
     * 
     * @return the named Pipe or null
     */
    public static Pipe getPipe(String name) {
        return pipes.get(name);
    }

    /**
     * Look up for a pipe object. This will wrap the Pipe in a Loader.
     * 
     * @param name the name of the actual pipe
     * @param activity the activity whose lifecycle the loader will follow
     * @return the new created Pipe object
     * 
     */
    public static LoaderPipe getPipe(String name, Activity activity) {
        Pipe pipe = pipes.get(name);
        LoaderAdapter adapter = new LoaderAdapter(activity, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }

    /**
     * Look up for a pipe object. This will wrap the Pipe in a Loader.
     * 
     * @param name the name of the actual pipe
     * @param fragment the Fragment whose lifecycle the activity will follow
     * @param applicationContext the Context of the application.
     * 
     * @return the new created Pipe object
     * 
     */
    public static LoaderPipe getPipe(String name, Fragment fragment, Context applicationContext) {
        Pipe pipe = pipes.get(name);
        LoaderAdapter adapter = new LoaderAdapter(fragment, applicationContext, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }
    
    /**
     * Look up for a pipe object. This will wrap the Pipe in a Loader.
     * 
     * @param name the name of the actual pipe
     * @param fragment the Fragment whose lifecycle the activity will follow
     * @param applicationContext the Context of the application.
     * 
     * @return the new created Pipe object
     * 
     */
    public static LoaderPipe getPipe(String name, android.support.v4.app.Fragment fragment, Context applicationContext) {
        Pipe pipe = pipes.get(name);
        LoaderAdapter adapter = new LoaderAdapter(fragment, applicationContext, pipe, name);
        adapter.setLoaderIds(loaderIdsForNamed);
        return adapter;
    }

}
