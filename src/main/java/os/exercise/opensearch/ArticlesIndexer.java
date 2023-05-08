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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import os.exercise.models.Article;

/**
 * The ArticlesIndexer class main purpose is to offer the utility of indexing a list of articles
 * {@link #bulkAppendArticles(String, List)}.
 */
public class ArticlesIndexer {

    private static final Logger LOGGER = LogManager.getLogger(ArticlesIndexer.class);
    private static final Logger INDEXING_LOGGER = LogManager.getLogger("FailedIndexedArticlesLogger");

    private final RestHighLevelClient client;
    private final ObjectMapper mapper;

    /**
     * Constructor of the ArticlesIndexer.
     *
     * @param client {@code RestHighLevelClient}.
     * @param mapper {@code ObjectMapper} to translate an Article into json format.
     */
    public ArticlesIndexer(RestHighLevelClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * Given a list of articles and the name of an index of articles, index the articles and returns a boolean flag
     * that is true if success.
     *
     * @param index The name of the articles index.
     * @param articles A list of articles.
     * @return True if success else false.
     */
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

    /**
     * Execute the request and returns a boolean flag that is true if success.
     * If there is a problem, the failed articles are logged.
     *
     * @param articles A list of articles.
     * @param request A {@code BulkRequest}.
     * @return True if success else false.
     */
    public boolean indexArticles(List<Article> articles, BulkRequest request) {
        boolean success = true;
        try {
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            List<String> failures = getNotIndexedArticles(bulkResponse, articles);
            if(!failures.isEmpty()){
                String failureMessage = bulkResponse.buildFailureMessage();
                INDEXING_LOGGER.error(
"There was an error indexing this batch: {}\nThe failures occurred in the article(s) with Id: {}",
                        failureMessage, failures);
                LOGGER.warn(
"Some articles were not indexed properly. Consult the failed-indexed-articles.log file for more information.");
            }
        } catch (IOException e){
            LOGGER.error("There was an error indexing the batch {} -> {}: {}",
                    articles.get(0).getId(), articles.get(articles.size() - 1).getId(), e);
            success = false;
        }
        return success;
    }

    /**
     * Given a {@code BulkResponse}, return a list with all articles that were not correctly indexed.
     *
     * @param bulkResponse A {@code BulkResponse}.
     * @param articles A list of articles.
     * @return A list with articles ids.
     */
    public List<String> getNotIndexedArticles(BulkResponse bulkResponse, List<Article> articles){
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

    /**
     * Creates a {@code BulkRequest} to append the list of articles into the index.
     *
     * @param index The name of the articles index.
     * @param articles A list of articles.
     * @return A {@code BulkRequest}.
     * @throws JsonProcessingException when the articles does not follow the Article.java format.
     */
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