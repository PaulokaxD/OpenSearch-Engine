package os.exercise.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import os.exercise.models.Article;

import java.io.IOException;
import java.util.List;

public class ArticlesIndexer {

    private final RestHighLevelClient client;

    public ArticlesIndexer(String host, Integer port, String scheme) {
        this.client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme)));
    }

    public void bulkAppendArticles(String index, List<Article> articles) {
        ObjectMapper mapper = new ObjectMapper();

        for (Article article : articles) {
            try{
                BulkRequest request = new BulkRequest();
                String jsonArticle = mapper.writeValueAsString(article);

                IndexRequest indexRequest = new IndexRequest(index)
                        .source(jsonArticle, XContentType.JSON);

                request.add(indexRequest);
                BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

                if(bulkResponse.hasFailures()){
                    System.out.println("There was an error indexing this batch");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public void close() {
        try{
            client.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}