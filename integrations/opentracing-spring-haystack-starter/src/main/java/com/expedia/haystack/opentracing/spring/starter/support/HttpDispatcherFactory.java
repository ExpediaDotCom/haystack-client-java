package com.expedia.haystack.opentracing.spring.starter.support;

import com.expedia.haystack.opentracing.spring.starter.Configuration;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;

public interface HttpDispatcherFactory {
    Dispatcher create(MetricsRegistry metricsRegistry, Configuration.HttpConfiguration httpConfiguration);
}