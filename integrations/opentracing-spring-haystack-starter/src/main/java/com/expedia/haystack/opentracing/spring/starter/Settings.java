package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("opentracing.spring.haystack")
public class Settings {
    private boolean enabled = true;
    private AgentConfiguration agent;
    private HttpConfiguration http;
    private LoggerConfiguration logger;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AgentConfiguration getAgent() {
        return agent;
    }

    public void setAgent(AgentConfiguration agent) {
        this.agent = agent;
    }

    public HttpConfiguration getHttp() {
        return http;
    }

    public void setHttp(HttpConfiguration http) {
        this.http = http;
    }

    public LoggerConfiguration getLogger() {
        return logger;
    }

    public void setLogger(LoggerConfiguration logger) {
        this.logger = logger;
    }

    public static class AgentConfiguration {
        private boolean enabled = false;
        private String host = "haystack-agent";
        private int port = 34100;
        private long keepAliveTimeMS = TimeUnit.SECONDS.toMillis(30);
        private long keepAliveTimeoutMS = TimeUnit.SECONDS.toMillis(30);
        private boolean keepAliveWithoutCalls = true;
        private NegotiationType negotiationType = NegotiationType.PLAINTEXT;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public long getKeepAliveTimeMS() {
            return keepAliveTimeMS;
        }

        public void setKeepAliveTimeMS(long keepAliveTimeMS) {
            this.keepAliveTimeMS = keepAliveTimeMS;
        }

        public long getKeepAliveTimeoutMS() {
            return keepAliveTimeoutMS;
        }

        public void setKeepAliveTimeoutMS(long keepAliveTimeoutMS) {
            this.keepAliveTimeoutMS = keepAliveTimeoutMS;
        }

        public boolean isKeepAliveWithoutCalls() {
            return keepAliveWithoutCalls;
        }

        public void setKeepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls;
        }

        public NegotiationType getNegotiationType() {
            return negotiationType;
        }

        public void setNegotiationType(NegotiationType negotiationType) {
            this.negotiationType = negotiationType;
        }

        GRPCAgentClient.Builder builder(MetricsRegistry metricsRegistry) {
            final GRPCAgentClient.Builder client = new GRPCAgentClient.Builder(metricsRegistry, this.host, this.port);
            client.withKeepAliveTimeMS(this.keepAliveTimeMS)
                    .withKeepAliveTimeoutMS(this.keepAliveTimeoutMS)
                    .withKeepAliveWithoutCalls(this.keepAliveWithoutCalls)
                    .withNegotiationType(this.negotiationType);
            return client;
        }
    }

    public static class HttpConfiguration {
        private String endpoint;
        private Map<String, String> headers;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }

    public static class LoggerConfiguration {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
