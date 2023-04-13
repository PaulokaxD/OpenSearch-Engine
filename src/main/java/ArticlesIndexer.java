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

    private final RestHighLevelClient client;

    public ArticlesIndexer(String host, Integer port, String scheme) {
        this.client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme)));
    }

    public void bulkAppendArticles(String index, List<Article> articles) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        for (Article article : articles) {
            BulkRequest request = new BulkRequest();
            String jsonArticle = mapper.writeValueAsString(article);

            IndexRequest indexRequest = new IndexRequest(index)
                    .source(jsonArticle, XContentType.JSON);

            request.add(indexRequest);
            client.bulk(request, RequestOptions.DEFAULT);
            // TODO: Add logguer: "Number of failed requests: " + bulkResponse.hasFailures()

        }

    }

    public void close() {
        try{
            client.close();
        }catch (IOException e){
            System.out.println(e.getStackTrace());
        }
    }
}