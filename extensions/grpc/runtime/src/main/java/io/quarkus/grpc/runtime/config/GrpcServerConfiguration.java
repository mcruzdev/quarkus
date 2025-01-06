package io.quarkus.grpc.runtime.config;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigDocSection;

import io.smallrye.config.WithDefault;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface GrpcServerConfiguration {

    /**
     * Do we use separate HTTP server to serve gRPC requests.
     * Set this to false if you want to use new Vert.x gRPC support,
     * which uses existing Vert.x HTTP server.
     */
    @WithDefault("true")
    boolean useSeparateServer();

    /**
     * Configure XDS usage, if enabled.
     */
    @ConfigDocSection(generated = true)
    Xds xds();

    /**
     * Configure InProcess usage, if enabled.
     */
    InProcess inProcess();

    /**
     * The gRPC Server port.
     */
    @WithDefault("9000")
    int port();

    /**
     * The gRPC Server port used for tests.
     */
    @WithDefault("9001")
    int testPort();

    /**
     * The gRPC server host.
     */
    @WithDefault("0.0.0.0")
    String host();

    /**
     * The gRPC handshake timeout.
     */
    Optional<Duration> handshakeTimeout();

    /**
     * The max inbound message size in bytes.
     * <p>
     * When using a single server (using {@code quarkus.grpc.server.use-separate-server=false}), the default value is 256KB.
     * When using a separate server (using {@code quarkus.grpc.server.use-separate-server=true}), the default value is 4MB.
     */
    OptionalInt maxInboundMessageSize();

    /**
     * The max inbound metadata size in bytes
     */
    OptionalInt maxInboundMetadataSize();

    /**
     * The SSL/TLS config.
     */
    SslServerConfig ssl();

    /**
     * Disables SSL, and uses plain text instead.
     * If disabled, configure the ssl configuration.
     */
    @WithDefault("true")
    boolean plainText();

    /**
     * Whether ALPN should be used.
     */
    @WithDefault("true")
    boolean alpn();

    /**
     * Configures the transport security.
     */
    GrpcTransportSecurity transportSecurity();

    /**
     * Enables the gRPC Reflection Service.
     * By default, the reflection service is only exposed in `dev` mode.
     * This setting allows overriding this choice and enable the reflection service every time.
     */
    @WithDefault("false")
    boolean enableReflectionService();

    /**
     * Number of gRPC server verticle instances.
     * This is useful for scaling easily across multiple cores.
     * The number should not exceed the amount of event loops.
     */
    @WithDefault("1")
    int instances();

    /**
     * Configures the netty server settings.
     */
    GrpcServerNettyConfig netty();

    /**
     * gRPC compression, e.g. "gzip"
     */
    Optional<String> compression();
}
