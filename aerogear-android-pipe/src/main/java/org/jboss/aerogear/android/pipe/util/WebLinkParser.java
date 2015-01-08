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
package org.jboss.aerogear.android.pipe.util;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.android.pipe.paging.WebLink;

import android.util.Log;

/**
 * A utility class to parse WebLink headers.
 * 
 * See the <a href="http://tools.ietf.org/html/rfc5988">RFC</a>
 */
public class WebLinkParser {

    private static final String TAG = WebLinkParser.class.getSimpleName();
    private static final Character COMMA = ',';
    private static final Character QUOTE = '"';
    private static final Character OPEN_LINK = '<';
    private static final Character CLOSE_LINK = '>';

    public static List<WebLink> parse(String linkHeader) throws ParseException {

        if (linkHeader == null) {
            throw new IllegalArgumentException("null String passed to WebLinkParser.parse");
        }

        List<WebLink> linksList = new ArrayList<WebLink>();

        boolean inBrackets = false;
        boolean inQuotes = false;

        char[] linkHeaderCharacters = linkHeader.trim().toCharArray();
        StringBuilder buffer = new StringBuilder(linkHeader.length());

        for (Character c : linkHeaderCharacters) {

            if (inBrackets) {
                inBrackets = !c.equals(CLOSE_LINK);
                appendPrintedCharacter(buffer, c);
            } else if (inQuotes) {
                inQuotes = !c.equals(QUOTE);
                appendPrintedCharacter(buffer, c);
            } else {
                inBrackets = c.equals(OPEN_LINK);
                inQuotes = c.equals(QUOTE);

                if (c.equals(COMMA)) {
                    String link = buffer.toString();
                    buffer.delete(0, buffer.length());
                    try {
                        linksList.add(new WebLink(link));
                    } catch (ParseException parseEx) {
                        Log.e(TAG, "Parse error", parseEx);
                        throw parseEx;
                    }
                } else {
                    appendPrintedCharacter(buffer, c);
                }

            }

        }
        if (buffer.toString().trim().length() > 0) {
            try {
                linksList.add(new WebLink(buffer.toString()));
            } catch (ParseException parseEx) {
                Log.e(TAG, "Parse error", parseEx);
                throw parseEx;
            }
        }
        return linksList;
    }

    private static void appendPrintedCharacter(StringBuilder buffer, Character c) {
        if (!Character.isWhitespace(c)) {
            buffer.append(c);
        }
    }
}
