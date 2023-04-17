import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {

    public static List<Article> readFile(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Article> articles = new ArrayList<>();
        String line;
        File file = path.toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
            Article article = mapper.readValue(line, Article.class);
            articles.add(article);
        }
        return articles;
    }

    public static List<List<Article>> readFileIntoPartition(Path path, Integer partitionSize) throws IOException {
        List<Article> collection = readFile(path);

        List<List<Article>> partitions = new ArrayList<>();

        for (int i = 0; i < collection.size(); i += partitionSize) {
            partitions.add(collection.subList(i, Math.min(i + partitionSize, collection.size())));
        }
        return partitions;
    }

    public static Integer getFileNumber(Path path) throws IOException {
        return readFile(path).size();
    }
}
