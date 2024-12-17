package io.quarkus.swaggerui.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.swagger-ui")
public interface SwaggerUiRuntimeConfig {

    /**
     * If Swagger UI is included, it should be enabled/disabled. By default, Swagger UI is enabled if it is included (see
     * {@code always-include}).
     */
    @WithDefault("true")
    boolean enable();

}
