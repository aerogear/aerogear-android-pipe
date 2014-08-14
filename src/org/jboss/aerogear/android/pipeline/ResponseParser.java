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

import java.util.List;
import org.jboss.aerogear.android.http.HeaderAndBody;

public interface ResponseParser<T> {

    /**
     * UnMarshall a response and return an object array.
     * 
     * @param response the data from the server
     * @param responseType the type to marshal to
     * @return an instance of responseType
     */
    List<T> handleResponse(HeaderAndBody response, Class<T> responseType);

    /**
     * The marshalling config sets options for reading and processing data
     * 
     * @return the current config
     */
    MarshallingConfig getMarshallingConfig();
}
