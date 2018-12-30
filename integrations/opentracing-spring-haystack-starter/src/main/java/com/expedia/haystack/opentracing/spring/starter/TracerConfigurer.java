package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.haystack.opentracing.spring.starter.support.GrpcDispatcherFactory;
import com.expedia.haystack.opentracing.spring.starter.support.HttpDispatcherFactory;
import com.expedia.haystack.opentracing.spring.starter.support.TracerCustomizer;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.ChainedDispatcher;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.LoggerDispatcher;
import com.expedia.www.haystack.client.dispatchers.RemoteDispatcher;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.metrics.micrometer.MicrometerMetricsRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(com.expedia.www.haystack.client.Tracer.class)
@ConditionalOnMissingBean(io.opentracing.Tracer.class)
@ConditionalOnProperty(value = "opentracing.haystack.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration.class)
@EnableConfigurationProperties(TracerSettings.class)
public class TracerConfigurer {

    @Bean
    public io.opentracing.Tracer tracer(@Value("${spring.application.name:unnamed-application}") String serviceName,
                                        final Dispatcher dispatcher,
                                        final MetricsRegistry metricsRegistry,
                                        ObjectProvider<Collection<TracerCustomizer>> tracerCustomizersProvider) {
        Validate.notEmpty(serviceName);
        Validate.notNull(dispatcher);
        Validate.notNull(metricsRegistry);
        Validate.notNull(tracerCustomizersProvider);

        final Tracer.Builder tracerBuilder = new Tracer.Builder(metricsRegistry, serviceName, dispatcher);
        final Optional<Collection<TracerCustomizer>> tracerCustomizers =
                Optional.ofNullable(tracerCustomizersProvider.getIfAvailable());
        tracerCustomizers.ifPresent(c -> c.forEach(customizer -> customizer.customize(tracerBuilder)));
        return tracerBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Dispatcher dispatcher(TracerSettings settings,
                                 MetricsRegistry metricsRegistry,
                                 GrpcDispatcherFactory grpcAgentFactory,
                                 HttpDispatcherFactory httpDispatcherFactory) {
        List<Dispatcher> dispatchers = new ArrayList<>();
        if (settings.getAgent() != null && settings.getAgent().isEnabled()) {
            dispatchers.add(grpcDispatcher(settings.getAgent(), metricsRegistry, grpcAgentFactory));
        }

        if (settings.getHttp() != null) {
            dispatchers.add(httpDispatcher(settings.getHttp(), metricsRegistry, httpDispatcherFactory));
        }

        if (settings.getLogger() != null) {
            dispatchers.add(loggerDispatcher(settings.getLogger(), metricsRegistry));
        }

        if (dispatchers.size() == 0) {
            return new LoggerDispatcher.Builder(metricsRegistry).withLogger("haystack").build();
        }

        return (dispatchers.size() == 1) ? dispatchers.get(0) : new ChainedDispatcher(dispatchers);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsRegistry metricsRegistry(final ObjectProvider<MeterRegistry> meterRegistryObjectProvider) {
        final MeterRegistry meterRegistry = meterRegistryObjectProvider.getIfAvailable();
        if (meterRegistry != null) {
            return new MicrometerMetricsRegistry(meterRegistry);
        }
        return new NoopMetricsRegistry();
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

    private Dispatcher grpcDispatcher(TracerSettings.AgentConfiguration agentConfiguration,
                                      MetricsRegistry metricsRegistry,
                                      GrpcDispatcherFactory factory) {
        return factory.create(metricsRegistry, agentConfiguration);
    }

    private Dispatcher httpDispatcher(TracerSettings.HttpConfiguration httpConfiguration,
                                      MetricsRegistry metricsRegistry,
                                      HttpDispatcherFactory factory) {
        return factory.create(metricsRegistry, httpConfiguration);
    }

    private Dispatcher loggerDispatcher(TracerSettings.LoggerConfiguration loggerConfiguration,
                                        MetricsRegistry metricsRegistry) {
        return new LoggerDispatcher.Builder(metricsRegistry).withLogger(loggerConfiguration.getName()).build();
    }
}
