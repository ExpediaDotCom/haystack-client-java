package com.expedia.haystack.dropwizard.configuration;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.expedia.open.tracing.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentClient;
import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A factory for configuring and building {@link GRPCAgentClient} instances.
 * 
 * Configaruation options are as follows:
 * <table>
 * <tr>
 *  <td>Name</td>
 *  <td>Default</td>
 *  <td>Description</td>
 * </tr>
 *
 * <tr>
 *  <td>host</td>
 *  <td>localhost</td>
 *  <td>The host to create a GRPC channel to</td>
 * </tr>
 *
 * <tr>
 *  <td>port</td>
 *  <td>None</td>
 *  <td>The port to sue when creating a GRPC channel</td>
 * </tr>
 *
 * <tr>
 *  <td>keepAliveTimeMS</td>
 *  <td>None</td>
 *  <td></td>
 * </tr>
 *
 * <tr>
 *  <td>keepAliveTimeoutMS</td>
 *  <td>None</td>
 *  <td></td>
 * </tr>
 *
 * <tr>
 *  <td>keepAliveWithoutCalls</td>
 *  <td>None</td>
 *  <td></td>
 * </tr>
 *
 * <tr>
 *   <td colspan="3">See {@link BaseClientFactory} for more options, if any.</td>
 * </tr>
 *
 * <tr>
 *   <td colspan="3">See {@link ClientFactory} for more options, if any.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("agent")
public class AgentClientFactory extends BaseClientFactory {

    @NotEmpty
    private String host = "localhost";

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer port;

    @Nullable
    private Long keepAliveTimeMS;

    @Nullable
    private Long  keepAliveTimeoutMS;

    @Nullable
    private Boolean keepAliveWithoutCalls;

    public AgentClientFactory() {
        setFormat(new ProtoBufFormatFactory());
    }

    @Override
    public Client build() {
        GRPCAgentClient.Builder grpcBuilder = new GRPCAgentClient.Builder(host, port);
        grpcBuilder.withFormat((Format<Span>) getFormat().build());

        if (keepAliveTimeMS != null) {
            grpcBuilder.withKeepAliveTimeMS(keepAliveTimeMS);
        }
        if (keepAliveTimeoutMS != null) {
            grpcBuilder.withKeepAliveTimeoutMS(keepAliveTimeoutMS);
        }
        if (keepAliveWithoutCalls != null) {
            grpcBuilder.withKeepAliveWithoutCalls(keepAliveWithoutCalls);
        }

        return grpcBuilder.build();
    }

    /**
     * @return the host
     */
    @JsonProperty
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    @JsonProperty
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    @JsonProperty
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the keepAliveTimeMS
     */
    @JsonProperty
    public Long getKeepAliveTimeMS() {
        return keepAliveTimeMS;
    }

    /**
     * @param keepAliveTimeMS the keepAliveTimeMS to set
     */
    @JsonProperty
    public void setKeepAliveTimeMS(Long keepAliveTimeMS) {
        this.keepAliveTimeMS = keepAliveTimeMS;
    }

    /**
     * @return the keepAliveTimeoutMS
     */
    @JsonProperty
    public Long getKeepAliveTimeoutMS() {
        return keepAliveTimeoutMS;
    }

    /**
     * @param keepAliveTimeoutMS the keepAliveTimeoutMS to set
     */
    @JsonProperty
    public void setKeepAliveTimeoutMS(Long keepAliveTimeoutMS) {
        this.keepAliveTimeoutMS = keepAliveTimeoutMS;
    }

    /**
     * @return the keepAliveWithoutCalls
     */
    @JsonProperty
    public Boolean getKeepAliveWithoutCalls() {
        return keepAliveWithoutCalls;
    }

    /**
     * @param keepAliveWithoutCalls the keepAliveWithoutCalls to set
     */
    @JsonProperty
    public void setKeepAliveWithoutCalls(Boolean keepAliveWithoutCalls) {
        this.keepAliveWithoutCalls = keepAliveWithoutCalls;
    }

}
