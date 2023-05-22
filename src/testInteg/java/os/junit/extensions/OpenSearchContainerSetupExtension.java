package os.junit.extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import java.util.List;
import os.junit.configuration.OpenSearchContainerConfiguration;

/**
 * The OpenSearchContainerSetupExtension class is a JUnit extension that sets up an OpenSearch Docker container
 * using the metadata stored in the {@link OpenSearchContainerConfiguration} class.
 */
public class OpenSearchContainerSetupExtension implements BeforeAllCallback {

    private static final DockerImageName OS_IMAGE = DockerImageName.parse("opensearchproject/opensearch:latest");
    @Container
    private OpensearchContainer osContainer = new OpensearchContainer(OS_IMAGE);

    public void beforeAll(ExtensionContext context) {
        Integer port = OpenSearchContainerConfiguration.PORT;
        osContainer.setPortBindings(List.of(String.format("%d:%d", port, port)));
        osContainer.start();
    }
}
