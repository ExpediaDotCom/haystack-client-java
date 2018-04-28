/*
 *  Copyright 2017 Expedia, Inc.
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

package com.expedia.haystack.sleuth.core.configuration;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.SpanAdjuster;
import org.springframework.cloud.sleuth.instrument.web.SleuthWebProperties;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expedia.haystack.sleuth.core.instrument.web.adjuster.NameAdjuster;
import com.expedia.haystack.sleuth.core.instrument.web.brave.HaystackHttpClientParser;
import com.expedia.haystack.sleuth.core.instrument.web.brave.HaystackHttpServerParser;
import com.expedia.haystack.sleuth.core.reporter.DispatcherSpanReporter;
import com.expedia.haystack.sleuth.core.reporter.FilterSpanReporter;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentClient;
import com.expedia.www.haystack.client.dispatchers.clients.InMemoryClient;
import com.expedia.www.haystack.client.dispatchers.clients.NoopClient;
import com.expedia.www.haystack.client.dispatchers.formats.ProtoBufFormat;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.micrometer.GlobalMetricsRegistry;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.sampler.Sampler;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@Configuration
@EnableConfigurationProperties( {SamplerProperties.class, HaystackSpanProperties.class})
public class HaystackSleuthConfiguration {

    @Autowired(required = false)
    private List<SpanAdjuster> spanAdjusters;

    @Autowired(required = false)
    private List<NameAdjuster> nameAdjusters;

    @Value("${spring.application.name}")
    private String serviceName;

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.haystack.client.span.dispatch", havingValue = "logger", matchIfMissing = true)
    class LogClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Client client() {
            return new NoopClient();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.haystack.client.span.dispatch", havingValue = "memory")
    class InMemoryClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Client client(HaystackSpanProperties haystackSpanProperties) {
            return new InMemoryClient(new Metrics(new GlobalMetricsRegistry()), haystackSpanProperties.getMemory().getMaxSpans());
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.haystack.client.span.dispatch", havingValue = "grpc")
    class GrpcClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Client client(HaystackSpanProperties haystackTraceProperties) {
            String host = haystackTraceProperties.getGrpc().getHost();
            int port = haystackTraceProperties.getGrpc().getPort();

            return new GRPCAgentClient.Builder(new Metrics(new GlobalMetricsRegistry()), host, port)
                .withFormat(new ProtoBufFormat())
                .withKeepAliveTimeMS(haystackTraceProperties.getGrpc().getKeepAliveInMs())
                .withKeepAliveTimeoutMS(haystackTraceProperties.getGrpc().getKeepAliveTimeoutMs())
                .withKeepAliveWithoutCalls(haystackTraceProperties.getGrpc().getKeepAliveWithoutCalls())
                .withNegotiationType(haystackTraceProperties.getGrpc().getNegotiationType())
                .build();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public Reporter<Span> spanReporter(Client client, SleuthWebProperties sleuthWebProperties) {
        return new FilterSpanReporter(new DispatcherSpanReporter(client), Pattern.compile(sleuthWebProperties.getSkipPattern()));
    }

    @Bean
    @ConditionalOnMissingBean
    public Sampler sampler(SamplerProperties samplerProperties) {
        return new ProbabilityBasedSampler(samplerProperties);
    }

    @Bean
    public HttpTracing haystackHttpTracing(Tracing tracing) {
        return HttpTracing.newBuilder(tracing)
                          .clientParser(new HaystackHttpClientParser(nameAdjusters))
                          .serverParser(new HaystackHttpServerParser(nameAdjusters))
                          .build();
    }

    @Bean
    public Tracing sleuthTracing(Propagation.Factory factory, CurrentTraceContext currentTraceContext, Reporter<Span> reporter, Sampler sampler) {
        return Tracing.newBuilder()
                      .sampler(sampler)
                      .traceId128Bit(true)
                      .supportsJoin(false)
                      .localServiceName(serviceName)
                      .propagationFactory(factory)
                      .currentTraceContext(currentTraceContext)
                      .spanReporter(adjustedReporter(reporter)).build();
    }

    private Reporter<Span> adjustedReporter(Reporter<Span> delegate) {
        return span -> {
            Span spanToAdjust = span;

            for (SpanAdjuster spanAdjuster : spanAdjusters) {
                spanToAdjust = spanAdjuster.adjust(spanToAdjust);
            }

            delegate.report(spanToAdjust);
        };
    }
}
