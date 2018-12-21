package com.expedia.haystack.opentracing.spring.starter.support;

import com.expedia.haystack.opentracing.spring.starter.TracerSettings;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;

public interface GrpcDispatcherFactory {
    Dispatcher create(MetricsRegistry metricsRegistry, TracerSettings.AgentConfiguration agentConfiguration);
}
