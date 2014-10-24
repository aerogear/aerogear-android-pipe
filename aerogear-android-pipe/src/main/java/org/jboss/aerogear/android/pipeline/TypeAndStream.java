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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.pipeline;

import java.io.InputStream;

/**
 * Convenience Wrapper for posting multipart files
 */
public class TypeAndStream {
    private final String mimeType;
    private final InputStream inputStream;
    private final String fileName;

    public TypeAndStream(String mimeType, String fileName, InputStream inputStream) {
        this.mimeType = mimeType;
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.mimeType != null ? this.mimeType.hashCode() : 0);
        hash = 29 * hash + (this.inputStream != null ? this.inputStream.hashCode() : 0);
        hash = 29 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeAndStream other = (TypeAndStream) obj;
        if ((this.mimeType == null) ? (other.mimeType != null) : !this.mimeType.equals(other.mimeType)) {
            return false;
        }
        if (this.inputStream != other.inputStream && (this.inputStream == null || !this.inputStream.equals(other.inputStream))) {
            return false;
        }
        if ((this.fileName == null) ? (other.fileName != null) : !this.fileName.equals(other.fileName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TypeAndStream{" + "mimeType=" + mimeType + ", inputTream=" + inputStream + ", fileName=" + fileName + '}';
    }

}
