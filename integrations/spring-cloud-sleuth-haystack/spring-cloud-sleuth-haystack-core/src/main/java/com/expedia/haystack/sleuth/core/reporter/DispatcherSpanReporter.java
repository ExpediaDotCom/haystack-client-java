package com.expedia.haystack.sleuth.core.reporter;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.UUID;

import com.expedia.haystack.sleuth.core.haystack.SpanHeader;
import com.expedia.haystack.sleuth.core.haystack.SpanMarkers;
import com.expedia.www.haystack.client.SpanContext;
import com.expedia.www.haystack.client.dispatchers.clients.Client;

import brave.internal.HexCodec;
import lombok.extern.slf4j.Slf4j;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

/**
 * This dispatcher will convert a brave span to an haystack span and uses the client passed as parameter to send the span.
 */
@Slf4j
public class DispatcherSpanReporter implements Reporter<Span> {

    private final Client client;

    public DispatcherSpanReporter(Client client) {
        this.client = client;
    }

    @Override
    public void report(Span span) {
        HaystackSpan haystackSpan = convert(span);
        log.debug("Send a new trace to Haystack - name=" + haystackSpan.getOperatioName() + "; traceId=" + haystackSpan.context().getTraceId() +
                      "; spanId=" + haystackSpan.context().getSpanId() + "; parentId=" + haystackSpan.context().getParentId() +
                      "; tags=" + haystackSpan.getTags() + "; startTime=" + haystackSpan.getStartTime() + "; endTime=" + haystackSpan.getEndTime() +
                      "; duration=" + haystackSpan.getDuration() + " ms");
        client.send(haystackSpan);
    }

    /**
     * Converts a given Brave span to a Haystack span.
     * EventId == messageId == SpanId
     * Requestid == ParentId
     * TransactionID == TraceId
     * startTime/endTime mandatory
     * hosts (host name or IP) - ideally both
     * duration - should be set (per span, not full transaction)
     */
    private HaystackSpan convert(Span span) {
        String spanName = getName(span);

        UUID spanId = new UUID(0, HexCodec.lowerHexToUnsignedLong(span.id()));
        UUID parentId = null;

        if (span.parentId() != null) {
            parentId = new UUID(0, HexCodec.lowerHexToUnsignedLong(span.parentId()));
        }

        String traceId = span.traceId();

        long lowTraceId = HexCodec.lowerHexToUnsignedLong(span.traceId());

        long traceHigh = 0L;

        if (traceId.length() == 32) {
            traceHigh = HexCodec.lowerHexToUnsignedLong(traceId, 0);
        }

        UUID transactionId = new UUID(traceHigh, lowTraceId);

        SpanContext spanContext = new SpanContext(transactionId, spanId, parentId);

        HaystackSpan haystackSpan = new HaystackSpan(spanName, spanContext, span.timestampAsLong());

        writeMedatada(haystackSpan, span);
        writeStartEndTime(haystackSpan, span);

        return haystackSpan;
    }

    private Boolean filterTags(String key) {
        return (!key.equals(SpanHeader.REQUEST)) && (!key.equals(SpanHeader.RESPONSE));
    }

    private String getName(Span span) {
        if (span.name() != null) {
            return span.name();
        }

        return "";
    }

    /**
     * Writes all the tags, the annotations as logs
     */
    private void writeMedatada(HaystackSpan haystackSpan, Span span) {
        if (!span.tags().isEmpty()) {
            span.tags().entrySet().stream().filter(entry -> filterTags(entry.getKey())).forEach(entry -> haystackSpan.setTag(entry.getKey(), entry.getValue()));
        }

        if (!span.annotations().isEmpty()) {
            span.annotations().forEach(annotation -> haystackSpan.log(annotation.timestamp(), annotation.value()));
        }

        switch (span.kind()) {
            case CLIENT:
                if (span.timestamp() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.CLIENT_SEND);
                }

                if (span.duration() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.CLIENT_RECV);
                }
                break;

            case SERVER:
                if (span.timestamp() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.SERVER_RECV);
                }

                if (span.duration() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.SERVER_SEND);
                }
                break;

            case PRODUCER:
                if (span.timestamp() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.MESSAGE_SEND);
                }

                if (span.duration() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.WIRE_SEND);
                }
                break;

            case CONSUMER:
                if ((span.timestamp() != 0L) && (span.duration() != 0L)) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.WIRE_RECV);
                    haystackSpan.log(span.timestamp(), SpanMarkers.MESSAGE_RECV);
                }

                if (span.timestamp() != 0L) {
                    haystackSpan.log(span.timestamp(), SpanMarkers.MESSAGE_RECV);
                }
                break;

            default:
                throw new AssertionError("update kind mapping");
        }

        if (span.localEndpoint() != null) {
            haystackSpan.setTag("localEndpoint", span.localEndpoint().toString());
        }

        if (span.remoteEndpoint() != null) {
            haystackSpan.setTag("remoteEndpoint", span.remoteEndpoint().toString());
        }
    }

    private void writeStartEndTime(HaystackSpan haystackSpan, Span span) {
        haystackSpan.finish(span.timestampAsLong() + span.durationAsLong());
        log.debug("transactionId=" + haystackSpan.context().getTraceId() + "; startTime=" + span.timestampAsLong() + "; endTime=" + haystackSpan.getEndTime() +
                      "; duration=" + span.durationAsLong() + " us");
    }
}

class HaystackSpan extends com.expedia.www.haystack.client.Span {
    HaystackSpan(String name, SpanContext spanContext, Long startTimeInMicros) {
        super(null, null, name, spanContext, startTimeInMicros, emptyMap(), emptyList());
    }

    @Override
    protected synchronized void finishTrace(long finishMicros) {
        try {
            super.finishTrace(finishMicros);
        } catch (Exception e) {
            // do nothing
            // It is thrown because the dispatcher is set to null and could not be overrided
        }
    }
}

