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

package com.expedia.www.opencensus.exporter.trace;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.google.common.primitives.Longs;
import com.google.errorprone.annotations.MustBeClosed;
import io.opencensus.common.Function;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.*;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@ThreadSafe
final class HaystackExporterHandler extends SpanExporter.Handler {
    private static final String EXPORT_SPAN_NAME = "ExportHaystackTraces";
    private static final Tag SPAN_KIND_SERVER_TAG = Tag.newBuilder().setKey("span.kind").setType(Tag.TagType.STRING).setVStr("server").build();
    private static final Tag SPAN_KIND_CLIENT_TAG = Tag.newBuilder().setKey("span.kind").setType(Tag.TagType.STRING).setVStr("client").build();
    private static final String DESCRIPTION = "message";
    private static final String MESSAGE_EVENT_ID = "id";

    private static final String RECEIVED_MESSAGE_EVENT_NAME = "received message id";
    private static final String SENT_MESSAGE_EVENT_NAME = "sent message id";
    private static final String MESSAGE_EVENT_COMPRESSED_SIZE = "compressed_size";
    private static final String MESSAGE_EVENT_UNCOMPRESSED_SIZE = "uncompressed_size";

    private static final Logger logger = Logger.getLogger(HaystackExporterHandler.class.getName());
    private static final Tracer tracer = Tracing.getTracer();

    private static final Function<Object, Tag.Builder> defaultAttributeConverter = o -> Tag.newBuilder().setVStr(o.toString()).setType(Tag.TagType.STRING);
    private static final Function<String, Tag.Builder> stringAttributeConverter = s -> Tag.newBuilder().setVStr(s).setType(Tag.TagType.STRING);
    private static final Function<Boolean, Tag.Builder> boolAttributeConverter = s -> Tag.newBuilder().setVBool(s).setType(Tag.TagType.BOOL);
    private static final Function<Long, Tag.Builder> longAttributeConverter = s -> Tag.newBuilder().setVLong(s).setType(Tag.TagType.LONG);
    private static final Function<Double, Tag.Builder> doubleAttributeConverter = s -> Tag.newBuilder().setVDouble(s).setType(Tag.TagType.DOUBLE);

    private final Client<com.expedia.open.tracing.Span> sender;
    private final String serviceName;

    HaystackExporterHandler(final Client<com.expedia.open.tracing.Span> sender,
                            final String serviceName,
                            final Metrics metrics) {
        checkNotNull(sender, "haystack span sender must NOT be null.");
        checkNotNull(serviceName, "service name must NOT be null");
        this.sender = sender;
        this.serviceName = serviceName;
    }

    @Override
    public void export(Collection<SpanData> spanDataList) {
        try (final Scope exportScope = newExportScope()) {
            doExport(spanDataList);
        } catch (Exception e) {
            tracer
                    .getCurrentSpan() // exportScope above.
                    .setStatus(Status.UNKNOWN.withDescription(getMessageOrDefault(e)));
            logger.log(Level.WARNING, "Failed to export traces to Haystack: " + e);
        }
    }

    @MustBeClosed
    private static Scope newExportScope() {
        return tracer.spanBuilder(EXPORT_SPAN_NAME).startScopedSpan();
    }

    private void doExport(final Collection<SpanData> spanDataList) {
        for (final SpanData spanData : spanDataList) {
            final com.expedia.open.tracing.Span haystackSpan = spanDataToHaystackProtoSpan(spanData);
            this.sender.send(haystackSpan);
        }
    }

    private com.expedia.open.tracing.Span spanDataToHaystackProtoSpan(final SpanData spanData) {
        final long startTimeInMicros = timestampToMicros(spanData.getStartTimestamp());
        final long endTimeInMicros = timestampToMicros(spanData.getEndTimestamp());

        final SpanContext context = spanData.getContext();

        final String parentSpanId;
        if (spanData.getParentSpanId() != null) {
            parentSpanId = byteArrayToLongString(spanData.getParentSpanId().getBytes());
        } else {
            parentSpanId = "";
        }

        final List<Tag> tags = buildTagsFromAttributes(spanData.getAttributes().getAttributeMap());

        // add span.kind tag
        addSpanKindToTag(spanData.getKind()).ifPresent(tags::add);

        final List<Log> logs = timedEventsToLogs(spanData.getAnnotations().getEvents(), spanData.getMessageEvents().getEvents());

        return com.expedia.open.tracing.Span.newBuilder()
                .setTraceId(byteArrayToLongString(context.getTraceId().getBytes()))
                .setSpanId(byteArrayToLongString(context.getSpanId().getBytes()))
                .setParentSpanId(parentSpanId)
                .setServiceName(serviceName)
                .setOperationName(spanData.getName())
                .setStartTime(startTimeInMicros)
                .setDuration(endTimeInMicros - startTimeInMicros)
                .addAllTags(tags)
                .addAllLogs(logs)
                .build();
    }

