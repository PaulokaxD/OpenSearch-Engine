package os.junit.opensearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import os.exercise.io.JsonReader;
import os.exercise.models.Article;
import os.exercise.opensearch.ArticlesIndexer;
import os.junit.configuration.OpenSearchContainerConfiguration;
import os.junit.extensions.OpenSearchContainerSetupExtension;

/**
 * Integration tests for the ArticlesIndexer class.
 */
@ExtendWith(OpenSearchContainerSetupExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticlesIndexerTestInteg {

    private Path articlesPath = Paths.get("src\\test\\resources", "articles-example.json");
    private String index = "articles";
    private String hostname = OpenSearchContainerConfiguration.HOSTNAME;
    private String scheme  = OpenSearchContainerConfiguration.SCHEME;
    private String indexURL;
    private String searchURL;
    private String matchAllQuery;

    private ObjectMapper mapper;
    private RestHighLevelClient client;
    private ArticlesIndexer indexer;

    @BeforeAll
    void setUp() throws UnirestException {
        int port = OpenSearchContainerConfiguration.PORT;

        indexURL = String.format("http://%s:%d/%s", hostname, port, index);
        searchURL = String.format("%s/_search", indexURL);

        mapper = new ObjectMapper();

        matchAllQuery = "{\r\n    \"query\": {\r\n        \"match_all\": {}\r\n    }\r\n}";
        String mappingsQuery = String.join("",
                "{\"settings\":{\"index\":{\"number_of_replicas\":0},",
                "\"analysis\":{\"filter\":{\"english_stop\":{\"type\":\"stop\",",
                "\"stopwords\":\"_english_\"},",
                "\"english_stemmer\":{\"type\":\"stemmer\",",
                "\"language\":\"english\"},",
                "\"english_possessive_stemmer\":{\"type\":\"stemmer\",",
                "\"language\":\"possessive_english\"},",
                "\"lang_synonym\":{\"type\":\"synonym\",",
                "\"synonyms\":[\"eng => English\"]}},",
                "\"analyzer\":{\"english_analyzer\":{\"tokenizer\":\"standard\",",
                "\"filter\":[\"english_possessive_stemmer\",\"lowercase\",",
                "\"english_stop\",\"english_stemmer\"]},",
                "\"autocomplete\":{\"tokenizer\":\"autocomplete\"},",
                "\"keyword_analyzer\":{\"tokenizer\":\"keyword\",",
                "\"filter\":[\"lowercase\"]},",
                "\"lang_expansion\":{\"tokenizer\":\"keyword\",",
                "\"filter\":[\"lang_synonym\"]}},",
                "\"tokenizer\":{\"autocomplete\":{\"type\":\"edge_ngram\",",
                "\"min_gram\":1,\"max_gram\":9,\"token_chars\":[\"digit\"]}}}},",
                "\"mappings\":{\"properties\":{\"id\":{\"type\":\"text\",",
                "\"analyzer\":\"autocomplete\"},",
                "\"article_title\":{\"type\":\"text\",",
                "\"analyzer\":\"english_analyzer\"},",
                "\"abstract_text\":{\"type\":\"text\",",
                "\"analyzer\":\"english_analyzer\"},",
                "\"keywords\":{\"type\":\"text\",",
                "\"analyzer\":\"keyword_analyzer\",\"fielddata\":\"true\"},",
                "\"pub_types\":{\"type\":\"keyword\"},",
                "\"language\":{\"type\":\"text\",",
                "\"analyzer\":\"lang_expansion\",\"fielddata\":\"true\"},",
                "\"pub_date\":{\"type\":\"date\"}}}}");

        Unirest.setTimeouts(0, 0);
        applyHttpInstruction(indexURL, mappingsQuery);

        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));
        indexer = new ArticlesIndexer(client, mapper);
    }

    @Test
    void givenCorrectJSONWhenIndexArticlesThenSuccess() throws IOException, UnirestException {
        List<Article> articles = JsonReader.readArticlesFile(articlesPath);
        BulkRequest request = indexer.getBulkRequest(index, articles);

        assertTrue(indexer.indexArticles(articles, request));
        refreshIndex(client, index);

        int hits = getHits();

        assertEquals(articles.size(),hits);

        clearIndex();
    }

    @Test
    void givenWrongJSONWhenIndexArticlesThenFailure() throws IOException {
        RestHighLevelClient wrongClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, 8080, scheme)));
        ArticlesIndexer wrongIndexer = new ArticlesIndexer(wrongClient, mapper);

        List<Article> failedArticles = JsonReader.readArticlesFile(articlesPath);
        BulkRequest request = wrongIndexer.getBulkRequest(index, failedArticles);

        assertFalse(wrongIndexer.indexArticles(failedArticles, request));
    }

    @Test
    void givenWrongArticleWhenIndexArticlesThenSuccess() throws IOException, UnirestException {
        Article wrongArticle = new Article();
        wrongArticle.setPubDate("notADate");
        List<Article> failedArticles = JsonReader.readArticlesFile(articlesPath);
        failedArticles.add(wrongArticle);

        BulkRequest request = indexer.getBulkRequest(index, failedArticles);

        assertTrue(indexer.indexArticles(failedArticles, request));

        refreshIndex(client, index);

        int hits = getHits();
        int correctFilesNumber = failedArticles.size() - 1;

        assertEquals(correctFilesNumber, hits);

        clearIndex();
    }

    private int getHits() throws UnirestException, JsonProcessingException {
        HttpResponse<String> response = applyHttpInstruction(searchURL, matchAllQuery);

        JsonNode jsonResponse = mapper.readTree(response.getBody());
        return Integer.parseInt(jsonResponse.findValue("hits").findValue("total").findValue("value").toString());
    }

    private void clearIndex() throws UnirestException {
        String deleteURL = String.format("%s/_delete_by_query", indexURL);
        applyHttpInstruction(deleteURL, matchAllQuery);
    }

    private HttpResponse<String> applyHttpInstruction(String url, String body) throws UnirestException {
        return Unirest.post(url)
                .header("Content-Type", "application/json")
                .body(body)
                .asString();
    }

    private void refreshIndex(RestHighLevelClient restClient, String refreshedIndex) throws IOException {
        RefreshRequest refreshRequest = new RefreshRequest(refreshedIndex);
        restClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
    }
}