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
package org.jboss.aerogear.android.impl.core;

import java.net.URL;

import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;

public class HttpProviderFactory implements Provider<HttpProvider> {

    @Override
    public HttpProvider get(Object... in) {
        switch (in.length) {
        case 1:
            return new HttpRestProvider((URL) in[0]);
        case 2:
            return new HttpRestProvider((URL) in[0], (Integer) in[1]);
        default:
            throw new IllegalArgumentException("Wrong number of Arguments.  This method expects a URL or a URL and a Integer");
        }
    }
}
