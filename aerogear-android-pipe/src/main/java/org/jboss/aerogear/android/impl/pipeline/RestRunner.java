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
package org.jboss.aerogear.android.impl.pipeline;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;
import org.jboss.aerogear.android.impl.pipeline.paging.URIBodyPageParser;
import org.jboss.aerogear.android.impl.pipeline.paging.URIPageHeaderParser;
import org.jboss.aerogear.android.impl.util.UrlUtils;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.RequestBuilder;
import org.jboss.aerogear.android.pipeline.ResponseParser;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.ParameterProvider;

import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.android.code.ModuleFields;
import org.jboss.aerogear.android.impl.util.ClassUtils;

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
    private AuthenticationModule authModule;
    private AuthzModule authzModule;
    

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

        if (config.getAuthModule() != null) {
            this.authModule = config.getAuthModule();
        }

        if (config.getAuthzModule() != null) {
            this.authzModule = config.getAuthzModule();
        }

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

    private void addAuthHeaders(HttpProvider httpProvider, AuthorizationFields fields) {
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

        AuthorizationFields fields = loadAuth(relativeUri, "GET");

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
    private AuthorizationFields loadAuth(URI relativeURI, String httpMethod) {

        if (authModule != null && authModule.isLoggedIn()) {
            ModuleFields fields = authModule.loadModule(relativeURI, httpMethod, new byte[] {});
            AuthorizationFields authorizationFields = new AuthorizationFields();
            authorizationFields.setHeaders(fields.getHeaders());
            authorizationFields.setQueryParameters(fields.getQueryParameters());
            return authorizationFields;
        } else if (authzModule != null && authzModule.hasCredentials()) {
            return authzModule.getAuthorizationFields(relativeURI, httpMethod, new byte[] {});
        }

        return new AuthorizationFields();
    }

    
    public void setAuthenticationModule(AuthenticationModule module) {
        this.authModule = module;
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

    private boolean retryAuth(AuthenticationModule authModule) {
        return authModule != null && authModule.isLoggedIn() && authModule.handleError(null);
    }

    private boolean retryAuthz(AuthzModule authzModule) {
        return authzModule != null && authzModule.isAuthorized() && authzModule.refreshAccess();
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
        } catch (HttpException exception) {
            //TODO: After modularization this should look over modules and pass in the httpException.
            if ((exception.getStatusCode() == HttpStatus.SC_UNAUTHORIZED
                    || exception.getStatusCode() == HttpStatus.SC_FORBIDDEN) && (retryAuth(authModule) || retryAuthz(authzModule))) {
                httpResponse = httpProvider.get();
            } else {
                throw exception;
            }
        }

        return httpResponse;
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
