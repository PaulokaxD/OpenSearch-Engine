package os.exercise.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import os.exercise.models.Article;

/**
 * The JSONReader class offers some utilities to load articles given a json file.
 */
public class JsonReader {

    private static final Logger LOGGER = LogManager.getLogger(JsonReader.class);

    private JsonReader() {
    }

    /**
     * Given a json with articles return a list of those articles.
     *
     * @param path The location of a json with articles.
     * @return A list of articles.
     */
    public static List<Article> readArticlesFile(Path path) {
        ObjectMapper mapper = new ObjectMapper();
        List<Article> articles = new ArrayList<>();
        String line;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            while ((line = reader.readLine()) != null) {
                Article article = mapper.readValue(line, Article.class);
                articles.add(article);
            }
        } catch (IOException e) {
            LOGGER.error("There was an error reading the file {}: {}", path, e);
        }
        return articles;
    }

    /**
     * Given a json with articles and a batch size return a list of batches.
     *
     * @param path The location of a json with articles.
     * @param partitionSize The size of the batches.
     * @return A list of those articles organized in batches of size {@code partitionSize}.
     */
    public static List<List<Article>> readArticlesFileIntoPartition(Path path, Integer partitionSize) {
        List<Article> collection = readArticlesFile(path);

        List<List<Article>> partitions = new ArrayList<>();

        for (int i = 0; i < collection.size(); i += partitionSize) {
            partitions.add(collection.subList(i, Math.min(i + partitionSize, collection.size())));
        }
        return partitions;
    }

    /**
     * Given a json with articles return the number of articles in that file.
     *
     * @param path The location of a json with articles.
     * @return The number of articles.
     */
    public static Integer getFileNumber(Path path) {
        return readArticlesFile(path).size();
    }
}
