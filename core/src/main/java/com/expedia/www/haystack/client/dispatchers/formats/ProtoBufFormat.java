/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client.dispatchers.formats;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.open.tracing.Tag.TagType;
import com.expedia.www.haystack.client.LogData;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.SpanContext;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.stream.Collectors;

public class ProtoBufFormat implements Format<com.expedia.open.tracing.Span> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoBufFormat.class);

    @Override
    public com.expedia.open.tracing.Span format(Span span) {
        com.expedia.open.tracing.Span.Builder builder = com.expedia.open.tracing.Span.newBuilder();

        SpanContext context = span.context();
        builder.setTraceId(context.getTraceId().toString())
                .setSpanId(context.getSpanId().toString());

        if (context.getParentId() != null) {
            builder.setParentSpanId(context.getParentId().toString());
        }

        builder.setServiceName(span.getServiceName())
                .setOperationName(span.getOperatioName())
                .setStartTime(span.getStartTime());

        if (span.getDuration() != null) {
            builder.setDuration(span.getDuration());
        }

        builder.addAllLogs(span.getLogs().stream()
                           .map(this::buildLog)
                           .collect(Collectors.toList()))
                .addAllTags(span.getTags().entrySet().stream()
                           .map(e -> buildTag(e.getKey(), e.getValue()))
                           .collect(Collectors.toList()))
                .addAllTags(context.getBaggage().entrySet().stream() // add the baggage items as tags for now
                           .map(e -> buildTag(e.getKey(), e.getValue()))
                           .collect(Collectors.toList()));

        return builder.build();
    }

    protected Log buildLog(final LogData log) {
        final Log.Builder builder = Log.newBuilder()
            .setTimestamp(log.getTimestamp());

        if (log.getFields() != null ) {
            builder.addAllFields(log.getFields().entrySet().stream()
                                 .map(e -> buildTag(e.getKey(), e.getValue()))
                                 .collect(Collectors.toList()));
        }
        return builder.build();
    }

    protected Tag buildTag(String key, Object value) {
        Tag.Builder builder = Tag.newBuilder().setKey(key);

        if (value == null) {
            // just a message collected; adding an empty payload
            builder.setType(TagType.STRING);
            builder.setVStr("");
        } else if (value instanceof String) {
            builder.setType(TagType.STRING);
            builder.setVStr((String) value);
        } else if (value instanceof Double || value instanceof Float) {
            builder.setType(TagType.DOUBLE);
            builder.setVDouble(((Number) value).doubleValue());
        } else if (value instanceof Long || value instanceof Integer || value instanceof Short) {
            builder.setType(TagType.LONG);
            builder.setVLong(((Number) value).longValue());
        } else if (value instanceof Boolean) {
            builder.setType(TagType.BOOL);
            builder.setVBool((Boolean) value);
        } else {
            builder.setType(TagType.BINARY);

            try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (final ObjectOutputStream os = new ObjectOutputStream(out)) {
                    os.writeObject(value);
                    builder.setVBytes(ByteString.copyFrom(out.toByteArray()));
                }
            } catch (IOException e) {
                LOGGER.warn("Conversion of tag to binary failed with exception: {}", e);
                // can't do much so set it to an EMPTY payload
                builder.setVBytes(ByteString.EMPTY);
            }
        }

        return builder.build();
    }
}
