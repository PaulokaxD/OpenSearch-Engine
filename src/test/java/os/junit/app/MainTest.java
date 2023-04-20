package os.junit.app;

import org.junit.jupiter.api.Test;
import os.exercise.app.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void givenAValidPathWhenGetPathsThenSuccess(){
        Path folderPath = Paths.get("data"); //TODO
        List<Path> paths = Main.getPaths(folderPath);

        long count = -1;
        try (Stream<Path> countingPaths = Files.list(folderPath)) {
            count = countingPaths.count();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(count, paths.size());
        for (Path path : paths) {
            assertTrue(path.toString().endsWith(".json"));
        }
    }

    @Test
    void givenAnInvalidPathWhenGetPathsThenNumberOfFilesEqualsZero() {
        Path folderPath = Paths.get("src/test/resources/non-existing-folder");
        List<Path> paths = Main.getPaths(folderPath);
        assertEquals(0, paths.size());
    }


// TODO: COMO TESTEO indexFile Y Main? Tendria que ser teset de integracion entonces no?
}
