package com.expedia.haystack.opentracing.spring.starter

import java.util

import com.expedia.haystack.opentracing.spring.starter.TracerSettings.{AgentConfiguration, HttpConfiguration, LoggerConfiguration}
import com.expedia.haystack.opentracing.spring.starter.support.{GrpcDispatcherFactory, HttpDispatcherFactory, TracerCustomizer}
import com.expedia.www.haystack.client.dispatchers.{ChainedDispatcher, Dispatcher, LoggerDispatcher}
import com.expedia.www.haystack.client.metrics.micrometer.MicrometerMetricsRegistry
import com.expedia.www.haystack.client.metrics.{MetricsRegistry, NoopMetricsRegistry}
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentracing.Tracer
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}
import org.scalatest.easymock.EasyMockSugar
import org.springframework.beans.factory.ObjectProvider
import org.easymock.EasyMock._

import scala.collection.JavaConverters._

class TracerConfigurerSpec extends FunSpec with GivenWhenThen with Matchers with EasyMockSugar {
  describe("configuring a tracer") {
    it("should build a tracer with given metricsRegistry and dispatcher") {
      Given("all required arguments")
      val serviceName = "foo-service"
      val dispatcher = niceMock[Dispatcher]
      val metricsRegistry = new NoopMetricsRegistry
      val objectProvider = niceMock[ObjectProvider[util.Collection[TracerCustomizer]]]
      val tracerConfigurer = new TracerConfigurer
      When("a new tracer is attempted")
      val tracer: Tracer = tracerConfigurer.tracer(serviceName, dispatcher, metricsRegistry, objectProvider)
      Then("it should build a new one as expected")
      tracer should not be null
    }
    it("should call the customizers if provided before building a tracer instance") {
      Given("all required arguments")
      val serviceName = "foo-service"
      val dispatcher = niceMock[Dispatcher]
      val metricsRegistry = new NoopMetricsRegistry
      val objectProvider = mock[ObjectProvider[util.Collection[TracerCustomizer]]]
      val tracerConfigurer = new TracerConfigurer
      And("a tracer customer")
      val customizer = mock[TracerCustomizer]
      expecting {
        customizer.customize(anyObject[com.expedia.www.haystack.client.Tracer.Builder]).once()
        objectProvider.getIfAvailable.andReturn(List(customizer).asJava).once()
      }
      replay(customizer, objectProvider)
      When("a new tracer is attempted")
      val tracer: Tracer = tracerConfigurer.tracer(serviceName, dispatcher, metricsRegistry, objectProvider)
      Then("it should call the customizer and build a new one as expected")
      verify(customizer, objectProvider)
      tracer should not be null
    }
    it("should create a default dispatcher if none is configured in the settings") {
      Given("a valid settings with no dispatcher configured")
      val settings = new TracerSettings
      val metricsRegistry = new NoopMetricsRegistry
      val grpcDispatcherFactory = niceMock[GrpcDispatcherFactory]
      val httpDispatcherFactory = niceMock[HttpDispatcherFactory]
      val tracerConfigurer = new TracerConfigurer
      When("a dispatcher is created")
      val dispatcher = tracerConfigurer.dispatcher(settings, metricsRegistry,
        grpcDispatcherFactory, httpDispatcherFactory)
      Then("it should create a default dispatcher")
      dispatcher should not be null
      dispatcher shouldBe a [LoggerDispatcher]
    }
    it("should create a chained dispatcher if there are more than one dispatcher in settings") {
      Given("a valid settings with two dispatchers configured")
      val settings = new TracerSettings
      settings.getDispatchers.setLogger(new LoggerConfiguration)
      val http = new HttpConfiguration
      http.setEndpoint("https://localhost")
      http.setHeaders(Map[String, String]().asJava)
      settings.getDispatchers.setHttp(http)
      val metricsRegistry = new NoopMetricsRegistry
      val tracerConfigurer = new TracerConfigurer
      When("a dispatcher is created")
      val dispatcher = tracerConfigurer.dispatcher(settings, metricsRegistry,
        tracerConfigurer.grpcDispatcherFactory(), tracerConfigurer.httpDispatcherFactory())
      Then("it should create a chained dispatcher")
      dispatcher should not be null
      dispatcher shouldBe a [ChainedDispatcher]
    }
    it("should use grpc dispatcher factory to create a grpc based remote dispatcher") {
      Given("a valid settings with grpc agent dispatcher configured")
      val settings = new TracerSettings
      val agent = new AgentConfiguration
      agent.setEnabled(true)
      settings.getDispatchers.setAgent(agent)
      val metricsRegistry = new NoopMetricsRegistry
      val grpcDispatcherFactory = mock[GrpcDispatcherFactory]
      val httpDispatcherFactory = niceMock[HttpDispatcherFactory]
      val grpcDispatcher = mock[Dispatcher]
      val tracerConfigurer = new TracerConfigurer
      expecting {
        grpcDispatcherFactory.create(metricsRegistry, agent).andReturn(grpcDispatcher).once()
      }
      replay(grpcDispatcherFactory)
      When("a dispatcher is created")
      val dispatcher = tracerConfigurer.dispatcher(settings, metricsRegistry,
        grpcDispatcherFactory, httpDispatcherFactory)
      Then("it should create a dispatcher by calling grpcAgentFactory")
      dispatcher should not be null
      dispatcher should be (grpcDispatcher)
      verify(grpcDispatcherFactory)
    }
    it("should use http dispatcher factory to create a http based remote dispatcher") {
      Given("a valid settings with http dispatcher configured")
      val settings = new TracerSettings
      val http = new HttpConfiguration
      http.setEndpoint("https://localhost")
      http.setHeaders(Map[String, String]().asJava)
      settings.getDispatchers.setHttp(http)
      val metricsRegistry = new NoopMetricsRegistry
      val grpcDispatcherFactory = mock[GrpcDispatcherFactory]
      val httpDispatcherFactory = mock[HttpDispatcherFactory]
      val httpDispatcher = mock[Dispatcher]
      val tracerConfigurer = new TracerConfigurer
      expecting {
        httpDispatcherFactory.create(metricsRegistry, http).andReturn(httpDispatcher).once()
      }
      replay(httpDispatcherFactory)
      When("a dispatcher is created")
      val dispatcher = tracerConfigurer.dispatcher(settings, metricsRegistry,
        grpcDispatcherFactory, httpDispatcherFactory)
      Then("it should create a dispatcher by calling httpAgentFactory")
      dispatcher should not be null
      dispatcher should be (httpDispatcher)
      verify(httpDispatcherFactory)
    }
    it("should create a metrics registry from a meter registry instance") {
      Given("a micrometer MeterRegistry")
      val meterRegistry = new SimpleMeterRegistry()
      val tracerConfigurer = new TracerConfigurer
      val objectProvider = mock[ObjectProvider[MeterRegistry]]
      expecting {
        objectProvider.getIfAvailable.andReturn(meterRegistry).once()
      }
      replay(objectProvider)
      When("a MetricsRegistry is created")
      val metricsRegistry = tracerConfigurer.metricsRegistry(objectProvider)
      Then("it should wrap and return a MetricsRegistry")
      metricsRegistry should not be null
      metricsRegistry shouldBe a [MicrometerMetricsRegistry]
      verify(objectProvider)
    }
    it("should create a no-op metrics registry if no meterregistry instance is provided") {
      Given("No micrometer MeterRegistry")
      val tracerConfigurer = new TracerConfigurer
      val objectProvider = mock[ObjectProvider[MeterRegistry]]
      expecting {
        objectProvider.getIfAvailable.andReturn(null).once()
      }
      replay(objectProvider)
      When("a MetricsRegistry is created")
      val metricsRegistry = tracerConfigurer.metricsRegistry(objectProvider)
      Then("it should return a Noop MetricsRegistry")
      metricsRegistry should not be null
      metricsRegistry shouldBe a [NoopMetricsRegistry]
      verify(objectProvider)
    }
  }
}
