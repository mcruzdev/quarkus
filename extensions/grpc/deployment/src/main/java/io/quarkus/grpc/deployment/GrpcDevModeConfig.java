package io.quarkus.grpc.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

public interface GrpcDevModeConfig {

    /**
     * Start gRPC server in dev mode even if no gRPC services are implemented.
     * By default set to `true` to ease incremental development of new services using dev mode.
     */
    @WithDefault("true")
    boolean forceServerStart();
}
