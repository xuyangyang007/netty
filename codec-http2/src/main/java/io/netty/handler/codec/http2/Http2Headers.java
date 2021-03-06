/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * An immutable collection of headers sent or received via HTTP/2.
 */
public abstract class Http2Headers implements Iterable<Entry<String, String>> {

    public static final Http2Headers EMPTY_HEADERS = new Http2Headers() {

        @Override
        public String get(CharSequence name) {
            return null;
        }

        @Override
        public List<String> getAll(CharSequence name) {
            return Collections.emptyList();
        }

        @Override
        public List<Entry<String, String>> entries() {
            return Collections.emptyList();
        }

        @Override
        public boolean contains(CharSequence name) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Set<String> names() {
            return Collections.emptySet();
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return entries().iterator();
        }

        @Override
        public String forEach(HeaderVisitor visitor) {
            return null;
        }
    };

    /**
     * The prefix used to denote an HTTP/2 psuedo-header.
     */
    public static String PSEUDO_HEADER_PREFIX = ":";

    /**
     * HTTP/2 pseudo-headers names.
     */
    public enum PseudoHeaderName {
        /**
         * {@code :method}.
         */
        METHOD(PSEUDO_HEADER_PREFIX + "method"),

        /**
         * {@code :scheme}.
         */
        SCHEME(PSEUDO_HEADER_PREFIX + "scheme"),

        /**
         * {@code :authority}.
         */
        AUTHORITY(PSEUDO_HEADER_PREFIX + "authority"),

        /**
         * {@code :path}.
         */
        PATH(PSEUDO_HEADER_PREFIX + "path"),

        /**
         * {@code :status}.
         */
        STATUS(PSEUDO_HEADER_PREFIX + "status");

        private final String value;

