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
package org.jboss.aerogear.android.pipe.rest;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.pipe.rest.gson.GsonRequestBuilder;
import org.jboss.aerogear.android.pipe.rest.gson.GsonResponseParser;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpProviderFactory;
import org.jboss.aerogear.android.pipe.paging.DefaultParameterProvider;
import org.jboss.aerogear.android.pipe.paging.URIBodyPageParser;
import org.jboss.aerogear.android.pipe.paging.URIPageHeaderParser;
import org.jboss.aerogear.android.pipe.util.UrlUtils;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeHandler;
import org.jboss.aerogear.android.pipe.RequestBuilder;
import org.jboss.aerogear.android.pipe.ResponseParser;
import org.jboss.aerogear.android.pipe.paging.PageConfig;
import org.jboss.aerogear.android.pipe.paging.ParameterProvider;

import android.util.Log;
import android.util.Pair;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.android.pipe.module.ModuleFields;
import org.jboss.aerogear.android.pipe.module.PipeModule;
import org.jboss.aerogear.android.pipe.util.ClassUtils;

public class RestRunner<T> implements PipeHandler<T> {

    private final PageConfig pageConfig;
    private static final String TAG = RestRunner.class.getSimpleName();
    private final RequestBuilder<T> requestBuilder;
    private final ParameterProvider parameterProvider;
    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializingg.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    private final Class<T[]> arrayKlass;
    private final URL baseURL;
    private final Provider<HttpProvider> httpProviderFactory = new HttpProviderFactory();
    private final Integer timeout;
    private final ResponseParser<T> responseParser;
    private Set<PipeModule> modules = new HashSet<PipeModule>();

    public RestRunner(Class<T> klass, URL baseURL) {
        this.klass = klass;
        this.arrayKlass = ClassUtils.asArrayClass(klass);
        this.baseURL = baseURL;
        this.requestBuilder = new GsonRequestBuilder<T>();
        this.pageConfig = null;
        this.parameterProvider = new DefaultParameterProvider();
        this.timeout = 60000;
        this.responseParser = new GsonResponseParser<T>();
    }

    RestRunner(Class<T> klass, URL baseURL,
            RestfulPipeConfiguration config) {
        this.klass = klass;
        this.arrayKlass = ClassUtils.asArrayClass(klass);
        this.baseURL = baseURL;
        this.timeout = config.getTimeout();

        if (config.getRequestBuilder() != null) {
            this.requestBuilder = config.getRequestBuilder();
        } else {
            this.requestBuilder = new GsonRequestBuilder<T>();
        }

        if (config.getResponseParser() != null) {
            this.responseParser = config.getResponseParser();
        } else {
            this.responseParser = new GsonResponseParser<T>();
        }

        if (config.getPageConfig() != null) {
            this.pageConfig = config.getPageConfig();

            if (pageConfig.getParameterProvider() != null) {
                this.parameterProvider = pageConfig.getParameterProvider();
            } else {
                this.parameterProvider = new DefaultParameterProvider();
            }

            if (pageConfig.getPageParameterExtractor() == null) {
                if (PageConfig.MetadataLocations.BODY.equals(pageConfig.getMetadataLocation())) {
                    pageConfig.setPageParameterExtractor(new URIBodyPageParser(baseURL));
                } else if (PageConfig.MetadataLocations.HEADERS.equals(pageConfig.getMetadataLocation())) {
                    pageConfig.setPageParameterExtractor(new URIPageHeaderParser(baseURL));
                }
            }

        } else {
            this.pageConfig = null;
            this.parameterProvider = new DefaultParameterProvider();
        }

        this.modules.addAll(config.getModules());

    }

    @Override
    public void onRemove(String id) {
        HttpProvider httpProvider = getHttpProvider();
        httpProvider.delete(id);
    }

