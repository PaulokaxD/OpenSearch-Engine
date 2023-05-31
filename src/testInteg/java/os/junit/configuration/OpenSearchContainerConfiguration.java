package os.junit.configuration;

/**
 * The OpenSearchContainerConfiguration class is a utility class that stores the metadata needed to configure
 * an OpenSearch Docker container.
 */
public class OpenSearchContainerConfiguration {
    /** Container's port opened to contact with the OpenSearch server. */
    public static final int PORT = 9200;
    /** Scheme used to connect to the OpenSearch container. */
    public static final String SCHEME = "http";
    /** Hostname where the OpenSearch container is located. */
    public static final String HOSTNAME = "localhost";

    private OpenSearchContainerConfiguration() {
    }
}
