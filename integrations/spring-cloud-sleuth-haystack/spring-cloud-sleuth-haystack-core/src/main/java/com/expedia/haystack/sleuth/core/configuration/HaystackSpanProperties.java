package com.expedia.haystack.sleuth.core.configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import io.grpc.netty.NegotiationType;
import lombok.Data;

@ConfigurationProperties(prefix = "spring.sleuth.haystack.client")
@Data
@Validated
public class HaystackSpanProperties {

    public static Duration DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_MILLIS = Duration.ofMinutes(30);
    public static Duration DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_TIME_OUT_MILLIS = Duration.ofMinutes(30);

    public enum SpanDispatchMode {
        LOGGER, GRPC
    }

    private SpanDispatchMode dispatch = SpanDispatchMode.LOGGER;

    @NestedConfigurationProperty
    private Grpc grpc = new Grpc();

    @Data
    class Grpc {
        @NotEmpty
        private String host = "haystack-agent";

        @NotEmpty
        @Min(1)
        @Max(65535)
        private int port = 34000;

        private Duration keepAliveInMs = DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_MILLIS;
        private Duration keepAliveTimeoutMs = DEFAULT_HAYSTACK_AGENT_KEEP_ALIVE_TIME_OUT_MILLIS;
        private Boolean keepAliveWithoutCalls= true;
        private NegotiationType negotiationType = NegotiationType.PLAINTEXT;
    }
}
