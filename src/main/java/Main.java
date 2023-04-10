import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        Path folderPath = Paths.get("data");
        List<Path> pathStream = Files.walk(folderPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toList());
        for(Path path : pathStream) {
            List<Article> articles = JSONReader.readFile(path);
        }
    }

}

