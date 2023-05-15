package os.junit.opensearch;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private Article article1;
    private Article article2;

    @BeforeAll
    void setUp(){


        articlesPath = Paths.get("src\\test\\resources", "articles-example.json");

        article1 = new Article();
        article2 = new Article();

        index = "articles";
        scheme = "http";
        osContainer.start();
        hostname = osContainer.getHost();
        port = osContainer.getFirstMappedPort();
    }

//    @ClassRule
//    public final ExternalResource externalResource = new ExternalResource() {
//        @Override
//        protected void before()  {
//            osContainer.start();
//            hostname = osContainer.getHost();
//            port = osContainer.getFirstMappedPort();
//
////                try (RestClient client = RestClient
////                        .builder(HttpHost.create(os_container.getHttpHostAddress()))
////                        .build()) {
////
////                    final Response response = client.performRequest(new Request("GET", "/_cluster/health"));
////                    ...
////                }
//        }
//
//        @Override
//        protected void after() {
//            osContainer.close();
//        }
//    };

    @Test
    void givenCorrectJSONWhenIndexArticlesThenSuccess() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        List<Article> articles = JsonReader.readArticlesFile(articlesPath);
        BulkRequest request = indexer.getBulkRequest(index, articles);

        assertTrue(indexer.indexArticles(articles, request));
        // TODO: HTTP REQUEST TO SEE NUMBER OF FILES
    }

    @Test
    void givenWrongJSONWhenIndexArticlesThenFailure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));
//      Throw(new IOException());

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> articlesBatch = Arrays.asList(article1, article2);
        BulkRequest request = indexer.getBulkRequest(index, articlesBatch);

        assertFalse(indexer.indexArticles(articlesBatch, request));
    }

    @Test
    void givenWrongArticleWhenIndexArticlesThenSuccess() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));

        Article wrongArticle = new Article();
        wrongArticle.setPubDate("notADate");

        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);
        List<Article> failedArticles = JsonReader.readArticlesFile(articlesPath);
        failedArticles.add(wrongArticle);
        BulkRequest request = indexer.getBulkRequest(index, failedArticles);

        assertTrue(indexer.indexArticles(failedArticles, request));
        // TODO: HTTP REQUEST TO SEE NUMBER OF FILES AND FAILURE
    }
}