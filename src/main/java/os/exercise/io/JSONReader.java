package os.exercise.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import os.exercise.models.Article;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {

    private JSONReader() {
    }

    public static List<Article> readArticlesFile(Path path) {
        ObjectMapper mapper = new ObjectMapper();
        List<Article> articles = new ArrayList<>();
        String line;
        try(BufferedReader reader = Files.newBufferedReader(path)){
            while ((line = reader.readLine()) != null) {
                Article article = mapper.readValue(line, Article.class);
                articles.add(article);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return articles;
    }

    public static List<List<Article>> readArticlesFileIntoPartition(Path path, Integer partitionSize) {
        List<Article> collection = readArticlesFile(path);

        List<List<Article>> partitions = new ArrayList<>();

        for (int i = 0; i < collection.size(); i += partitionSize) {
            partitions.add(collection.subList(i, Math.min(i + partitionSize, collection.size())));
        }
        return partitions;
    }

    public static Integer getFileNumber(Path path) {
        return readArticlesFile(path).size();
    }
}
