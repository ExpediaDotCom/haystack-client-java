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

package com.expedia.www.opencensus.exporter.trace

import java.util.Collections

import com.expedia.www.haystack.client.dispatchers.clients.Client
import com.expedia.www.haystack.client.metrics.{Metrics, NoopMetricsRegistry}
import com.google.common.collect.{ImmutableMap, Lists}
import io.opencensus.common.Timestamp
import io.opencensus.trace.export.SpanData
import io.opencensus.trace.{MessageEvent, Tracestate, _}
import org.easymock.EasyMock
import org.scalatest._
import org.scalatest.easymock.EasyMockSugar

import scala.collection.JavaConverters._

class HaystackExporterHandlerSpec extends FunSpec with GivenWhenThen with Matchers with EasyMockSugar {

  private val FF = 0xFF.toByte
  describe("Haystack Exporter Handler") {
    it("should convert and dispatch opencensus modeled spans to haystack remote client") {
      val client = mock[Client[com.expedia.open.tracing.Span]]
      val capturedSpan = EasyMock.newCapture[com.expedia.open.tracing.Span]()

      expecting {
        client.send(EasyMock.capture(capturedSpan)).andReturn(true)
      }

      whenExecuting(client) {
        val handler = new HaystackExporterHandler(client, "dummy-service", new Metrics(new NoopMetricsRegistry))
        handler.export(Collections.singleton(spanData(System.currentTimeMillis() - 10l*1000, System.currentTimeMillis())))
      }

      val haystackSpan = capturedSpan.getValue
      haystackSpan.getServiceName shouldEqual "dummy-service"
      haystackSpan.getOperationName shouldEqual "myop"
      haystackSpan.getParentSpanId shouldEqual "9223372036854775807"
      haystackSpan.getSpanId shouldEqual "256"
      haystackSpan.getTraceId shouldEqual "ff000000-0000-0000-0000-000000000001"
      haystackSpan.getTagsCount shouldBe 4
      haystackSpan.getTagsList.asScala.find(_.getKey == "BOOL").get.getVBool shouldBe false
      haystackSpan.getTagsList.asScala.find(_.getKey == "STRING").get.getVStr shouldEqual "hello world!"
      haystackSpan.getTagsList.asScala.find(_.getKey == "LONG").get.getVLong shouldBe 9223372036854775807l
      haystackSpan.getTagsList.asScala.find(_.getKey == "span.kind").get.getVStr shouldEqual "server"

      haystackSpan.getLogsCount shouldBe 2
      haystackSpan.getDuration shouldBe 10l * 1000 * 1000l
      haystackSpan.getLogs(0).getTimestamp shouldBe 1234567890000000l

      val logTags = haystackSpan.getLogs(0).getFieldsList.asScala
      logTags.find(_.getKey == "bool").get.getVBool shouldBe true
      logTags.find(_.getKey == "string").get.getVStr shouldEqual "hello asia!"
      logTags.find(_.getKey == "message").get.getVStr shouldEqual "annotation #1"
      haystackSpan.getLogs(1).getTimestamp shouldBe 1234567890000000l
    }

    it("should swallow and only log exception if remote client is failing!") {
      val client = mock[Client[com.expedia.open.tracing.Span]]
      val capturedSpan = EasyMock.newCapture[com.expedia.open.tracing.Span]()

      expecting {
        client.send(EasyMock.capture(capturedSpan)).andThrow(new RuntimeException("fail to connect to remote box!")).times(1)
      }

      whenExecuting(client) {
        val handler = new HaystackExporterHandler(client, "dummy-service", new Metrics(new NoopMetricsRegistry))
        handler.export(Collections.singleton(spanData(System.currentTimeMillis() - 10l*1000, System.currentTimeMillis())))
      }
    }
  }

  private def spanData(startTime: Long, endTime: Long): SpanData = {
    import io.opencensus.trace.Span.Kind
    import io.opencensus.trace.export.SpanData
    SpanData.create(
      sampleSpanContext,
      SpanId.fromBytes(Array[Byte](0x7F.toByte, FF, FF, FF, FF, FF, FF, FF)),
      true,
      "myop",
      Kind.SERVER,
      Timestamp.fromMillis(startTime),
      SpanData.Attributes.create(sampleAttributes, 0),
      SpanData.TimedEvents.create(Collections.singletonList(sampleAnnotation), 0),
      SpanData.TimedEvents.create(Collections.singletonList(sampleMessageEvent), 0),
      SpanData.Links.create(sampleLinks, 0), 0, Status.OK, Timestamp.fromMillis(endTime))
  }


  private def sampleSpanContext =
    SpanContext.create(
      TraceId.fromBytes(Array[Byte](FF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)),
      SpanId.fromBytes(Array[Byte](0, 0, 0, 0, 0, 0, 1, 0)),
      TraceOptions.builder.setIsSampled(true).build,
      Tracestate.builder.build)

  private def sampleAttributes = ImmutableMap.of(
    "BOOL", AttributeValue.booleanAttributeValue(false),
    "LONG", AttributeValue.longAttributeValue(Long.MaxValue),
    "STRING", AttributeValue.stringAttributeValue("hello world!"))

  private def sampleAnnotation = SpanData.TimedEvent.create(Timestamp.create(1234567890L, 100), Annotation.fromDescriptionAndAttributes("annotation #1", ImmutableMap.of("bool", AttributeValue.booleanAttributeValue(true), "long", AttributeValue.longAttributeValue(12345l), "string", AttributeValue.stringAttributeValue("hello asia!"))))

  private def sampleMessageEvent = SpanData.TimedEvent.create(Timestamp.create(1234567890L, 500), MessageEvent.builder(MessageEvent.Type.SENT, 4L).setCompressedMessageSize(50).setUncompressedMessageSize(100).build)

  private def sampleLinks =
    Lists.newArrayList(
      Link.fromSpanContext(
        SpanContext.create(TraceId.fromBytes(Array[Byte](FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, 0)),
          SpanId.fromBytes(Array[Byte](0, 0, 0, 0, 0, 0, 2, 0)),
          TraceOptions.builder.setIsSampled(false).build, Tracestate.builder.build),
        Link.Type.CHILD_LINKED_SPAN, ImmutableMap.of("Bool", AttributeValue.booleanAttributeValue(true), "Long", AttributeValue.longAttributeValue(299792458L), "String", AttributeValue.stringAttributeValue("hello-asia"))))
}