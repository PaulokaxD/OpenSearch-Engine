package os.exercise.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import os.exercise.io.JSONReader;
import os.exercise.opensearch.ArticlesIndexer;
import os.exercise.models.Article;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Integer N_INDEXING_FILES = 100;
    private static final String INDEX_NAME = "articles";
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String DATA_PATH = "data";

    public static void main(String[] args) {
        Path folderPath = Paths.get(DATA_PATH);
        List<Path> paths = getPaths(folderPath);

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        ObjectMapper mapper = new ObjectMapper();
        ArticlesIndexer indexer = new ArticlesIndexer(client, mapper);

        indexFile(paths, indexer);
    }

    public static void indexFile(List<Path> paths, ArticlesIndexer indexer) {
        int nFile = 1;
        for(Path path : paths) {
            logger.info("Processing file {}", path);

            int fileNumber = JSONReader.getFileNumber(path);
            List<List<Article>> articlesBatches = JSONReader.readArticlesFileIntoPartition(path, N_INDEXING_FILES);

            Integer nBatch = 1;
            for(List<Article> articlesBatch : articlesBatches){
                indexer.bulkAppendArticles(INDEX_NAME, articlesBatch);
                logger.info("File {}/{} -> {}/{},", nFile, paths.size(),
                        Math.min(N_INDEXING_FILES*nBatch, fileNumber), fileNumber);
                nBatch++;
            }
            nFile++;
        }
    }

    public static List<Path> getPaths(Path folderPath) {
        List<Path> paths = new ArrayList<>();

        try (Stream<Path> pathStream = Files.walk(folderPath)) {
            paths = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e){
            e.printStackTrace();
        }
        return paths;
    }

}

