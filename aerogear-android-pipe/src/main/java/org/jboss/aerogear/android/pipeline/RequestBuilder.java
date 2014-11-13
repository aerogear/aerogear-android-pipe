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

public interface RequestBuilder<T> {

    /**
     * This method creates the body of a request for a Pipe to use.
     * 
     * This is basically a serialization style operation.
     * 
     * @param data an object to form a body out of.
     * 
     * @return A request body which represents the data parameter
     */
    byte[] getBody(T data);

    /**
     * @return the Content-Type header to be sent to the server.
     */
    String getContentType();

    /**
     * The marshalling config sets options for reading and processing data
     * 
     * @return the current config
     */
    MarshallingConfig getMarshallingConfig();
}
