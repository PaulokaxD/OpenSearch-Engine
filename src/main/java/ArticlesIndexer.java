import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

public class ArticlesIndexer {

    private static final String INDEX_NAME = "articles";

    private final RestHighLevelClient client;

    public ArticlesIndexer() {
        this.client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }

    public void bulkAppendArticles(List<Article> articles) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        for (Article article : articles) {
            BulkRequest request = new BulkRequest();
            String jsonArticle = mapper.writeValueAsString(article);

            IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                    .source(jsonArticle, XContentType.JSON);

            request.add(indexRequest);
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            // TODO: Add logguer: "Number of failed requests: " + bulkResponse.hasFailures()

        }

    }

    public void close() throws IOException {
        client.close();
    }
}