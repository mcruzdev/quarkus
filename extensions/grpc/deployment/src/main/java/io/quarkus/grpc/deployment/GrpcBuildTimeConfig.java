package io.quarkus.grpc.deployment;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.grpc")
public interface GrpcBuildTimeConfig {

    /**
     * Configuration gRPC dev mode.
     */
    @ConfigDocSection(generated = true)
    GrpcDevModeConfig devMode();

}
