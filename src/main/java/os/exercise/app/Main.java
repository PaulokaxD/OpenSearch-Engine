package os.exercise.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import os.exercise.io.JsonReader;
import os.exercise.models.Article;
import os.exercise.opensearch.ArticlesIndexer;

/**
 * Main class where all the funcionalities meets.
 */
public class Main {

    private static final Integer N_INDEXING_FILES = 100;
    private static final String INDEX_NAME = "articles";
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String DATA_PATH = "data";

    private static final int PORT = 9200;
    private static final String HOST = "localhost";
    private static final String SCHEME = "http";

    private Main() {
    }

    /**
     * Main function where all the funcionalities meet to index the content of a folder with jsons containing articles
     * into an OpenSearch index.
     *
     * @param args not used yet.
     */
    public static void main(String[] args) {
        Path folderPath = Paths.get(DATA_PATH);
        List<Path> paths = getPaths(folderPath);

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(HOST, PORT, SCHEME)));

        ObjectMapper mapper = new ObjectMapper();
        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        indexFile(paths, indexer);
    }

    /**
     * Index the articles in the json files using the {@ArticlesIndexer}.
     *
     * @param paths A list of json files.
     * @param indexer An {@ArticlesIndexer}.
     */
    public static void indexFile(List<Path> paths, ArticlesIndexer indexer) {
        int nFile = 1;
        for(Path path : paths) {
            LOGGER.info("Processing file {}", path);

            int fileNumber = JsonReader.getFileNumber(path);
            List<List<Article>> articlesBatches = JsonReader.readArticlesFileIntoPartition(path, N_INDEXING_FILES);

            Integer nBatch = 1;
            for(List<Article> articlesBatch : articlesBatches){
                indexer.bulkAppendArticles(INDEX_NAME, articlesBatch);
                LOGGER.info("File {}/{} -> {}/{},", nFile, paths.size(),
                        Math.min(N_INDEXING_FILES*nBatch, fileNumber), fileNumber);
                nBatch++;
            }
            nFile++;
        }
    }

    /**
     * Get all json files path in a folder.
     *
     * @param folderPath The data folder.
     * @return A list of all json paths.
     */
    public static List<Path> getPaths(Path folderPath) {
        List<Path> paths = new ArrayList<>();

        try (Stream<Path> pathStream = Files.walk(folderPath)) {
            paths = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e){
            LOGGER.warn(e);
        }
        return paths;
    }

}

