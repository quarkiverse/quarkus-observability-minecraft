package io.quarkiverse.observability.minecraft.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class MinecrafterDevServicesDisabledTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.minecrafter.devservices.enabled", "false");

    @Test
    public void devServiceShouldNotStartWhenDisabled() {
        String baseUrl = ConfigProvider.getConfig()
                .getValue("quarkus.minecrafter.base-url", String.class);
        assertEquals("http://localhost:8081/", baseUrl);
    }
}
