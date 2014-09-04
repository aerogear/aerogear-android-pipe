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

import org.jboss.aerogear.android.impl.util.UrlUtils;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeFactory;

public final class DefaultPipeFactory implements PipeFactory {

    @Override
    public <T> Pipe<T> createPipe(Class<T> klass, PipeConfig config) {
        Pipe<T> createdPipe;
        if (PipeTypes.REST.equals(config.getType())) {
            URL url = UrlUtils.appendToBaseURL(config.getBaseURL(), config.getEndpoint());

            createdPipe = new RestAdapter<T>(klass, url, config);

        } else {
            throw new IllegalArgumentException("Type is not supported yet");
        }

        return createdPipe;
    }

}
