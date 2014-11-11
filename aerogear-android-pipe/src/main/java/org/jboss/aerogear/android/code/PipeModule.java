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
package org.jboss.aerogear.android.code;

import java.net.URI;
import org.jboss.aerogear.android.http.HttpException;

/**
 * A PipeModule allows special actions to be taken during certain phases of the
 * Pipe life cycle.
 * 
 * 
 */
public interface PipeModule {

    /**
     * When construction a HTTP request, the module can prepare several
     * parameters to be applied to the request body, query, and headers.
     * 
     * @param relativeURI the URI the request will be made for.
     * @param httpMethod the HTTP method (GET, POST, PUT, DELETE) which will be
     *            used
     * @param requestBody the body of the request, if known. May be empty may
     *            not be null.
     * 
     * @return moduleFields which
     */
    ModuleFields loadModule(URI relativeURI, String httpMethod, byte[] requestBody);

    /**
     * This will try to resolve an error.
     * 
     * @param exception the exception to be resolved
     * 
     * @return if the error resolution was successful and the attempt should be
     *         retried.
     */
    public boolean handleError(HttpException exception);

}
