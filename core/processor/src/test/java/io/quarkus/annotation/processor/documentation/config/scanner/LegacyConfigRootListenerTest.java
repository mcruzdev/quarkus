package io.quarkus.annotation.processor.documentation.config.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.quarkus.annotation.processor.documentation.config.model.ConfigPhase;
import io.quarkus.annotation.processor.documentation.config.model.Extension;
import io.quarkus.annotation.processor.documentation.config.model.ExtensionModule;
import io.quarkus.annotation.processor.util.Config;
import io.quarkus.annotation.processor.util.Utils;

class AbstractConfigListenerTest {

    @ParameterizedTest
    @EnumSource(value = ConfigPhase.class, names = { "RUN_TIME", "BUILD_AND_RUN_TIME_FIXED" })
    void shouldThrowExceptionWhenConfigIsRuntimeAndModuleTypeIsDeployment(ConfigPhase phase) {
        Config config = new Config(
                new ExtensionModule(
                        "io.quarkus",
                        "quarkus-extension-deployment",
                        ExtensionModule.ExtensionModuleType.DEPLOYMENT,
                        new Extension(
                                "io.quarkus",
                                "quarkus-extension-deployment",
                                "extension",
                                null,
                                false,
                                null,
                                true),
                        true),
                false,
                false);

        AbstractConfigListener listener = new LegacyConfigRootListener(
                config, new Utils(
                        null),
                new ConfigCollector());

        Assertions.assertThrows(IllegalStateException.class, () -> {
            listener.validateRuntimeConfigOnDeploymentModules(phase);
        });
    }

    @ParameterizedTest
    @EnumSource(value = ConfigPhase.class, names = { "RUN_TIME", "BUILD_AND_RUN_TIME_FIXED" })
    void shouldNotThrowExceptionWhenConfigIsRuntimeAndModuleTypeIsRunTime(ConfigPhase phase) {
        Config config = new Config(
                new ExtensionModule(
                        "io.quarkus",
                        "quarkus-extension",
                        ExtensionModule.ExtensionModuleType.RUNTIME,
                        new Extension(
                                "io.quarkus",
                                "quarkus-extension",
                                "extension",
                                Extension.NameSource.EXTENSION_METADATA,
                                false,
                                "https://quarkus.io/guide/quarkus-extension",
                                true),
                        true),
                false,
                false);

        AbstractConfigListener listener = new LegacyConfigRootListener(
                config, new Utils(
                        null),
                new ConfigCollector());

        Assertions.assertDoesNotThrow(() -> {
            listener.validateRuntimeConfigOnDeploymentModules(phase);
        });
    }
}
