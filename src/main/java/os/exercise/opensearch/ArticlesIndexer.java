package os.exercise.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.opensearch.action.bulk.BulkItemResponse;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArticlesIndexer {

    private static final Logger logger = LogManager.getLogger(ArticlesIndexer.class);
    private static final Logger indexingLogger = LogManager.getLogger("FailedIndexedArticlesLogger");

    private final RestHighLevelClient client;

    public ArticlesIndexer(String host, Integer port, String scheme) {
        this.client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme)));
    }

    public void bulkAppendArticles(String index, List<Article> articles) {
        ObjectMapper mapper = new ObjectMapper();
        BulkRequest request = new BulkRequest();

        for (Article article : articles) {
            try{
                String jsonArticle = mapper.writeValueAsString(article);

                IndexRequest indexRequest = new IndexRequest(index)
                        .source(jsonArticle, XContentType.JSON);

                request.add(indexRequest);

            }catch (IOException e){
                logger.error("There was an error parsing the article {}: {}", article, e);
            }
        }

        try {
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            if(bulkResponse.hasFailures()){
                BulkItemResponse[] responses = bulkResponse.getItems();
                List<String> failures = IntStream.range(0, responses.length)
                        .filter(i -> responses[i].getFailure() != null)
                        .boxed()
                        .map(i -> articles.get(i).getId())
                        .collect(Collectors.toList());
                String failureMessage = bulkResponse.buildFailureMessage();
                indexingLogger.error("There was an error indexing this batch: {}\n" +
                                "The failures occurred in the article(s) with Id: {}",
                        failureMessage, failures);
                logger.warn("Some articles were not indexed properly. Consult the failed-indexed-articles.log" +
                        " file for more information.");
            }

        } catch (IOException e){
            logger.error("There was an error indexing the batch {} -> {}: {}",
                    articles.get(0).getId(), articles.get(articles.size() - 1).getId(), e);
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