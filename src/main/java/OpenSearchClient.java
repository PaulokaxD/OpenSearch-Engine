import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

public class OpenSearchClientExample {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "/full/path/to/keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "password-to-keystore");

        final HttpHost host = new HttpHost("https", "localhost", 9200);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        //Only for demo purposes. Don't specify your credentials in code.
        credentialsProvider.setCredentials(new AuthScope(host),
                new UsernamePasswordCredentials("admin", "admin".toCharArray()));

        //Initialize the client with SSL and TLS enabled
        final RestClient restClient = RestClient.builder(host).
                setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                }).build();

        final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        final OpenSearchClient client = new OpenSearchClient(transport);
    }
}