    private List<Log> timedEventsToLogs(final List<SpanData.TimedEvent<Annotation>> annotations,
                                        final List<SpanData.TimedEvent<MessageEvent>> messageEvents) {
        final List<Log> logs = new ArrayList<>();

        for (final SpanData.TimedEvent<Annotation> event : annotations) {
            final long timestampsInMicros = timestampToMicros(event.getTimestamp());
            final List<Tag> tags = buildTagsFromAttributes(event.getEvent().getAttributes());
            tags.add(Tag.newBuilder().setKey(DESCRIPTION).setVStr(event.getEvent().getDescription()).setType(Tag.TagType.STRING).build());
            final Log log = Log.newBuilder().setTimestamp(timestampsInMicros).addAllFields(tags).build();
            logs.add(log);
        }

        for (final SpanData.TimedEvent<MessageEvent> event : messageEvents) {
            final long timestampsInMicros = timestampToMicros(event.getTimestamp());
            final List<Tag> tags = new ArrayList<>(4);
            tags.add(addTagWithLongValue(MESSAGE_EVENT_ID, event.getEvent().getMessageId()));
            tags.add(addTagWithLongValue(MESSAGE_EVENT_COMPRESSED_SIZE, event.getEvent().getCompressedMessageSize()));
            tags.add(addTagWithLongValue(MESSAGE_EVENT_UNCOMPRESSED_SIZE, event.getEvent().getUncompressedMessageSize()));
            if (event.getEvent().getType() == MessageEvent.Type.RECEIVED) {
                tags.add(addTagWithLongValue(RECEIVED_MESSAGE_EVENT_NAME, event.getEvent().getMessageId()));
            } else {
                tags.add(addTagWithLongValue(SENT_MESSAGE_EVENT_NAME, event.getEvent().getMessageId()));
            }
            final Log log = Log.newBuilder().setTimestamp(timestampsInMicros).addAllFields(tags).build();
            logs.add(log);
        }

        return logs;
    }

    private Tag addTagWithLongValue(final String messageEventId,
                                    final long value) {
        return Tag.newBuilder().setKey(messageEventId).setVLong(value).setType(Tag.TagType.LONG).build();
    }

    private List<Tag> buildTagsFromAttributes(final Map<String, AttributeValue> attributes) {
        final List<Tag> tags = new ArrayList<>(attributes.size() + 2);
        attributes.forEach((attrKey, attrVal) -> {
            final Tag.Builder tagBuilder = attrVal.match(
                    stringAttributeConverter,
                    boolAttributeConverter,
                    longAttributeConverter,
                    doubleAttributeConverter,
                    defaultAttributeConverter);
            tagBuilder.setKey(attrKey);
            tags.add(tagBuilder.build());
        });
        return tags;
    }

    private String byteArrayToLongString(final byte[] idBuffer) {
        if (idBuffer.length == 16) {
            final Long lowLong = Longs.fromBytes(
                    idBuffer[8],
                    idBuffer[9],
                    idBuffer[10],
                    idBuffer[11],
                    idBuffer[12],
                    idBuffer[13],
                    idBuffer[14],
                    idBuffer[15]);

            final Long highLong = Longs.fromBytes(
                    idBuffer[0],
                    idBuffer[1],
                    idBuffer[2],
                    idBuffer[3],
                    idBuffer[4],
                    idBuffer[5],
                    idBuffer[6],
                    idBuffer[7]);
            return new UUID(highLong, lowLong).toString();
        } else {
            return String.valueOf(Longs.fromByteArray(idBuffer));
        }
    }

    private static long timestampToMicros(final @Nullable Timestamp timestamp) {
        return (timestamp == null)
                ? 0L
                : SECONDS.toMicros(timestamp.getSeconds()) + NANOSECONDS.toMicros(timestamp.getNanos());
    }

    private static Optional<Tag> addSpanKindToTag(@Nullable final Span.Kind kind) {
        if (kind == null) {
            return Optional.empty();
        }
        switch (kind) {
            case CLIENT:
                return Optional.of(SPAN_KIND_CLIENT_TAG);
            case SERVER:
                return Optional.of(SPAN_KIND_SERVER_TAG);
            default:
                return Optional.empty();
        }
    }

    private static String getMessageOrDefault(final Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