    /**
     * 
     * @param queryParameters
     * @return a url with query params added
     */
    private URL addAuthorization(List<Pair<String, String>> queryParameters, URL baseURL) {

        StringBuilder queryBuilder = new StringBuilder();

        String amp = "";
        for (Pair<String, String> parameter : queryParameters) {
            try {
                queryBuilder.append(amp)
                        .append(URLEncoder.encode(parameter.first, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(parameter.second, "UTF-8"));

                amp = "&";
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UTF-8 encoding is not supported.", ex);
                throw new RuntimeException(ex);

            }
        }

        return appendQuery(queryBuilder.toString(), baseURL);

    }

    private void addAuthHeaders(HttpProvider httpProvider, ModuleFields fields) {
        List<Pair<String, String>> authHeaders = fields.getHeaders();

        for (Pair<String, String> header : authHeaders) {
            httpProvider.setDefaultHeader(header.first, header.second);
        }

    }

    private HttpProvider getHttpProvider() {
        return getHttpProvider(URI.create(""));
    }

    private HttpProvider getHttpProvider(URI relativeUri) {
        final String queryString;

        ModuleFields fields = loadAuth(relativeUri, "GET");

        if (relativeUri == null || relativeUri.getQuery() == null) {
            queryString = "";
        } else {
            queryString = relativeUri.getQuery().toString();
        }

        URL mergedURL = UrlUtils.appendToBaseURL(baseURL, relativeUri.getPath());
        URL authorizedURL = addAuthorization(fields.getQueryParameters(), UrlUtils.appendQueryToBaseURL(mergedURL, queryString));

        final HttpProvider httpProvider = httpProviderFactory.get(authorizedURL, timeout);
        httpProvider.setDefaultHeader("Content-TYpe", requestBuilder.getContentType());
        addAuthHeaders(httpProvider, fields);
        return httpProvider;

    }

    /**
     * Apply authentication if the token is present
     */
    private ModuleFields loadAuth(URI relativeURI, String httpMethod) {

        ModuleFields authFields = new ModuleFields();

        for (PipeModule module : modules) {
            ModuleFields moduleFields = module.loadModule(relativeURI, httpMethod, new byte[] {});
            if (!moduleFields.getHeaders().isEmpty()) {
                for (Pair<String, String> header : moduleFields.getHeaders()) {
                    authFields.addHeader(header.first, header.second);
                }
            }

            if (!moduleFields.getQueryParameters().isEmpty()) {
                for (Pair<String, String> header : moduleFields.getQueryParameters()) {
                    authFields.addQueryParameter(header.first, header.second);
                }
            }

        }

        return authFields;

    }

    private URL appendQuery(String query, URL baseURL) {
        try {
            URI baseURI = baseURL.toURI();
            String baseQuery = baseURI.getQuery();
            if (baseQuery == null || baseQuery.isEmpty()) {
                baseQuery = query;
            } else {
                if (query != null && !query.isEmpty()) {
                    baseQuery = baseQuery + "&" + query;
                }
            }

            if (baseQuery.isEmpty()) {
                baseQuery = null;
            }

            return new URI(baseURI.getScheme(), baseURI.getUserInfo(), baseURI.getHost(), baseURI.getPort(), baseURI.getPath(), baseQuery, baseURI.getFragment()).toURL();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "The URL could not be created from " + baseURL.toString(), ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "Error turning " + query + " into URI query.", ex);
            throw new RuntimeException(ex);
        }
    }

    protected RequestBuilder<T> getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public HeaderAndBody onRawRead(Pipe<T> requestingPipe, String id) {
        ReadFilter filter = new ReadFilter();
        filter.setLinkUri(URI.create(id));
        return onRawReadWithFilter(filter, requestingPipe);
    }

    @Override
    public HeaderAndBody onRawRead(Pipe<T> requestingPipe) {
        return onRawReadWithFilter(new ReadFilter(), requestingPipe);
    }

    @Override
    public HeaderAndBody onRawReadWithFilter(ReadFilter filter, Pipe<T> requestingPipe) {
        HttpProvider httpProvider;

        if (filter == null) {
            filter = new ReadFilter();
        }

        if (filter.getLinkUri() == null) {
            httpProvider = getHttpProvider(parameterProvider.getParameters(filter));
        } else {
            httpProvider = getHttpProvider(filter.getLinkUri());
        }

        return runHttpGet(httpProvider);

    }

    private HeaderAndBody runHttpGet(HttpProvider httpProvider) {
        HeaderAndBody httpResponse;

        try {
            httpResponse = httpProvider.get();
            return httpResponse;
        } catch (HttpException exception) {
            for (PipeModule module : modules) {
                if (module.handleError(exception)) {
                    httpResponse = httpProvider.get();
                    return httpResponse;
                }
            }
            throw exception;
        }
    }

    @Override
    public HeaderAndBody onRawSave(String id, byte[] item) {
        final HttpProvider httpProvider = getHttpProvider();

        HeaderAndBody result;
        if (id == null || id.length() == 0) {
            result = httpProvider.post(item);
        } else {
            result = httpProvider.put(id, item);
        }
        return result;
    }

}
