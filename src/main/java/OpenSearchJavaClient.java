import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
//import org.apache.hc.core5.http.HttpHost;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

public class OpenSearchJavaClient {
    public static void main(String[] args) throws Exception {

        final HttpHost host = new HttpHost("https://localhost", 9200);
        final RestClient restClient = RestClient.builder(host).build();
        final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        final OpenSearchClient client = new OpenSearchClient(transport);

    }
}