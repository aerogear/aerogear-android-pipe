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

import java.net.URL;
import java.util.List;

import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.ReadFilter;

/**
 * A {@link Pipe} represents a server connection. An object of this class is responsible to communicate
 * with the server in order to perform read/write operations.
 * 
 * @param <T> The data type of the {@link Pipe} operation
 */
public interface Pipe<T> {

    /**
     * Returns the {@link URL} to which this {@link Pipe} object points.
     * 
     * @return the endpoint URL
     */
    URL getUrl();

    /**
     * Sends a signal to the Pipe to read its data and return it via the callback.
     * 
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void read(Callback<List<T>> callback);

    /**
     * Reads all the data from the underlying server connection.
     * 
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     * @param filter a {@link ReadFilter} for performing pagination and querying.
     */
    void read(ReadFilter filter, Callback<List<T>> callback);

    /**
     * Saves or updates a given object on the server.
     * 
     * @param item the item to save or update
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void save(T item, Callback<T> callback);

    /**
     * Removes an object from the underlying server connection. The given key argument is used as the objects ID.
     * 
     * @param id representing the ‘id’ of the object to be removed
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void remove(String id, Callback<Void> callback);

    /**
     * @return the class which travels on this pipe
     */
    Class<T> getKlass();

    /**
     * Returns the instance which is responsible for handling read, save, and remove.
     * 
     * @return the handler performing operations for this Pipe. May be the Pipe itself.
     */
    PipeHandler<T> getHandler();

    /**
     * The {@link RequestBuilder} is responsible for turning objects in bodies of requests.
     * 
     * @return the current RequestBuilder instance
     */
    RequestBuilder<T> getRequestBuilder();

    /**
     * The {@link ResponseParser} is responsible for turning responses from a SAVE into an object
     * 
     * @return the current ResponseParser instance
     */
    ResponseParser<T> getResponseParser();

}
