/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.code;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the possible extra fields which may be applied to a
 * request by a module
 */
public class ModuleFields {

    private List<Pair<String, String>> queryParameters = new ArrayList<Pair<String, String>>();
    private List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();

    /**
     * @return a copy queryParameters
     */
    public List<Pair<String, String>> getQueryParameters() {
        return new ArrayList<Pair<String, String>>(queryParameters);
    }

    /**
     *
     * @param queryParameters may not be null
     * @throws IllegalArgumentException if queryParameters is null
     */
    public void setQueryParameters(List<Pair<String, String>> queryParameters) {
        if (queryParameters == null) {
            throw new IllegalArgumentException("queryParameters may not be null");
        }
        this.queryParameters = new ArrayList<Pair<String, String>>(queryParameters);
    }

    /**
     * @return a copy of headers
     */
    public List<Pair<String, String>> getHeaders() {
        return new ArrayList<Pair<String, String>>(headers);
    }

    /**
     * @param headers may not be null
     * @throws IllegalArgumentException if headers is null
     */
    public void setHeaders(List<Pair<String, String>> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("headers may not be null");
        }
        this.headers = new ArrayList<Pair<String, String>>(headers);
    }

    public void addHeader(String headerName, String headerValue) {
        Pair<String, String> newToken = new Pair<String, String>(headerName, headerValue);
        headers.add(newToken);
    }

    public void addQueryParameter(String parameterName, String parameterValue) {
        Pair<String, String> newParameter = new Pair<String, String>(parameterName, parameterValue);
        queryParameters.add(newParameter);
    }
}
