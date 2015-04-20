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

import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;

/**
 * Classes which implement this interface provide the logic for how pipes
 * interact with services.
 */
public interface PipeHandler<T> {

    /**
     * 
     * This method fetches a resource with an id on behalf of a Pipe.  This 
     * method will often be an HTTP GET but it is not required to be.
     * 
     * @param requestingPipe the Pipe the handler is acting on behalf of.
     * @param id the id of the remote resource
     * 
     * @return parsed headers (if any) and the raw byte array of the remote 
     * resource stored in the body parameter.
     */
    HeaderAndBody onRawRead(Pipe<T> requestingPipe, String id);

    /**
     * 
     * This method fetches a resource on behalf of a Pipe.  This 
     * method will often be an HTTP GET but it is not required to be.
     * 
     * @param requestingPipe the Pipe the handler is acting on behalf of.
     * 
     * @return parsed headers (if any) and the raw byte array of the remote 
     * resource stored in the body parameter.
     */
    HeaderAndBody onRawRead(Pipe<T> requestingPipe);

    /**
     * 
     * This method fetches a resource with an filter on behalf of a Pipe.  This 
     * method will often be an HTTP GET but it is not required to be.
     * 
     * @param filter an object with various filter parameters to use during the 
     * request.
     * @param requestingPipe the Pipe the handler is acting on behalf of.
     
     * @return parsed headers (if any) and the raw byte array of the remote 
     * resource stored in the body parameter.
     */
    HeaderAndBody onRawReadWithFilter(ReadFilter filter, Pipe<T> requestingPipe);

    /**
     * This method will save data for a Pipe.  This is often an HTTP PUT or 
     * HTTP POST but is not required to be.
     * 
     * @param id The id of the object to save.  May be empty or null.
     * @param item data to save.  This must not be null.
     * 
     * @return the results of the save operation.  The contents will vary based 
     * on implementation.
     */
    HeaderAndBody onRawSave(String id, byte[] item);

    /**
     * This method removes a resource with the id.  This method may throw a 
     * unchecked exception if the remove is unsuccessful, but the exact 
     * exception depends on the implementation.  This often performs an HTTP 
     * DELETE but is not required.
     * 
     * @param id the id of the resource to remove.  Must not be null or empty.
     */
    void onRemove(String id);
}
