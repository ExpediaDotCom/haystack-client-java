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

package com.www.expedia.opencensus.exporter.trace


import java.util
import java.util.{Collections, Random}

import com.www.expedia.opencensus.exporter.trace.config.GrpcAgentDispatcherConfig
import io.opencensus.trace.samplers.Samplers
import io.opencensus.trace.{AttributeValue, Status, Tracer, Tracing}
import org.apache.kafka.clients.consumer.ConsumerConfig._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.scalatest.{BeforeAndAfterAll, FunSpec, GivenWhenThen, Matchers}

import scala.collection.JavaConverters._

class HaystackExporterIntegrationSpec extends FunSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  private val OPERATION_NAME = "/search"
  private val SERVICE_NAME = "my-service"
  private var consumer: KafkaConsumer[String, Array[Byte]] = _

  override def beforeAll(): Unit = {
    Thread.sleep(20000)
    val config: java.util.Map[String, Object] = new util.HashMap()
    config.put(BOOTSTRAP_SERVERS_CONFIG, "kafkasvc:9092")
    config.put(GROUP_ID_CONFIG, "integration_test")
    config.put(AUTO_OFFSET_RESET_CONFIG, "earliest")
    consumer = new KafkaConsumer(config, new StringDeserializer(), new ByteArrayDeserializer())
    consumer.subscribe(Collections.singleton("proto-spans"))
  }

  override def afterAll(): Unit = {
    HaystackTraceExporter.unregister()
  }

  private def generateTrace(tracer: Tracer) = {
    val spanBuilder = tracer.spanBuilder(OPERATION_NAME).setRecordEvents(true).setSampler(Samplers.alwaysSample())
    val spanDurationInMillis = new Random().nextInt(10) + 1

    val scopedSpan = spanBuilder.startScopedSpan
    try {
      tracer.getCurrentSpan.addAnnotation("start searching")
      Thread.sleep(spanDurationInMillis)
      tracer.getCurrentSpan.putAttribute("foo", AttributeValue.stringAttributeValue("bar"))
      tracer.getCurrentSpan.putAttribute("items", AttributeValue.longAttributeValue(10l))
      tracer.getCurrentSpan.addAnnotation("done searching")
    } catch {
      case _: Exception =>
        tracer.getCurrentSpan.addAnnotation("Exception thrown when processing video.")
        tracer.getCurrentSpan.setStatus(Status.UNKNOWN)
    } finally {
      scopedSpan.close()
    }
  }

  describe("Integration Test with haystack and opencensus") {
    it ("should dispatch the spans to haystack-agent") {
      HaystackTraceExporter.createAndRegister(new GrpcAgentDispatcherConfig("haystack-agent", 35000), SERVICE_NAME)
      val tracer = Tracing.getTracer

      generateTrace(tracer)
      Thread.sleep(2000)
      generateTrace(tracer)

      // wait for few sec to let the span reach kafka
      Thread.sleep(10000)

      val records = consumer.poll(2000)
      records.count > 1 shouldBe true
      val record = records.iterator().next()
      val protoSpan = com.expedia.open.tracing.Span.parseFrom(record.value())
      protoSpan.getTraceId shouldEqual record.key()
      protoSpan.getServiceName shouldEqual SERVICE_NAME
      protoSpan.getOperationName shouldEqual OPERATION_NAME
      protoSpan.getTagsCount shouldBe 2
      protoSpan.getTagsList.asScala.find(_.getKey == "foo").get.getVStr shouldEqual "bar"
      protoSpan.getTagsList.asScala.find(_.getKey == "items").get.getVLong shouldBe 10
    }
  }
}
