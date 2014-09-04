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

import static org.jboss.aerogear.android.impl.pipeline.PipeTypes.REST;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.aerogear.android.Pipeline;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.pipeline.Pipe;

import android.test.AndroidTestCase;

public class PipelineTest extends AndroidTestCase {

    private URL url;

    public void setUp() throws MalformedURLException, Exception {
        super.setUp();
        url = new URL("http://server.com/context/");
    }

    public void testRegisterPipeFactory() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url, new StubPipeFactory());

        Pipe stubPipe = pipeline.pipe(Data.class, new PipeConfig(url, Data.class));

        assertNotNull("received pipe", stubPipe);
        assertEquals("verifying the given URL", "http://myStubUrl/myStubProject", stubPipe.getUrl().toString());
        assertEquals("verifying the type", "Stub", stubPipe.getType().getName());
    }

    public void testAddPipe() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        Pipe newPipe = pipeline.pipe(Data.class);

        assertEquals("verifying the given URL", "http://server.com/context/data", newPipe.getUrl().toString());
        assertEquals("verifying the type", REST, newPipe.getType());
    }

    public void testAddPipeWithEndpoint() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("bad name");
        config.setEndpoint("foo");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/context/foo", newPipe.getUrl().toString());
    }

    public void testAddPipeWithType() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
    }

    public void testAddPipeWithUrl() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setName("foo");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/otherContext/data", newPipe.getUrl().toString());
    }

    public void testAddPipeWithEndpointAndType() throws MalformedURLException {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        config.setEndpoint("bar/");
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
        assertEquals("verifying the given URL", "http://server.com/context/bar/", newPipe.getUrl().toString());
    }

    public void testAddPipeWithEndpointAndURL() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setName("bad name");
        config.setEndpoint("foo/");
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the given URL", "http://server.com/otherContext/foo/", newPipe.getUrl().toString());
    }

    public void testAddPipeWithTypeAndUrl() throws MalformedURLException {
        URL otherURL = new URL("http://server.com/otherContext/");

        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(otherURL, Data.class);
        config.setType(REST);
        Pipe newPipe = pipeline.pipe(Data.class, config);

        assertEquals("verifying the type", REST, newPipe.getType());
        assertEquals("verifying the given URL", "http://server.com/otherContext/data", newPipe.getUrl().toString());
    }

    public void testGetExistingPipe() {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        pipeline.pipe(Data.class, config);

        Pipe fooPipe = pipeline.get("foo");
        assertNotNull("received pipe", fooPipe);
    }

    public void testGetNonExistingPipe() {
        Pipeline pipeline = new Pipeline(url);

        Pipe fooPipe = pipeline.get("Footasks");
        assertNull("Not received pipe", fooPipe);
    }

    public void testRemoveExistingPipe() {
        Pipeline pipeline = new Pipeline(url);
        final PipeConfig config = new PipeConfig(url, Data.class);
        config.setName("foo");
        pipeline.pipe(Data.class, config);

        Pipe fooPipe = pipeline.remove("foo");
        assertNotNull("deleted pipe", fooPipe);

        fooPipe = pipeline.get("foo");
        assertNull("Not received pipe", fooPipe);
    }

    public void testRemoveNonExistingPipe() {
        Pipeline pipeline = new Pipeline(url);

        Pipe fooPipe = pipeline.remove("foo");
        assertNull("Not deleted pipe", fooPipe);
    }

    public void testPipeConfigEndpointIsSetCorrectly() {
        Pipeline pipeline = new Pipeline(url);
        PipeConfig pipeConfig = new PipeConfig(url, Data.class);
        pipeConfig.setEndpoint("EndPoint");
        Pipe<Data> pipe = pipeline.pipe(Data.class, pipeConfig);
        assertEquals("http://server.com/context/EndPoint", pipe.getUrl().toString());

    }

    public void defaultPipeFactoryThrowsIAEOnBadConfigType() {
        try {
            DefaultPipeFactory factory = new DefaultPipeFactory();
            PipeConfig config = new PipeConfig(url, Data.class);
            config.setType(null);
            factory.createPipe(Data.class, config);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail("Expected IllegalArgumentException");
    }

    public void pipelineStringConstructorThrowsExceptionOnBadURL()
            throws Exception {
        try {
            new Pipeline("ttp:");
        } catch (IllegalArgumentException ignore) {
            return;
        }
        fail("Expected IllegalArgumentException");
    }

    public void pipeFactoryAppendsUrlCorrectly() throws Exception {
        URL altUrl = new URL("http://server.com/context");
        DefaultPipeFactory factory = new DefaultPipeFactory();
        PipeConfig config = new PipeConfig(altUrl, Data.class);
        Pipe<Data> pipe = factory.createPipe(Data.class, config);
        assertEquals("http://server.com/context/data", pipe.getUrl().toString());
    }

    public void pipelineStringConstructor() {
        Pipeline pipeline = new Pipeline(url.toString());
        Pipeline pipeline2 = new Pipeline(url);

        Pipe pipe = pipeline.pipe(Data.class);
        Pipe pipe2 = pipeline2.pipe(Data.class);

        assertEquals(pipe.getUrl(), pipe2.getUrl());
    }
}
