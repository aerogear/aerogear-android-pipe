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

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import org.jboss.aerogear.android.Pipeline;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.RequestBuilder;
import org.jboss.aerogear.android.pipeline.ResponseParser;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;

import com.google.gson.GsonBuilder;

/**
 * Specifies configurations for {@link Pipe} to be build by {@link Pipeline}
 */
@SuppressWarnings("rawtypes")
public final class PipeConfig {

    private URL baseURL;
    private String name;
    private String endpoint;
    private PipeType type = PipeTypes.REST;
    private PageConfig pageConfig;
    private AuthenticationModule authModule;
    private AuthzModule authzModule;
    private PipeHandler handler;
    private Integer timeout = 60000;
    private ResponseParser responseParser = new GsonResponseParser();
    /**
     * Where the data elements the pipe wants to extract are found in the
     * response from the server. Defaults to the root of the data structure
     * represented by an empty string
     */
    private String dataRoot = "";
    private Charset encoding = Charset.forName("UTF-8");
    private RequestBuilder requestBuilder = new GsonRequestBuilder();
    private GsonBuilder gsonBulder = new GsonBuilder();

    public PipeConfig(URL baseURL, Class klass) {
        this.baseURL = baseURL;
        this.name = klass.getSimpleName().toLowerCase(Locale.US);
        this.endpoint = name;
        this.type = PipeTypes.REST;
    }

    /**
     * @return The Name that Pipeline will use to reference Pipes built by this
     * configuration.
     */
    public String getName() {
        return name;
    }

    /**
     * Modify the name field. This is the value Pipeline uses to reference a
     * Pipe built by this configuration.
     *
     * @param name the name of this pipe
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the URL which Endpoints will be appended to.
     */
    public URL getBaseURL() {
        return baseURL;
    }

    /**
     * Change the URL which Endpoints are appended to.
     *
     * @param baseURL the URL which Endpoints are appended to.
     */
    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * @return the string appended to BaseURL. It is the resource a Pipe
     * connects to.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Change the string appended to BaseURL. It is the resource a Pipe connects
     * to.
     *
     * @param endpoint the new endpoint name
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return type of Pipe to configure.
     * @see PipeTypes
     */
    public PipeType getType() {
        return type;
    }

    /**
     * @param type a PipeType to use when building a Pipe with this config.
     * @see PipeTypes
     */
    public void setType(PipeType type) {
        this.type = type;
    }

    /**
     * @return a {@link AuthenticationModule} which will be used for
     * Authentication.
     */
    public AuthenticationModule getAuthModule() {
        return authModule;
    }

    /**
     * This value must be set to use Authentication in Pipes build from this
     * config.
     *
     * @param authModule a {@link AuthenticationModule} which works with this
     * PipeConfig
     */
    public void setAuthModule(AuthenticationModule authModule) {
        this.authModule = authModule;
    }

    /**
     * PageConfig is the configuration information for Paging.
     * 
     * @return the current pageConfig
     *
     * @see PageConfig the current pageconfig
     */
    public PageConfig getPageConfig() {
        return pageConfig;
    }

    /**
     * PageConfig is the configuration information for Paging.
     *
     * @param pageConfig a new pageconfig
     * @see PageConfig
     */
    public void setPageConfig(PageConfig pageConfig) {
        this.pageConfig = pageConfig;
    }

    /**
     *
     * @return the current {@link PipeHandler} for Pipes build using this
     * configuration
     */
    public PipeHandler getHandler() {
        return handler;
    }

    /**
     * @param handler a new {@link PipeHandler} for Pipes build using this
     * configuration
     */
    public void setHandler(PipeHandler handler) {
        this.handler = handler;
    }

    /**
     * Timeout is the length of time in milliseconds that a Pipe will wait for a
     * response from a call to read, save or remove
     *
     * @return the current timeout.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Timeout is the length of time in milliseconds that a Pipe will wait for a
     * response from a call to read, save or remove
     *
     * @param timeout a new
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * A request builder is responsible for turning an object into a request
     * used in a Pipe's save methods.
     *
     * This value defaults to {@link GsonRequestBuilder}
     *
     * @param requestBuilder a new request builder
     */
    public void setRequestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    /**
     * A request builder is responsible for turning an object into a request
     * used in a Pipe's save methods.
     *
     * This value defaults to {@link GsonRequestBuilder}
     *
     * @return the current request builder.
     */
    public RequestBuilder getRequestBuilder() {
        return this.requestBuilder;
    }

    /**
     * A ResponseParser is responsible for parsing a String value of the
     * response from a remote source into a object instance.
     *
     * @return the current value of the ResponseParser field
     *
     */
    public ResponseParser getResponseParser() {
        return responseParser;
    }

    /**
     * A ResponseParser is responsible for parsing a String value of the
     * response from a remote source into a object instance.
     *
     * @param responseParser the response parser to use
     * 
     */
    public void setResponseParser(ResponseParser responseParser) {
        this.responseParser = responseParser;
    }

    public AuthzModule getAuthzModule() {
        return authzModule;
    }

    public void setAuthzModule(AuthzModule authzModule) {
        this.authzModule = authzModule;
    }

}
