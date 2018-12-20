/*
 *  Copyright 2018 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.opencensus.exporter.trace.config;

import org.apache.commons.lang3.Validate;

public abstract class DispatcherConfig {
    public enum DispatchType {
        GRPC,
        HTTP
    }

    final private long shutdownTimeoutInMillis;

    public DispatcherConfig(long shutdownTimeoutInMillis) {
        Validate.isTrue(shutdownTimeoutInMillis > 0, "shutdown timeout greater than zero");
        this.shutdownTimeoutInMillis = shutdownTimeoutInMillis;
    }

    public long getShutdownTimeoutInMillis() {
        return shutdownTimeoutInMillis;
    }

    public abstract DispatchType getType();
}
