package com.expedia.haystack.sleuth.core.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import io.grpc.netty.NegotiationType;
import lombok.Data;

@ConfigurationProperties(prefix = "spring.sleuth.haystack.client")
@Data
public class HaystackSpanProperties {

    public static long DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_MILLIS = TimeUnit.MINUTES.toMillis(30);
    public static long DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_TIME_OUT_MILLIS = TimeUnit.MINUTES.toMillis(30);

    public enum SpanDispatchMode {
        LOG, AGENT, MEMORY
    }

    private Boolean enabled = true;
    private SpanDispatchMode dispatch = SpanDispatchMode.LOG;

    @NestedConfigurationProperty
    private Grpc grpc = new Grpc();

    @NestedConfigurationProperty
    private Memory memory = new Memory();

    @Data
    class Grpc {
        private String host = "haystack-agent";
        private int port = 34000;
        private Long keepAliveInMs = DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_MILLIS;
        private Long keepAliveTimeoutMs = DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_TIME_OUT_MILLIS;
        private Boolean keepAliveWithoutCalls= true;
        private NegotiationType negotiationType = NegotiationType.PLAINTEXT;
    }

    @Data
    class Memory {
        private int maxSpans = 1000;
    }
}
