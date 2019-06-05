package com.expedia.www.haystack.client.propagation;

import com.expedia.www.haystack.client.SpanContext;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class TextMapPropagatorTest {

    @Test
    public void propagatorExtractsSpanContextIdentitiesAsExpected() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        carrier.put(keyConvention.traceIdKey(), "8557731e-cce9-45c2-9485-1fd86f5116ca");
        carrier.put(keyConvention.spanIdKey(), "30898bb0-f836-43fb-ad69-44969f15e52d");
        carrier.put(keyConvention.parentIdKey(), "3a0bc1c1-504f-4f5d-907b-9b4522453bcf");

        final SpanContext spanContext = propagator.extract(carrier);

        Assert.assertEquals("trace-id was not extracted correctly",
                            "8557731e-cce9-45c2-9485-1fd86f5116ca", spanContext.getTraceId().toString());
        Assert.assertEquals("span-id was not extracted correctly",
                            "30898bb0-f836-43fb-ad69-44969f15e52d", spanContext.getSpanId().toString());
        Assert.assertEquals("parent-id was not extracted correctly",
                            "3a0bc1c1-504f-4f5d-907b-9b4522453bcf", spanContext.getParentId().toString());
    }

    @Test
    public void propagatorExtractsNonUUIDSpanContextIdentitiesAsExpected() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        carrier.put(keyConvention.traceIdKey(), "1234");
        carrier.put(keyConvention.spanIdKey(), "5678");
        carrier.put(keyConvention.parentIdKey(), "9012");

        final SpanContext spanContext = propagator.extract(carrier);

        Assert.assertEquals("trace-id was not extracted correctly",
                "1234", spanContext.getTraceId().toString());
        Assert.assertEquals("span-id was not extracted correctly",
                "5678", spanContext.getSpanId().toString());
        Assert.assertEquals("parent-id was not extracted correctly",
                "9012", spanContext.getParentId().toString());
    }

    @Test
    public void nullSpanContextIsReturnedIfTraceIdIsNull() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        carrier.put(keyConvention.traceIdKey(), null);
        carrier.put(keyConvention.spanIdKey(), "5678");
        carrier.put(keyConvention.parentIdKey(), "9012");

        final SpanContext spanContext = propagator.extract(carrier);

        Assert.assertNull("Expected a null spanContext", spanContext);
    }

    @Test
    public void nullSpanContextIsReturnedIfSpanIdIsNull() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        carrier.put(keyConvention.traceIdKey(), "1234");
        carrier.put(keyConvention.spanIdKey(), null);
        carrier.put(keyConvention.parentIdKey(), "9012");

        final SpanContext spanContext = propagator.extract(carrier);

        Assert.assertNull("Expected a null spanContext", spanContext);
    }

    @Test
    public void propagatorExtractsSpanContextIdentitiesAsExpectedRegardlessOfCase() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        carrier.put(keyConvention.traceIdKey().toLowerCase(), "8557731e-cce9-45c2-9485-1fd86f5116ca");
        carrier.put(keyConvention.spanIdKey().toLowerCase(), "30898bb0-f836-43fb-ad69-44969f15e52d");
        carrier.put(keyConvention.parentIdKey().toLowerCase(), "3a0bc1c1-504f-4f5d-907b-9b4522453bcf");
        carrier.put(keyConvention.baggagePrefix().toLowerCase() + "item-1", "foo");

        final SpanContext spanContext = propagator.extract(carrier);

        Assert.assertEquals("trace-id was not extracted correctly",
                            "8557731e-cce9-45c2-9485-1fd86f5116ca", spanContext.getTraceId().toString());
        Assert.assertEquals("span-id was not extracted correctly",
                            "30898bb0-f836-43fb-ad69-44969f15e52d", spanContext.getSpanId().toString());
        Assert.assertEquals("parent-id was not extracted correctly",
                            "3a0bc1c1-504f-4f5d-907b-9b4522453bcf", spanContext.getParentId().toString());
        Assert.assertEquals("baggage item was not extracted correctly",
                            "foo", spanContext.getBaggage().get("item-1"));
    }

    @Test
    public void propagatorInjectsSpanContextIdentitiesAsExpected() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapPropagator propagator = new TextMapPropagator.Builder().withKeyConvention(keyConvention).build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        final SpanContext spanContext = new SpanContext(UUID.fromString("8557731e-cce9-45c2-9485-1fd86f5116ca"),
                                                        UUID.fromString("30898bb0-f836-43fb-ad69-44969f15e52d"),
                                                        UUID.fromString("3a0bc1c1-504f-4f5d-907b-9b4522453bcf"));

        propagator.inject(spanContext, carrier);

        Assert.assertEquals("trace-id was not injected correctly",
                            "8557731e-cce9-45c2-9485-1fd86f5116ca", carrier.get(keyConvention.traceIdKey()));
        Assert.assertEquals("span-id was not injected correctly",
                            "30898bb0-f836-43fb-ad69-44969f15e52d", carrier.get(keyConvention.spanIdKey()));
        Assert.assertEquals("parent-id was not injected correctly",
                            "3a0bc1c1-504f-4f5d-907b-9b4522453bcf", carrier.get(keyConvention.parentIdKey()));
    }

    @Test
    public void builderUsesTheProvidedCodexForKeyAndValue() {
        final KeyConvention keyConvention = new DefaultKeyConvention();
        final TextMapCodex codex = new OneAppendingCodex();
        final TextMapPropagator propagator = new TextMapPropagator.Builder()
                .withKeyConvention(keyConvention)
                .withCodex(codex)
                .build();
        final MapBackedTextMap carrier = new MapBackedTextMap();

        final SpanContext spanContext = new SpanContext(UUID.fromString("8557731e-cce9-45c2-9485-1fd86f5116ca"),
                                                        UUID.fromString("30898bb0-f836-43fb-ad69-44969f15e52d"),
                                                        UUID.fromString("3a0bc1c1-504f-4f5d-907b-9b4522453bcf"));

        propagator.inject(spanContext, carrier);

        Assert.assertEquals("trace-id was not injected correctly",
                            "1-8557731e-cce9-45c2-9485-1fd86f5116ca",
                            carrier.get("1-" + keyConvention.traceIdKey()));
        Assert.assertEquals("span-id was not injected correctly",
                            "1-30898bb0-f836-43fb-ad69-44969f15e52d",
                            carrier.get("1-" + keyConvention.spanIdKey()));
        Assert.assertEquals("parent-id was not injected correctly",
                            "1-3a0bc1c1-504f-4f5d-907b-9b4522453bcf",
                            carrier.get("1-" + keyConvention.parentIdKey()));
    }

    private class OneAppendingCodex extends  TextMapCodex {
        @Override
        public String encode(String value) {
            return "1-" + value;
        }

        @Override
        public String decode(String value) {
            return value.substring(2);
        }
    }
}
