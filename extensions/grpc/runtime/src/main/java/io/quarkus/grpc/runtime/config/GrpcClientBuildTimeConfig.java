package io.quarkus.grpc.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.grpc")
public interface GrpcClientBuildTimeConfig {

    /**
     * If set to true, and a Stork load balancer is used, connections with all available service instances will be
     * requested proactively. This means better load balancing at the cost of having multiple active connections.
     */
    @WithDefault("true")
    boolean storkProactiveConnections();
}
