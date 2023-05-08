package os.junit.opensearch;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opensearch.OpenSearchException;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.shard.ShardId;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import os.exercise.models.Article;
import os.exercise.opensearch.ArticlesIndexer;

/**
 * Unitary tests for the ArticlesIndexer class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticlesIndexerTest {

    private String index;

    private Article article1;
    private Article article2;

    private BulkItemResponse response1;
    private BulkItemResponse response2;
    private BulkItemResponse failedResponse;

    @BeforeAll
    void setUp(){
        article1 = new Article();
        article2 = new Article();

        index = "articles";

        ShardId shardId = new ShardId(index, "_na_", -1);
        IndexResponse indexResponse1 = new IndexResponse(shardId, "_RYHt4cBxgR8sHVdlD-S",
                3599353, 8, 1, true);
        response1 = new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, indexResponse1);

        IndexResponse indexResponse2 = new IndexResponse(shardId, "_xYHt4cBxgR8sHVdlD-S",
                3599354, 8, 1, true);
        response2 = new BulkItemResponse(1, DocWriteRequest.OpType.INDEX, indexResponse2);

        Exception indexingException = new OpenSearchException("OpenSearch exception [type=illegal_argument_exception, "
                + "reason=failed to parse date field [foo] with format [strict_date_optional_time||epoch_millis]]");
        BulkItemResponse.Failure failure = new BulkItemResponse.Failure(index,
                "BBY6t4cBxgR8sHVdXkCN", indexingException);
        failedResponse = new BulkItemResponse(1, DocWriteRequest.OpType.INDEX, failure);
    }

    @Test
    void givenTheArticlesIndexWhenBulkAppendArticlesThenSuccess() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        BulkItemResponse[] responses = {response1, response2};

        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenReturn(new BulkResponse(responses,2L));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        List<Article> articlesBatch = Arrays.asList(article1, article2);
        boolean success = indexer.bulkAppendArticles(index, articlesBatch);
        assertTrue(success);
    }

    @Test
    void givenWrongJSONWhenBulkAppendArticlesThenFailure() throws IOException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Article.class))).thenThrow(new JsonProcessingException(""){});

        BulkItemResponse[] responses = {response1, response2};

        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenReturn(new BulkResponse(responses,2L));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);

        boolean failure = indexer.bulkAppendArticles(index, articlesBatch);
        assertFalse(failure);
    }

    @Test
    void givenExceptionWhileIndexingWhenBulkAppendArticlesThenFailure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenThrow(new IOException());

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);

        boolean failure = indexer.bulkAppendArticles(index, articlesBatch);
        assertFalse(failure);
    }

    @Test
    void givenCorrectJSONWhenGetBulkRequestThenSuccess() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2, article1);

        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);
        assertEquals(request.numberOfActions(), articlesBatch.size());
    }

    @Test
    void givenWrongJSONWhenGetBulkRequestThenFailure() throws JsonProcessingException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Article.class))).thenThrow(new JsonProcessingException(""){});

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);

        assertThrows(IOException.class, () -> indexer.getBulkRequest(index, articlesBatch));
    }

    @Test
    void givenSuccessfulBulkResponseWhenGetErrorMessagesThenLengthErrorMessagesEqualsZero(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        ObjectMapper mapper = new ObjectMapper();
        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        BulkItemResponse[] responses = {response1, response2};
        BulkResponse bulkResponse = new BulkResponse(responses,2L);
        List<Article> articlesBatch = Arrays.asList(article1, article2);

        List<String> failures = indexer.getNotIndexedArticles(bulkResponse, articlesBatch);
        assertEquals(0, failures.size());
    }

    @Test
    void givenBulkResponseWithFailuresWhenGetErrorMessagesThenLengthErrorMessagesBiggerThanZero(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        ObjectMapper mapper = new ObjectMapper();
        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        BulkItemResponse[] responses = {response1, failedResponse};
        BulkResponse bulkResponse = new BulkResponse(responses,2L);
        List<Article> articlesBatch = Arrays.asList(article1, article2);

        List<String> failures = indexer.getNotIndexedArticles(bulkResponse, articlesBatch);
        assertTrue(failures.size() > 0);
    }

    @Test
    void givenCorrectJSONWhenIndexArticlesThenSuccess() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        BulkItemResponse[] responses = {response1, response2};

        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenReturn(new BulkResponse(responses,2L));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);
        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);

        assertTrue(indexer.indexArticles(articlesBatch, request));
    }

    @Test
    void givenWrongJSONWhenIndexArticlesThenFailure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenThrow(new IOException());

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);
        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);

        assertFalse(indexer.indexArticles(articlesBatch, request));
    }

    @Test
    void givenWrongArticleWhenIndexArticlesThenSuccess() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        BulkItemResponse[] responses = {response1, failedResponse};
        RestHighLevelClient client = mock(RestHighLevelClient.class);
        when(client.bulk(isA(BulkRequest.class), isA(RequestOptions.class)))
                .thenReturn(new BulkResponse(responses,2L));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);
        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);

        assertTrue(indexer.indexArticles(articlesBatch, request));
    }
}