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

import java.util.concurrent.TimeUnit;

/**
 * Clock implementation using the System timer for all calculations.
 *
 * @see System#nanoTime()
 * @see System#currentTimeMillis()
 */
public class SystemClock implements Clock {

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public long microTime() {
        return TimeUnit.MILLISECONDS.toMicros(milliTime());
    }

    @Override
    public long milliTime() {
        return System.currentTimeMillis();
    }
}
