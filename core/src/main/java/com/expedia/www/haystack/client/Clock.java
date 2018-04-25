/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client;

/**
 * Wrapper around common time calculations needed for creating various events within the system.
 */
public interface Clock {

    /**
     * Returns the current value of the registered timer, in
     * nanoseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in nanoseconds.
     */
    long nanoTime();

    /**
     * Returns the current value of the registered timer, in
     * microseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in microseconds.
     */
    long microTime();

    /**
     * Returns the current value of the registered timer, in
     * milliseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in milliseconds.
     */
    long milliTime();
}
