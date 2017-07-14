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
package org.jboss.aerogear.android.pipe.paging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.aerogear.android.pipe.util.ParseException;

/**
 * This class represents a "Link" header link.
 * See the <a href="http://tools.ietf.org/html/rfc5988">RFC </a>
 */
public class WebLink {

    private static final Pattern P = Pattern.compile("\\s*<(.*)>\\s*(.*)");

    private static final String EQUALS = "=";

    private String uri;
    private Map<String, String> parameters;

    /**
     * Attempts to parse a header value into a WebLink.
     * 
     * @param headerContent the header to parse
     * @throws ParseException thrown if parsing fails
     */
    public WebLink(String headerContent) throws ParseException {
        Matcher uriAndFields = P.matcher(headerContent);
        if (!uriAndFields.matches()) {
            throw new ParseException("Can not parse value:" + headerContent);
        }

        uri = uriAndFields.group(1);

        parameters = parseParams(uriAndFields.group(2));

    }

    private Map<String, String> parseParams(String group) throws ParseException {
        String[] params = group.split(";");
        HashMap<String, String> paramsMap = new HashMap<String, String>(params.length);
        for (String param : params) {
            if (param.trim().isEmpty()) {
                continue;
            }
            String[] pair = param.split(EQUALS);
            if (pair.length != 2) {
                throw new ParseException(param + " could not be parsed");
            } else {
                if (paramsMap.get(pair[0]) == null) {
                    paramsMap.put(pair[0], pair[1].replace("\"", ""));
                }
            }
        }
        return paramsMap;
    }

    /**
     * @return a string representation of the URI in a WebLink
     */
    public String getUri() {
        return uri;
    }

    /**
     * 
     * @return a map of parameters for the WebLink
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

}
