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
package com.expedia.www.haystack.client.metrics.dropwizard;

import com.codahale.metrics.Meter;
import com.expedia.www.haystack.client.metrics.Counter;

public class DropwizardCounter implements Counter {
    private final Meter delegate;

    public DropwizardCounter(Meter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void increment(double amount) {
        delegate.mark((long) amount);
    }

    @Override
    public void decrement(double amount) {
        delegate.mark((long) (-1 * amount));
    }

    @Override
    public double count() {
        return delegate.getCount();
    }
}