        PseudoHeaderName(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        /**
         * Indicates whether the given header name is a valid HTTP/2 pseudo header.
         */
        public static boolean isPseudoHeader(String header) {
            if (header == null || !header.startsWith(Http2Headers.PSEUDO_HEADER_PREFIX)) {
                // Not a pseudo-header.
                return false;
            }

            // Check the header name against the set of valid pseudo-headers.
            for (PseudoHeaderName pseudoHeader : PseudoHeaderName.values()) {
                String pseudoHeaderName = pseudoHeader.value();
                if (pseudoHeaderName.equals(header)) {
                    // It's a valid pseudo-header.
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns the {@link Set} of all header names.
     */
    public abstract Set<String> names();

    /**
     * Returns the header value with the specified header name. If there is more than one header
     * value for the specified header name, the first value is returned.
     *
     * @return the header value or {@code null} if there is no such header
     */
    public abstract String get(CharSequence name);

    /**
     * Returns the header values with the specified header name.
     *
     * @return the {@link List} of header values. An empty list if there is no such header.
     */
    public abstract List<String> getAll(CharSequence name);

    /**
     * Returns all header names and values that this frame contains.
     *
     * @return the {@link List} of the header name-value pairs. An empty list if there is no header
     *         in this message.
     */
    public abstract List<Map.Entry<String, String>> entries();

    /**
     * Returns {@code true} if and only if there is a header with the specified header name.
     */
    public abstract boolean contains(CharSequence name);

    /**
     * Checks if no header exists.
     */
    public abstract boolean isEmpty();

    /**
     * Gets the number of headers contained in this object.
     */
    public abstract int size();

    /**
     * Allows a means to reduce GC pressure while iterating over a collection
     */
    public interface HeaderVisitor {
        /**
         * @return
         * <ul>
         * <li>{@code true} if the processor wants to continue the loop and handle the entry.</li>
         * <li>{@code false} if the processor wants to stop handling headers and abort the loop.</li>
         * </ul>
         */
        boolean visit(Map.Entry<String, String> entry);
    }

    /**
     * Iterates over the entries contained within this header object in no guaranteed order
     * @return {@code null} if the visitor iterated to or beyond the end of the headers.
     *  The last-visited header name If the {@link HeaderVisitor#visit(Entry)} returned {@code false}.
     */
    public abstract String forEach(HeaderVisitor visitor);

    /**
     * Interface for the Builder pattern for {@link Http2Headers}.
     */
    public interface Builder {
        /**
         * Build all the collected headers into a {@link Http2Headers}.
         * @return The {@link Http2Headers} object which this builder has been used for
         */
        Http2Headers build();

        /**
         * Gets the number of headers contained in this object.
         */
        int size();

        /**
         * Clears all values from this collection.
         */
        Builder clear();

        /**
         * Returns the header value with the specified header name. If there is more than one header
         * value for the specified header name, the first value is returned.
         * <p>
         * Note that all HTTP2 headers names are lower case and this method will not force {@code name} to lower case.
         * @return the header value or {@code null} if there is no such header
         */
        String get(CharSequence name);

        /**
         * Returns the header values with the specified header name.
         * <p>
         * Note that all HTTP2 headers names are lower case and this method will not force {@code name} to lower case.
         * @return the {@link List} of header values. An empty list if there is no such header.
         */
        List<String> getAll(CharSequence name);

        /**
         * Clears all existing headers from this collection and replaces them with the given header
         * set.
         */
        void set(Http2Headers headers);

        /**
         * Adds the given header to the collection.
         * @throws IllegalArgumentException if the name or value of this header is invalid for any reason.
         */
        Builder add(CharSequence name, Object value);

        /**
         * Adds the given header to the collection.
         * @throws IllegalArgumentException if the name or value of this header is invalid for any reason.
         */
        Builder add(String name, Object value);

        /**
         * Removes the header with the given name from this collection.
         * This method will <b>not</b> force the {@code name} to lower case before looking for a match.
         */
        Builder remove(CharSequence name);

        /**
         * Removes the header with the given name from this collection.
         * This method will force the {@code name} to lower case before looking for a match.
         */
        Builder remove(String name);

        /**
         * Sets the given header in the collection, replacing any previous values.
         * @throws IllegalArgumentException if the name or value of this header is invalid for any reason.
         */
        Builder set(CharSequence name, Object value);

        /**
         * Sets the given header in the collection, replacing any previous values.
         * @throws IllegalArgumentException if the name or value of this header is invalid for any reason.
         */
        Builder set(String name, Object value);

        /**
         * Sets the given header in the collection, replacing any previous values.
         * @throws IllegalArgumentException if the name or value of this header is invalid for any reason.
         */
        Builder set(String name, Iterable<?> values);

        /**
         * Sets the {@link PseudoHeaderName#METHOD} header.
         */
        Builder method(String method);

        /**
         * Sets the {@link PseudoHeaderName#SCHEME} header.
         */
        Builder scheme(String scheme);

        /**
         * Sets the {@link PseudoHeaderName#AUTHORITY} header.
         */
        Builder authority(String authority);

        /**
         * Sets the {@link PseudoHeaderName#PATH} header.
         */
        Builder path(String path);

        /**
         * Sets the {@link PseudoHeaderName#STATUS} header.
         */
        Builder status(String status);
    }

    /**
     * Gets the {@link PseudoHeaderName#METHOD} header or {@code null} if there is no such header
     */
    public final String method() {
        return get(PseudoHeaderName.METHOD.value());
    }

    /**
     * Gets the {@link PseudoHeaderName#SCHEME} header or {@code null} if there is no such header
     */
    public final String scheme() {
        return get(PseudoHeaderName.SCHEME.value());
    }

    /**
     * Gets the {@link PseudoHeaderName#AUTHORITY} header or {@code null} if there is no such header
     */
    public final String authority() {
        return get(PseudoHeaderName.AUTHORITY.value());
    }

    /**
     * Gets the {@link PseudoHeaderName#PATH} header or {@code null} if there is no such header
     */
    public final String path() {
        return get(PseudoHeaderName.PATH.value());
    }

    /**
     * Gets the {@link PseudoHeaderName#STATUS} header or {@code null} if there is no such header
     */
    public final String status() {
        return get(PseudoHeaderName.STATUS.value());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (String name : names()) {
            result = prime * result + name.hashCode();
            Set<String> values = new TreeSet<String>(getAll(name));
            for (String value : values) {
                result = prime * result + value.hashCode();
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Http2Headers)) {
            return false;
        }
        Http2Headers other = (Http2Headers) o;

        // First, check that the set of names match.
        Set<String> names = names();
        if (!names.equals(other.names())) {
            return false;
        }

        // Compare the values for each name.
        for (String name : names) {
            List<String> values = getAll(name);
            List<String> otherValues = other.getAll(name);
            if (values.size() != otherValues.size()) {
                return false;
            }
            // Convert the values to a set and remove values from the other object to see if
            // they match.
            Set<String> valueSet = new HashSet<String>(values);
            valueSet.removeAll(otherValues);
            if (!valueSet.isEmpty()) {
                return false;
            }
        }

        // They match.
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Http2Headers[");
        for (Map.Entry<String, String> header : this) {
            builder.append(header.getKey());
            builder.append(':');
            builder.append(header.getValue());
            builder.append(',');
        }
        builder.append(']');
        return builder.toString();
    }
}
