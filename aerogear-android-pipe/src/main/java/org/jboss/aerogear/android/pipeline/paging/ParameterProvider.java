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
package org.jboss.aerogear.android.pipeline.paging;

import java.net.URI;

import org.jboss.aerogear.android.ReadFilter;

/**
 * Classes which implement this interface should provide the necessary parameters for paging to a Pipe's read request.
 */
public interface ParameterProvider {

    /**
     * 
     * @param filter the {@link ReadFilter} associated with a request.
     * @return a URI query which represents the filter's paging request.
     */
    public URI getParameters(ReadFilter filter);

}
