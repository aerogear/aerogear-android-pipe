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
package org.jboss.aerogear.android.pipe.http;

import java.net.URL;

import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;

public class HttpStubProvider implements HttpProvider {

    private final URL url;
    private final HeaderAndBody response;

    public HttpStubProvider(URL url) {
        this.url = url;
        response = null;
    }

    public HttpStubProvider(URL url, HeaderAndBody response) {
        this.url = url;
        this.response = response;
    }

    public URL getUrl() {
        return url;
    }

    public HeaderAndBody get() {
        return response;
    }

    public HeaderAndBody post(String data) {
        return response;
    }

    public HeaderAndBody put(String id, String data) {
        return response;
    }

    public HeaderAndBody delete(String id) {
        return response;
    }

    @Override
    public void setDefaultHeader(String headerName, String headerValue) {
    }

    @Override
    public HeaderAndBody post(byte[] arg0) throws HttpException {
        return response;
    }

    @Override
    public HeaderAndBody put(String arg0, byte[] arg1) throws HttpException {
        return response;
    }

}
