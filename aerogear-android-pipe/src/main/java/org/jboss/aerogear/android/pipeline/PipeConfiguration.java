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
package org.jboss.aerogear.android.pipeline;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.code.PipeModule;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

/**
 * 
 * This is the top level PipeConfiguration class.  
 *
 * @param <CONFIGURATION> The implementation class type.
 */
public abstract class PipeConfiguration<CONFIGURATION extends PipeConfiguration<CONFIGURATION>> implements Config<CONFIGURATION> {

    private String name;
    private Collection<OnPipeCreatedListener> listeners;
    
    public PipeConfiguration() {
        listeners = new HashSet<OnPipeCreatedListener>();
    }
    
    /**
     * The name is the key which is used to reference the created pipe in 
     * PipeManager.
     * 
     * @return the current name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * The name is the key which is used to reference the created pipe in 
     * PipeManager.
     * 
     * @param name new name
     * @return this configuration for chaining.
     */
    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }
    
    /**
     * OnPipeCreatedListseners are a collection of classes to be notified when 
     * the configuration of the Pipe is complete.
     * 
     * @return the current collection.
     */
    public Collection<OnPipeCreatedListener> getOnPipeCreatedListeners() {
        return listeners;
    }

    /**
     * OnPipeCreatedListseners are a collection of classes to be notified when 
     * the configuration of the Pipe is complete.
     * 
     * @param listener  new listener to add to the collection
     * @return this configuration
     */
    public CONFIGURATION addOnPipeCreatedListener(OnPipeCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    /**
     * OnPipeCreatedListseners are a collection of classes to be notified when 
     * the configuration of the Pipe is complete.
     * 
     * @param listeners  new collection to replace the current one
     * @return this configuration
     */
    public CONFIGURATION setOnPipeCreatedListeners(Collection<OnPipeCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }
    
    /**
     * 
     * Creates a pipe based on the current configuration and notifies all listeners
     * 
     * @param <DATA> The data type of the Pipe
     * @param aClass The data type class of the Pipe
     * @return A pipe based on this configuration
     * 
     * @throws IllegalStateException if the Pipe can not be constructed.
     * 
     */
    public final <DATA> Pipe<DATA> forClass(Class<DATA> aClass) {
        Pipe<DATA> newPipe = buildPipeForClass(aClass);
        
        for (OnPipeCreatedListener listener : getOnPipeCreatedListeners()) {
            listener.onPipeCreated(this, newPipe);
        }
        
        return newPipe;   
    }

    /**
     * 
     * Validates configuration parameters and returns a Pipe instance.
     * 
     * @param <DATA> The data type of the Pipe
     * @param aClass The data type class of the Pipe
     * @return A pipe based on this configuration
     * 
     * @throws IllegalStateException if the Pipe can not be constructed.
     */
    protected abstract <DATA> Pipe<DATA> buildPipeForClass(Class<DATA> aClass);
    
    /**
     * The URL is a location of some resource or service the Pipe will interact with.
     * 
     * @param url the base URL the pipe will build upon.
     * @return this configuration
     */
    public abstract CONFIGURATION withUrl(URL url);

    /**
     * Modules are bits of functionality which are called during a Pipes 
     * lifecycle.
     * 
     * @param module a module to add to the lifecycle.
     * @return this configuration
     */
    public abstract CONFIGURATION module(PipeModule module);

    
    /**
     * Modules are bits of functionality which are called during a Pipes 
     * lifecycle.
     * 
     * @return the current List of Modules
     */
    public abstract List<PipeModule> getModules();

    
    /**
     * Because of their async nature, Pipes need to have a timeout which will 
     * error if reached.
     * 
     * @param timeout an amount of time in milliseconds.
     * @return this configuration
     */
    public abstract CONFIGURATION timeout(Integer timeout);
    
    /**
     * Paging is controlled and managed by the pageConfig objects
     * 
     * @param pageConfig a pageConfiguration
     * @return this configuration
     */
    public abstract CONFIGURATION pageConfig(PageConfig pageConfig);

    /**
     * Request builders are responsible for serializing the objects provided
     * to the Pipe into binary streams for consumption by a service.
     * 
     * @param builder the requestBuilder
     * @return this configuration
     */
    public abstract CONFIGURATION requestBuilder(RequestBuilder builder);
    
    /**
     * 
     * Response Parsers turn service responses into Objects for the Pipe.
     * 
     * @param responseParser a resonse Parser instance
     * @return this configuration
     */
    public abstract CONFIGURATION responseParser(ResponseParser responseParser);

}
