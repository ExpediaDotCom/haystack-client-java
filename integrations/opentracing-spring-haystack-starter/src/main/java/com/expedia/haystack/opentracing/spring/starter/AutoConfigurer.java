package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.haystack.opentracing.spring.starter.support.GrpcDispatcherFactory;
import com.expedia.haystack.opentracing.spring.starter.support.HttpDispatcherFactory;
import com.expedia.haystack.opentracing.spring.starter.support.TracerBuilderCustomizer;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.ChainedDispatcher;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.LoggerDispatcher;
import com.expedia.www.haystack.client.dispatchers.RemoteDispatcher;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.micrometer.MicrometerMetricsRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass(com.expedia.www.haystack.client.Tracer.class)
@ConditionalOnMissingBean(io.opentracing.Tracer.class)
@ConditionalOnProperty(value = "opentracing.spring.haystack.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration.class)
@EnableConfigurationProperties(Configuration.class)
public class AutoConfigurer {

    @Autowired(required = false)
    private List<TracerBuilderCustomizer> tracerCustomizers = Collections.emptyList();

    @Bean
    public io.opentracing.Tracer tracer(@Value("${spring.application.name:spring-boot}") String serviceName,
                                        final Dispatcher dispatcher,
                                        final MetricsRegistry metricsRegistry) {
        final Tracer.Builder tracerBuilder = new Tracer.Builder(metricsRegistry, serviceName, dispatcher);
        tracerCustomizers.forEach(customizer -> customizer.customize(tracerBuilder));
        return tracerBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Dispatcher dispatcher(Configuration configuration,
                                 MetricsRegistry metricsRegistry,
                                 GrpcDispatcherFactory grpcAgentFactory,
                                 HttpDispatcherFactory httpDispatcherFactory) {
        List<Dispatcher> dispatchers = new ArrayList<>();
        if (configuration.getAgent() != null) {
            dispatchers.add(grpcDispatcher(configuration.getAgent(), metricsRegistry, grpcAgentFactory));
        }

        if (configuration.getHttp() != null) {
            dispatchers.add(httpDispatcher(configuration.getHttp(), metricsRegistry, httpDispatcherFactory));
        }

        if (configuration.getLogger() != null) {
            dispatchers.add(loggerDispatcher(configuration.getLogger(), metricsRegistry));
        }

        if (dispatchers.size() == 0) {
            return new LoggerDispatcher.Builder(metricsRegistry).withLogger("haystack").build();
        }

        return (dispatchers.size() == 1) ? dispatchers.get(0) : new ChainedDispatcher(dispatchers);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsRegistry metricsRegistry(final MeterRegistry meterRegistry) {
        return new MicrometerMetricsRegistry(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcDispatcherFactory grpcDispatcherFactory() {
        return (metricsRegistry, config) ->
                new RemoteDispatcher.Builder(metricsRegistry, config.builder(metricsRegistry).build()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpDispatcherFactory httpDispatcherFactory() {
        return (metricsRegistry, config) ->
                new RemoteDispatcher.Builder(metricsRegistry, new HttpCollectorClient(config.getEndpoint(),
                                                                                      config.getHeaders())).build();
    }

    private Dispatcher grpcDispatcher(Configuration.AgentConfiguration agentConfiguration,
                                      MetricsRegistry metricsRegistry,
                                      GrpcDispatcherFactory factory) {
        return factory.create(metricsRegistry, agentConfiguration);
    }

    private Dispatcher httpDispatcher(Configuration.HttpConfiguration httpConfiguration,
                                      MetricsRegistry metricsRegistry,
                                      HttpDispatcherFactory factory) {
        return factory.create(metricsRegistry, httpConfiguration);
    }

    private Dispatcher loggerDispatcher(Configuration.LoggerConfiguration loggerConfiguration,
                                        MetricsRegistry metricsRegistry) {
        return new LoggerDispatcher.Builder(metricsRegistry).withLogger(loggerConfiguration.getName()).build();
    }
}
