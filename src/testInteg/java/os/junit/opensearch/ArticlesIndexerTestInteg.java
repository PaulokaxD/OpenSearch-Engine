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
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import os.exercise.io.JsonReader;
import os.exercise.models.Article;
import os.exercise.opensearch.ArticlesIndexer;

/**
 * Unitary tests for the ArticlesIndexer class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticlesIndexerTestInteg {

    private static final DockerImageName OS_IMAGE = DockerImageName.parse("opensearchproject/opensearch:latest");

    @Container
    private OpensearchContainer osContainer = new OpensearchContainer(OS_IMAGE);

    private Path articlesPath;
    private String index;
    private String hostname;
    private String scheme;
    private Integer port;
    private String indexURL;
    private String searchURL;

    private Article article1;
    private Article article2;

    private ObjectMapper mapper;

    @BeforeAll
    void setUp() throws UnirestException {
        articlesPath = Paths.get("src\\test\\resources", "articles-example.json");

        article1 = new Article();
        article2 = new Article();

        index = "articles";
        scheme = "http";
        osContainer.start();
        hostname = osContainer.getHost();
        port = osContainer.getFirstMappedPort();
        indexURL = String.format("http://%s:%d/%s", hostname, port, index);
        searchURL = String.format("%s/_search", indexURL);

        mapper = new ObjectMapper();

        Unirest.setTimeouts(60000, 60000);
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

        Unirest.put(indexURL)
                .header("Content-Type", "application/json")
                .body(mappingsQuery)
                .asString();
    }

    @Test
    void givenCorrectJSONWhenIndexArticlesThenSuccess() throws IOException, UnirestException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        List<Article> articles = JsonReader.readArticlesFile(articlesPath);
        BulkRequest request = indexer.getBulkRequest(index, articles);

        assertTrue(indexer.indexArticles(articles, request));

        int hits = getHits();

        assertEquals(articles.size(),hits);

        clearIndex();
    }

    @Test
    void givenWrongJSONWhenIndexArticlesThenFailure() throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));
//      Throw(new IOException());

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);
        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);

        assertFalse(indexer.indexArticles(articlesBatch, request));
    }

    @Test
    void givenWrongArticleWhenIndexArticlesThenSuccess() throws IOException, UnirestException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));

        Article wrongArticle = new Article();
        wrongArticle.setPubDate("notADate");

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> failedArticles = JsonReader.readArticlesFile(articlesPath);
        failedArticles.add(wrongArticle);
        BulkRequest request = indexer.getBulkRequest(index, failedArticles);

        assertTrue(indexer.indexArticles(failedArticles, request));

        int hits = getHits();

        assertEquals(failedArticles.size() - 1, hits);

        clearIndex();
    }

    private int getHits() throws UnirestException, JsonProcessingException {
        HttpResponse<String> response = Unirest.post(searchURL)
                .header("Content-Type", "application/json")
                .body("{\r\n    \"query\": {\r\n        \"match_all\": {}\r\n    }\r\n}")
                .asString();

        JsonNode jsonResponse = mapper.readTree(response.getBody());
        return Integer.parseInt(jsonResponse.findValue("hits").findValue("total").findValue("value").toString());
    }

    private void clearIndex() {

    }
}