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

package com.www.expedia.opencensus.exporter.trace;

import com.expedia.open.tracing.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentProtoClient;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorProtoClient;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.www.expedia.opencensus.exporter.trace.config.DispatcherConfig;
import com.www.expedia.opencensus.exporter.trace.config.GrpcAgentDispatcherConfig;
import com.www.expedia.opencensus.exporter.trace.config.HttpDispatcherConfig;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static com.google.common.base.Preconditions.checkState;

public class HaystackTraceExporter {
    private static final String REGISTER_NAME = HaystackTraceExporter.class.getName();
    private static final Object monitor = new Object();

    @GuardedBy("monitor")
    @Nullable
    static SpanExporter.Handler handler = null;


    private HaystackTraceExporter() {
    }

    public static void createAndRegister(final DispatcherConfig dispatcherConfig,
                                         final String serviceName) {
        createAndRegister(dispatcherConfig, serviceName, new Metrics(new NoopMetricsRegistry()));
    }

    public static void createAndRegister(final DispatcherConfig dispatcherConfig,
                                         final String serviceName,
                                         final Metrics metrics) {
        synchronized (monitor) {
            checkState(handler == null, "haystack exporter is already registered.");
            final Client<Span> dispatcher = buildRemoteClient(dispatcherConfig, metrics);
            final SpanExporter.Handler newHandler = new HaystackExporterHandler(dispatcher, serviceName, metrics);
            HaystackTraceExporter.handler = newHandler;
            register(Tracing.getExportComponent().getSpanExporter(), newHandler);
        }
    }

    private static Client<Span> buildRemoteClient(final DispatcherConfig dispatcherConfig, final Metrics metrics) {
        switch (dispatcherConfig.getType()) {
            case GRPC:
                final GrpcAgentDispatcherConfig grpcConfig = (GrpcAgentDispatcherConfig) dispatcherConfig;
                return new GRPCAgentProtoClient.Builder(metrics, grpcConfig.getHost(), grpcConfig.getPort()).build();
            case HTTP:
               final HttpDispatcherConfig httpConfig = (HttpDispatcherConfig) dispatcherConfig;
               return new HttpCollectorProtoClient(httpConfig.getHost(), httpConfig.getHttpHeaders());
            default:
                throw new RuntimeException("Fail to recognize the dispatcher config for haystack");
        }
    }

    /**
     * Registers the {@link HaystackTraceExporter}.
     *
     * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
     */
    private static void register(final SpanExporter spanExporter, final SpanExporter.Handler handler) {
        spanExporter.registerHandler(REGISTER_NAME, handler);
    }

    /**
     * Unregisters the {@link HaystackTraceExporter}.
     */
    public static void unregister() {
        synchronized (monitor) {
            checkState(handler != null, "haystack exporter is not registered.");
            Tracing.getExportComponent().getSpanExporter().unregisterHandler(REGISTER_NAME);
            handler = null;
        }
    }
}