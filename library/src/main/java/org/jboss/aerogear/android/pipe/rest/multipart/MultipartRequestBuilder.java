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
package org.jboss.aerogear.android.pipe.rest.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.aerogear.android.core.reflection.Property;
import org.jboss.aerogear.android.pipe.RequestBuilder;

import android.util.Log;
import android.webkit.MimeTypeMap;
import org.jboss.aerogear.android.pipe.MarshallingConfig;

/**
 * This class generates a Multipart request with the type multipart/form-data
 * 
 * It will load the entire contents of files into memory before it uploads them.
 * 
 */
public class MultipartRequestBuilder<T> implements RequestBuilder<T> {

    private static final String TAG = MultipartRequestBuilder.class.getSimpleName();
    private static final String lineEnd = "\r\n";
    private static final String twoHyphens = "--";
    private final String boundary = UUID.randomUUID().toString();
    private final String CONTENT_TYPE = "multipart/form-data; boundary=" + boundary;
    private final String OCTECT_STREAM_MIME_TYPE = "application/octet-stream";
    private MarshallingConfig marshallingConfig = new MarshallingConfig();

    @Override
    public byte[] getBody(T data) {

        ByteArrayOutputStream binaryStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(binaryStream);

        try {
            List<Property> properties = getProperties(data.getClass());

            Map<String, String> fields = new HashMap<String, String>(properties.size());
            Map<String, TypeAndStream> files = new HashMap<String, TypeAndStream>(properties.size());

            for (Property propertyDescriptor : properties) {

                Object value = propertyDescriptor.getValue(data);
                if (value == null) {
                    continue;
                } else if (value.getClass().isPrimitive() || value instanceof String) {
                    fields.put(propertyDescriptor.getFieldName(), value.toString());

                } else {
                    if (value instanceof byte[]) {
                        files.put(propertyDescriptor.getFieldName(),
                                new TypeAndStream(OCTECT_STREAM_MIME_TYPE,
                                        propertyDescriptor.getFieldName(),
                                        new ByteArrayInputStream((byte[]) value)));
                    } else if (value instanceof InputStream) {
                        files.put(propertyDescriptor.getFieldName(),
                                new TypeAndStream(OCTECT_STREAM_MIME_TYPE,
                                        propertyDescriptor.getFieldName(),
                                        (InputStream) value));
                    } else if (value instanceof File) {
                        files.put(propertyDescriptor.getFieldName(),
                                new TypeAndStream(getMimeType((File) value),
                                        ((File) value).getName(),
                                        new FileInputStream((File) value)));
                    } else if (value instanceof TypeAndStream) {
                        files.put(propertyDescriptor.getFieldName(),
                                (TypeAndStream) value);
                    } else {
                        throw new IllegalArgumentException(propertyDescriptor.getFieldName() + " is not a supported type for Multipart uplaod");
                    }
                }
            }

            for (Map.Entry<String, String> field : fields.entrySet()) {
                setField(dataOutputStream, field.getKey(), field.getValue());
            }

            if (files.size() == 1) {
                Map.Entry<String, TypeAndStream> pair = files.entrySet().iterator().next();
                TypeAndStream type = pair.getValue();
                String name = pair.getKey();
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + type.getFileName() + "\"" + lineEnd);
                dataOutputStream.writeBytes("Content-Type: " + type.getMimeType() + lineEnd);
                dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                int b;
                while ((b = type.getInputStream().read()) != -1) {
                    dataOutputStream.write(b);
                }
                dataOutputStream.writeBytes(lineEnd);
            } else if (files.size() > 1) {
                String newBoundary = UUID.randomUUID().toString();
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"files\"" + lineEnd);
                dataOutputStream.writeBytes("Content-Type: multipart/mixed; boundary=" + newBoundary + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                for (Map.Entry<String, TypeAndStream> file : files.entrySet()) {
                    TypeAndStream type = file.getValue();
                    dataOutputStream.writeBytes(twoHyphens + newBoundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: file; filename=\"" + type.getFileName() + "\"" + lineEnd);
                    dataOutputStream.writeBytes("Content-Type: " + type.getMimeType() + lineEnd);
                    dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    int b;
                    while ((b = type.getInputStream().read()) != -1) {
                        dataOutputStream.write(b);
                    }
                    dataOutputStream.writeBytes(lineEnd);
                }
                dataOutputStream.writeBytes(twoHyphens + newBoundary + twoHyphens + lineEnd);
            }
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            return binaryStream.toByteArray();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new IllegalStateException(ex);
        }

    }

    private List<Property> getProperties(Class<? extends Object> baseClass) {

        ArrayList<Property> properties = new ArrayList<Property>();

        for (Field field : baseClass.getDeclaredFields()) {
            
            if (!field.isSynthetic()) {
                Property property = new Property(baseClass, field.getName());
                properties.add(property);
            }

        }

        return properties;

    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    private void setField(DataOutputStream dataOutputStream, String name, Object value) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: text/plain; charset=US-ASCII" + lineEnd);
        dataOutputStream.writeBytes("Content-Transfer-Encoding: 8bit" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(value.toString() + lineEnd);
    }

    private String getMimeType(File file) throws MalformedURLException {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public MarshallingConfig getMarshallingConfig() {
        return marshallingConfig;
    }

    public void setMarshallingConfig(MarshallingConfig marshallingConfig) {
        this.marshallingConfig = marshallingConfig;
    }
}
