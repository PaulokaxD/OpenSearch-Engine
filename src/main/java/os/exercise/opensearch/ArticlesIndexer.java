package os.exercise.opensearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import os.exercise.models.Article;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArticlesIndexer {

    private static final Logger LOGGER = LogManager.getLogger(ArticlesIndexer.class);
    private static final Logger indexingLogger = LogManager.getLogger("FailedIndexedArticlesLogger");

    private final RestHighLevelClient client;
    private final ObjectMapper mapper;

    public ArticlesIndexer(RestHighLevelClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public boolean bulkAppendArticles(String index, List<Article> articles) {
        boolean success;
        try {
            BulkRequest request = getBulkRequest(index, articles);
            success = indexArticles(articles, request);
        } catch (IOException e){
            success = false;
        }
        return success;
    }

    public boolean indexArticles(List<Article> articles, BulkRequest request) {
        boolean success = true;
        try {
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            List<String> failures = getErrorMessages(bulkResponse, articles);
            if(!failures.isEmpty()){
                String failureMessage = bulkResponse.buildFailureMessage();
                indexingLogger.error("There was an error indexing this batch: {}\n" +
                                "The failures occurred in the article(s) with Id: {}",
                        failureMessage, failures);
                LOGGER.warn("Some articles were not indexed properly. Consult the failed-indexed-articles.log" +
                        " file for more information.");
            }
        } catch (IOException e){
            LOGGER.error("There was an error indexing the batch {} -> {}: {}",
                    articles.get(0).getId(), articles.get(articles.size() - 1).getId(), e);
            success = false;
        }
        return success;
    }

    public List<String> getErrorMessages(BulkResponse bulkResponse, List<Article> articles){
        List<String> failures = new ArrayList<>();
        if(bulkResponse.hasFailures()) {
            BulkItemResponse[] responses = bulkResponse.getItems();
            failures = IntStream.range(0, responses.length)
                    .filter(i -> responses[i].getFailure() != null)
                    .boxed()
                    .map(i -> articles.get(i).getId())
                    .collect(Collectors.toList());
        }
        return failures;
    }

    public BulkRequest getBulkRequest(String index, List<Article> articles) throws JsonProcessingException {
        BulkRequest request = new BulkRequest();

        for (Article article : articles) {
            try{
                String jsonArticle = mapper.writeValueAsString(article);

                IndexRequest indexRequest = new IndexRequest(index)
                        .source(jsonArticle, XContentType.JSON);
                request.add(indexRequest);
            }catch (JsonProcessingException e){
                LOGGER.error("There was an error parsing the article {}: {}", article, e);
                throw e;
            }
        }
        return request;
    }
}